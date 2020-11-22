package net.justminecraft.minigames.minigamecore;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class MG {
    public static MinigameCore core() {
        return MinigameCore.core();
    }

    public static void resetPlayer(Player p) {
        p.setMaxHealth(20);
        for (PotionEffect e : p.getActivePotionEffects())
            p.removePotionEffect(e.getType());
        p.setHealth(20);
        p.getInventory().clear();
        p.getEquipment().clear();
        p.setAllowFlight(false);
        p.setPlayerTime(0, true);
        p.setSaturation(20);
        p.setFoodLevel(20);
        p.setFireTicks(0);
        p.setGameMode(GameMode.SURVIVAL);
        p.setCanPickupItems(true);
        p.setExhaustion(0);
        p.setLevel(0);
        p.setFallDistance(0);
        for (Player p2 : Bukkit.getOnlinePlayers()) // Remove Player.hidePlayer effects (this way removes any chance of it glitching)
            if (p != p2)
                p.showPlayer(p2);
    }
}
