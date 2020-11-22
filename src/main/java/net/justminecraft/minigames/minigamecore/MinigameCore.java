package net.justminecraft.minigames.minigamecore;

import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class MinigameCore extends JavaPlugin {

    public static MinigameCore mc = null;
    public World world = null;
    public ArrayList<Minigame> minigames = new ArrayList<>();
    public ArrayList<Game> queues = new ArrayList<>();
    ArrayList<Game> games = new ArrayList<>();
    Runnable queueTicker = () -> {
        for (Game g : queues.toArray(new Game[0])) {
            if (g.players.size() < g.minigame.getMinPlayers())
                g.queueStart = 0;
            if (g.players.size() >= g.minigame.getMaxPlayers() || (g.queueStart > 0 && (60 - (System.currentTimeMillis() - g.queueStart) / 1000) < 1))
                g.startGame();
            if (g.queueStart == 0 && g.players.size() >= g.minigame.getMinPlayers())
                g.queueStart = System.currentTimeMillis() - 500;
            for (Player p : g.players) {
                if (g.players.size() >= g.minigame.getMinPlayers()) {
                    g.minigame.message(p, "Starting in " + (60 - (System.currentTimeMillis() - g.queueStart) / 1000)
                            + "s (" + g.players.size() + "/" + g.minigame.getMaxPlayers() + " players)");
                } else {
                    g.minigame.message(p, "Waiting for more players...");
                }
            }
        }

        // AFK Ticker
        for (Game g : games.toArray(new Game[0])) {
            for (Player p : g.players.toArray(new Player[0])) {
                Long l = g.afkTimer.get(p.getUniqueId());
                if (l != null && l < System.currentTimeMillis() - 300000) {
                    PlayerData.get(p.getUniqueId()).incrementStat("afk_kicks");
                    PlayerData.get(p.getUniqueId()).incrementStat(g.minigame.getMinigameName(), "afk_kicks");
                    g.playerLeave(p);
                    p.sendMessage(ChatColor.YELLOW + "You have been kicked from the game for inactivity");
                } else if (l != null && l < System.currentTimeMillis() - 270000) {
                    p.sendMessage(ChatColor.YELLOW + "If you do not move soon you will be kicked from the game for inactivity");
                }
            }
        }

        // Ontime
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerData d = PlayerData.get(p.getUniqueId());
            d.setStat(d.getStat("ontime") + 5, "ontime");
            Game g = getGame(p);
            if (g != null)
                d.setStat(d.getStat(g.minigame.getMinigameName(), "ontime") + 5,
                        g.minigame.getMinigameName(), "ontime");
        }
    };

    public static MinigameCore core() {
        return mc;
    }

    public Minigame getMinigame(String n) {
        Minigame mg = null;
        for (Minigame m : minigames) {
            if (m.getMinigameName().equalsIgnoreCase(n)) {
                mg = m;
                break;
            }
        }
        return mg;
    }

    public Game getQueue(Minigame mg) {
        for (Game g : queues) {
            if (g.minigame.equals(mg)) {
                return g;
            }
        }
        return new Game(mg);
    }

    public Game getQueue(Player p) {
        for (Game g : queues) {
            if (g.players.contains(p)) {
               return g;
            }
        }
        return null;
    }

    public Game getGame(Player p) {
        for (Game g : games) {
            if (g.players.contains(p)) {
                return g;
            }
        }
        return null;
    }

    public ArrayList<Game> getGames(Minigame m) {
        ArrayList<Game> g = new ArrayList<>();
        for (Game a : games) {
            if (a.minigame.equals(m)) {
                g.add(a);
            }
        }
        return g;
    }

    public void registerMinigame(Minigame mg) {
        for (World world : Bukkit.getWorlds()) {
            if (world.getName().startsWith(mg.getMinigameName() + "-")) {
                for (Player player : world.getPlayers()) {
                    MG.resetPlayer(player);
                    player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation().add(0.5, 0, 0.5));
                }

                Bukkit.unloadWorld(world, false);
            }
        }

        for (File file : new File(".").listFiles()) {
            if (file.getName().startsWith(mg.getMinigameName() + "-")) {
                Game.deleteFiles(file);
            }
        }

        minigames.add(mg);
        mg.newGame();
    }

    public void joinMinigameQueue(Player p, String mg) {
        Game q = getQueue(p);
        Minigame m = getMinigame(mg);
        if (q != null) {
            p.sendMessage(ChatColor.RED + "You are already in a queue!");
        } else if (m == null) {
            p.sendMessage(ChatColor.RED + "Couldn't find the minigame " + mg);
        } else {
            Game g = getQueue(m);
            g.players.add(p);
            message(p, "Joined queue for " + m.getMinigameName() + " (" + g.players.size() + "/" + m.getMaxPlayers() + ")");
            Bukkit.getPluginManager().callEvent(new QueueJoinEvent(p, m));
            if (g.players.size() >= m.getMaxPlayers())
                g.startGame();
        }
    }

    public void leaveMinigameQueue(Player p) {
        Game q = getQueue(p);
        Game g = getGame(p);
        if (g != null) {
            g.playerLeave(p);
            message(p, "Left the game of " + g.minigame.getMinigameName());
        } else if (q == null) {
            p.sendMessage(ChatColor.RED + "You are not in a queue!");
        } else {
            q.players.remove(p);
            message(p, "Left queue for " + q.minigame.getMinigameName());
            Bukkit.getPluginManager().callEvent(new QueueLeaveEvent(p, q.minigame));
        }
    }

    public void message(Player p, String msg) {
        p.sendMessage(ChatColor.GOLD + "[Minigames] " + msg);
    }

    public void onEnable() {
        getLogger().info("Enabling MinigameCore...");

        mc = this;
        PlayerData.dir = new File(this.getDataFolder(), "players");
        world = Bukkit.createWorld(new WorldCreator("minigames").type(WorldType.FLAT).generatorSettings("2;0;1;"));
        world.setSpawnLocation(0, 70, 0);
        world.getBlockAt(0, 69, 0).setType(Material.GOLD_BLOCK);
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Lag(), 100L, 1L);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, queueTicker, 100L, 100L);
        getServer().getPluginManager().registerEvents(new QueueListener(this), this);
        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        //Logger logger = new Logger(this);
        //getServer().getPluginManager().registerEvents(logger, this);
        //getServer().getScheduler().scheduleSyncRepeatingTask(this, logger, 20L, 20L);
        //getServer().getPluginManager().registerEvents(new AntiCheat(), this);

        getLogger().info("MinigameCore is enabled!");
    }

    public void onDisable() {
        getLogger().info("Disabling MinigameCore...");
        ArrayList<UUID> uuids = new ArrayList<>();
        for (UUID u : PlayerData.cache.keySet())
            uuids.add(u);
        for (UUID u : uuids)
            PlayerData.save(u);
        getLogger().info("MinigameCore is disabled!");
    }

    public void listMinigames(CommandSender s) {
        s.sendMessage("Avaliable Minigames:");
        for (Minigame g : minigames) {
            s.sendMessage("    " + g.getMinigameName() + " (" + getQueue(g).players.size() + " players in queue)");
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("minigame")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must be a player to do that!");
                return true;
            }
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("join")) {
                    if (args.length > 1) {
                        joinMinigameQueue((Player) sender, args[1]);
                    } else
                        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " join <minigame>");
                } else if (args[0].equalsIgnoreCase("leave")) {
                    leaveMinigameQueue((Player) sender);
                }
                return true;
            }
            sender.sendMessage(ChatColor.RED + "Usage:");
            sender.sendMessage(ChatColor.RED + "/" + label + " join <minigame>");
            sender.sendMessage(ChatColor.RED + "/" + label + " leave");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("join")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must be a player to do that!");
                return true;
            }
            if (args.length > 0) {
                joinMinigameQueue((Player) sender, args[0]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <minigame>");
                listMinigames(sender);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("leave")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must be a player to do that!");
                return true;
            }
            leaveMinigameQueue((Player) sender);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("spectate")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must be a player to do that!");
                return true;
            }

            if (args.length > 0) {
                Player player = Bukkit.getPlayer(args[0]);
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + "Could not find player " + args[0]);
                } else if (MG.core().getGame((Player) sender) != null) {
                    sender.sendMessage(ChatColor.RED + "You are currently in a minigame");
                } else if (MG.core().getGame(player) == null) {
                    sender.sendMessage(ChatColor.RED + player.getName() + " is not in a minigame");
                } else {
                    ((Player) sender).setGameMode(GameMode.SPECTATOR);
                    ((Player) sender).teleport(player);
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <player>");
            }

            return true;
        }

        if (cmd.getName().equalsIgnoreCase("forcestart")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must be a player to do that!");
                return true;
            }
            Game q = getQueue((Player) sender);
            if (q == null) {
                sender.sendMessage(ChatColor.RED + "You must be in a queue to do that!");
                return true;
            }
            //if(q.queue.size() < q.getMinPlayers()){
            //	sender.sendMessage(ChatColor.RED + "Not enough players in queue!");
            //	return true;
            //}
            q.startGame();
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("money")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must be a player to do that!");
                return true;
            }
            Player p = (Player) sender;
            PlayerData d = PlayerData.get(p.getUniqueId());
            p.sendMessage("You have " + d.money + " coins");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("stats")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must be a player to do that!");
                return true;
            }
            Player p = (Player) sender;
            PlayerData d = PlayerData.get(p.getUniqueId());
            try {
                JSONObject o = d.stats;
                for (int i = 0; i < args.length; i++) {
                    if (o.containsKey(args[i]))
                        o = (JSONObject) o.get(args[i]);
                    else {
                        for (Object k : o.keySet())
                            if (k.toString().equalsIgnoreCase(args[i]))
                                o = (JSONObject) o.get(k);
                    }
                }
                p.sendMessage(StringUtils.join(args, " ") + " Stats:");
                for (Object k : o.keySet()) {
                    Object v = o.get(k);
                    if (v instanceof Long)
                        p.sendMessage(k + "=" + v);
                    else if (v instanceof Integer)
                        p.sendMessage(k + "=" + v);
                }
                if (args.length == 0)
                    p.sendMessage(ChatColor.YELLOW + "Use /stats <minigame> to see minigame specific stats");
            } catch (Exception e) {
                p.sendMessage("Invalid minigame! (or maybe you've never played it?)");
            }
            return true;
        }
        return false;
    }
}
