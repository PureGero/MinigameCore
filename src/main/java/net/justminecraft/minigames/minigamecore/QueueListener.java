package net.justminecraft.minigames.minigamecore;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class QueueListener implements Listener {
    MinigameCore core;

    public QueueListener(MinigameCore core) {
        this.core = core;
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        Game q = core.getQueue(e.getPlayer());
        if (q != null) {
            q.players.remove(e.getPlayer());
            Bukkit.getPluginManager().callEvent(new QueueLeaveEvent(e.getPlayer(), q.minigame));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuitMonitor(final PlayerQuitEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(core, () -> PlayerData.save(e.getPlayer().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreJoin(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() == Result.ALLOWED) {
            PlayerData p = PlayerData.get(e.getUniqueId());
            p.name = e.getName();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        if (e.getPlayer().getWorld() == Bukkit.getWorlds().get(0)) {
            e.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }
}
