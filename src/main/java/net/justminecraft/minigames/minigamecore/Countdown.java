package net.justminecraft.minigames.minigamecore;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Countdown implements Runnable {
    int t = 10;

    Game g;
    Runnable c;

    public Countdown(Game g, Runnable callback) {
        this.g = g;
        this.c = callback;
        g.minigame.getServer().getScheduler().scheduleSyncDelayedTask(g.minigame, this, 0);
    }

    @Override
    public void run() {
        if (t <= 0) {
            for (Player p : g.players) {
                p.playSound(p.getLocation(), Sound.LEVEL_UP, 2, 1);
                p.setLevel(0);
                p.setExp(0);
                g.minigame.message(p, "Game has started!");
            }
            c.run();
        } else {
            for (Player p : g.players) {
                p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);
                p.setLevel(t);
                if (t <= 5 || t == 10)
                    g.minigame.message(p, "Game starts in " + t);
            }
            t--;
            g.minigame.getServer().getScheduler().scheduleSyncDelayedTask(g.minigame, this, 20);
        }
    }
}
