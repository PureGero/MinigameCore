package net.justminecraft.minigames.minigamecore;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class GameListener implements Listener {
    MinigameCore core;

    public GameListener(MinigameCore core) {
        this.core = core;
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        Game g = core.getGame(e.getPlayer());
        if (g != null)
            g.playerLeave(e.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Game g = core.getGame(e.getPlayer());
        if (g != null && g.reg && e.getTo().getWorld() != g.world) // If they teleport away from the game
            g.playerLeave(e.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Game g = core.getGame(e.getEntity());
        if (g != null) {
            g.onPlayerDeath(e.getEntity());
            e.setKeepInventory(true);
            e.setKeepLevel(true);
            e.setDeathMessage(null);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (core.getGame(p) == null) {
                e.setDamage(0);
            } else if (e.getCause() == DamageCause.VOID) {
                e.setCancelled(true);
                //p.setHealth(0);
                core.getGame(p).onPlayerDeath(p);
            }
        }
    }


    @EventHandler
    public void debugHandler(PlayerCommandPreprocessEvent e) {
        String m = e.getMessage().toLowerCase();
        if (m.equals("/d") || m.startsWith("/d ")) {
            Player p = e.getPlayer();
            if (m.contains(" gl") || m.contains(" gamelist")) {
                p.sendMessage("Games:");
                for (Game g : core.games) {
                    p.sendMessage("  " + g.world.getName() + ") " + g.minigame.getName() + " - " + g.players.size() + " players");
                }
            } else if (m.contains(" ql") || m.contains(" queuelist")) {
                p.sendMessage("Queues:");
                for (Game g : core.queues) {
                    p.sendMessage("  " + g.minigame.getName() + " - " + g.players.size() + "/" + g.minigame.getMaxPlayers() + " players" + (g.queueStart > 0 ? " - " + (60 - (System.currentTimeMillis() - g.queueStart) / 1000) + "s" : ""));
                }
            } else if (m.contains(" tps")) {
                p.sendMessage("TPS: " + Math.floor(Lag.getTPS() * 100) / 100);
            } else if (m.contains(" loadedchunks")) {
                String s = new String();
                for (Chunk c : p.getWorld().getLoadedChunks())
                    s += c.getX() + "," + c.getZ() + " ";
                p.sendMessage("Loadedchunks: " + s);
            } else {
                p.sendMessage("/d [gl/gamelist/ql/queuelist/tps/loadedchunks]");
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            Game g = core.getGame((Player) e.getEntity());
            if (g != null && g.disableHunger)
                e.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Game g = core.getGame(e.getPlayer());
        if (g != null && g.disableBlockBreaking)
            e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Game g = core.getGame(e.getPlayer());
        if (g != null && g.disableBlockPlacing)
            e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            Game g = core.getGame((Player) e.getEntity());
            if (g != null && g.disablePvP)
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Game g = core.getGame(e.getPlayer());
        if (g != null)
            g.afkTimer.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        MG.resetPlayer(e.getPlayer());
        e.getPlayer().teleport(Bukkit.getWorlds().get(0).getSpawnLocation().add(Math.random(), 0, Math.random()));
    }
}
