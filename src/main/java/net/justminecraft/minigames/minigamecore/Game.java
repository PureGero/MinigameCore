package net.justminecraft.minigames.minigamecore;

import net.justminecraft.minigames.minigamecore.worldbuffer.WorldBuffer;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Game {
    public ArrayList<Player> players = new ArrayList<>();
    public HashMap<UUID, Long> afkTimer = new HashMap<>();
    public Minigame minigame;
    public boolean disableHunger = true;
    public boolean disableBlockBreaking = true;
    public boolean disableBlockPlacing = true;
    public boolean disablePvP = true;
    public int moneyPerDeath = 2;
    public int moneyPerWin = 10;
    public int coinMultiplier = 0;
    public boolean generated = false;
    public boolean reg = false;
    public String worldName;
    public World world = null;
    public long queueStart = 0;

    public Game(Minigame mg) {
        minigame = mg;
        worldName = mg.getMinigameName() + "-" + Double.toString(Math.random()).substring(2);

        Bukkit.getScheduler().runTaskAsynchronously(MinigameCore.core(), () -> {
            WorldBuffer buffer = new WorldBuffer(new File(worldName));
            mg.generateWorld(this, buffer);
            buffer.save();

            generated = true;
        });

        MG.core().queues.add(this);
    }

    public void startGame() {
        MG.core().queues.remove(this);
        MG.core().games.add(this);

        minigame.newGame();

        world = Bukkit.createWorld(new WorldCreator(worldName).type(WorldType.FLAT).generatorSettings("2;0;1;"));

        ArrayList<String> donars = new ArrayList<>();
        for (Player p : players) {
            if (p.hasPermission("minigame.quadruplecoins"))
                coinMultiplier += 4;
            else if (p.hasPermission("minigame.triplecoins"))
                coinMultiplier += 3;
            else if (p.hasPermission("minigame.doublecoins"))
                coinMultiplier += 2;
            if (p.hasPermission("minigame.doublecoins") || p.hasPermission("minigame.triplecoins")
                    || p.hasPermission("minigame.quadruplecoins")) {
                if (p.getScoreboard() != null && p.getScoreboard().getPlayerTeam(p) != null
                        && p.getScoreboard().getPlayerTeam(p).getPrefix() != null) {
                    donars.add(p.getScoreboard().getPlayerTeam(p).getPrefix() + p.getName());
                } else
                    donars.add(p.getName());
            }
            afkTimer.put(p.getUniqueId(), System.currentTimeMillis());
            PlayerData.get(p.getUniqueId()).incrementStat("games");
            PlayerData.get(p.getUniqueId()).incrementStat(minigame.getMinigameName(), "games");
            minigame.message(p, "Joining " + minigame.getMinigameName() + " game");
        }
        if (donars.size() > 0) {
            String cm = coinMultiplier + "x";
            if (coinMultiplier == 2) {
                cm = "double";
            } else if (coinMultiplier == 3) {
                cm = "triple";
            }
            String s = "A donar, ";
            if (donars.size() > 1) s = "Donars ";
            for (int i = 0; i < donars.size(); i++) {
                s += donars.get(i) + ChatColor.AQUA;
                if (i < donars.size() - 2) s += ", ";
                else if (i == donars.size() - 2) s += " and ";
            }
            broadcast(ChatColor.AQUA + s + (donars.size() == 1 ? " is" : " are") + " in your game. You get " + ChatColor.BOLD + cm + ChatColor.AQUA + " coins!");
        }
        if (coinMultiplier == 0) // No donars
            coinMultiplier = 1;
        reg = true;

        minigame.startGame(this);
    }

    public void giveMoney(Player p, int money) {
        if (money > 0) {
            PlayerData.get(p.getUniqueId()).money += money * coinMultiplier;
            p.sendMessage(ChatColor.GOLD + "+" + money * coinMultiplier + " coins");
            PlayerData d = PlayerData.get(p.getUniqueId());
            d.setStat(d.getStat("coins_earnt") + money * coinMultiplier, "coins_earnt");
            d.setStat(d.getStat(minigame.getMinigameName(), "coins_earnt") + money * coinMultiplier,
                    minigame.getMinigameName(), "coins_earnt");
        }
    }

    public void doCountdown(Runnable callback) {
        new Countdown(this, callback);
    }

    public void broadcast(String m) {
        for (Player p : world.getPlayers()) {
            minigame.message(p, m);
        }
    }

    public void broadcastRaw(String m) {
        for (Player p : world.getPlayers()) {
            p.sendMessage(m);
        }
    }

    protected void playerLeave(final Player p) {
        prePlayerLeave(p);
        players.remove(p);
        postPlayerLeave(p);
        MG.resetPlayer(p);
        p.setGameMode(GameMode.SPECTATOR);
        p.teleport(players.get(0));
    }

    public void onPlayerDeath(Player p) {
        playerLeave(p); // Players lose if they die, unless this method is overridden
    }

    public void prePlayerLeave(Player p) {
        broadcast(p.getDisplayName() + " has died (" + (players.size() - 1) + " players left)");
    }

    public void postPlayerLeave(Player p) {
        if (moneyPerDeath > 0)
            for (Player p2 : players) {
                giveMoney(p2, moneyPerDeath);
            }
        if (players.size() < 2) {
            if (players.size() > 0) {
                broadcast(players.get(0).getDisplayName() + " has won!!!");
                if (moneyPerWin > 0)
                    giveMoney(players.get(0), moneyPerWin);
            }
            finishGame();
        }
    }

    public final void finishGame() {
        while (players.size() > 0) {
            Player p = players.remove(0);
            PlayerData.get(p.getUniqueId()).incrementStat("wins");
            PlayerData.get(p.getUniqueId()).incrementStat(minigame.getMinigameName(), "wins");
        }
        MG.core().games.remove(this);

        for (Player player : world.getPlayers()) {
            MG.resetPlayer(player);
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation().add(Math.random(), 0, Math.random()));
        }

        MG.core().getLogger().info("Unloading world " + world.getName() + " in 30 seconds...");
        Bukkit.getScheduler().runTaskLater(MG.core(), () -> Bukkit.unloadWorld(world, false), 30*20L);
        Bukkit.getScheduler().runTaskLaterAsynchronously(MG.core(), () -> deleteFiles(new File(worldName)), 60*20L);
    }

    public static void deleteFiles(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                deleteFiles(f);
            }
        }
        file.delete();
    }
}
