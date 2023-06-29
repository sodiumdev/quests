package zip.sodium.quests.event;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import zip.sodium.quests.quest.Quest;

public class QuestAssignEvent extends Event implements Cancellable {
    public static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean cancelled = false;

    private final OfflinePlayer assignedPlayer;
    private final Quest quest;

    public QuestAssignEvent(OfflinePlayer assignedPlayer, Quest quest) {
        this.assignedPlayer = assignedPlayer;
        this.quest = quest;
    }

    public Quest getQuest() {
        return quest;
    }

    public OfflinePlayer getAssignedPlayer() {
        return assignedPlayer;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
