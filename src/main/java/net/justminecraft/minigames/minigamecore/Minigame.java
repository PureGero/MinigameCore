package net.justminecraft.minigames.minigamecore;

import net.justminecraft.minigames.minigamecore.worldbuffer.WorldBuffer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public abstract class Minigame extends JavaPlugin {

    public abstract String getMinigameName();

    public abstract int getMinPlayers();

    public abstract int getMaxPlayers();

    /**
     * Generate a world for a game
     */
    public abstract void generateWorld(Game game, WorldBuffer buffer);

    /**
     * Start a game
     */
    public abstract void startGame(Game game);

    public void message(Player p, String msg) {
        p.sendMessage(ChatColor.GOLD + "[" + getMinigameName() + "] " + msg);
    }
}
