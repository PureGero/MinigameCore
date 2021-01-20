package net.justminecraft.minigames.minigamecore;

import com.connorlinfoot.titleapi.TitleAPI;
import org.bukkit.ChatColor;

public class EndGameCountdown implements Runnable {
    private final Game game;
    private final String winningTeamName;
    private int countdown = 10;

    public EndGameCountdown(Game game, String winningTeamName) {
        this.game = game;
        this.winningTeamName = winningTeamName;
        run();
    }

    @Override
    public void run() {
        if (countdown == 0) {
            game.postFinishGame();
            return;
        }

        game.world.getPlayers().forEach(player -> TitleAPI.sendTitle(
                player, 0, countdown == 1 ? 20 : 30, 0,
                ChatColor.GREEN + winningTeamName + " won!",
                ChatColor.GOLD + "Returning to lobby in " + countdown + " seconds..."
        ));

        countdown--;
        MG.core().getServer().getScheduler().runTaskLater(MG.core(), this, 20);
    }
}
