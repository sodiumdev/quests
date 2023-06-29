package zip.sodium.quests.event;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class QuestXPUpdateEvent extends Event implements Cancellable {
    public static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean cancelled = false;

    private final double xpDiff;
    private final OfflinePlayer player;

    public QuestXPUpdateEvent(OfflinePlayer player, double xpDiff){
        this.xpDiff = xpDiff;
        this.player = player;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public double getXpDifference() {
        return xpDiff;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
