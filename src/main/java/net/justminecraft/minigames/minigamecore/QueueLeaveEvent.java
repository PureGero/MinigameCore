package net.justminecraft.minigames.minigamecore;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class QueueLeaveEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    Minigame queue;

    public QueueLeaveEvent(Player who, Minigame queue) {
        super(who);
        this.queue = queue;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Minigame getQueue() {
        return queue;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
