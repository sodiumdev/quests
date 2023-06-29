package zip.sodium.quests.quest;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import zip.sodium.quests.Quests;
import zip.sodium.quests.event.QuestAssignEvent;

import java.util.concurrent.CompletableFuture;

public abstract class Quest {
    private final String id;

    private double progress = 0;

    private final boolean cacheable;

    public Quest(String id, boolean cacheable) {
        this.id = id;
        this.cacheable = cacheable;
    }

    public String getId() {
        return id;
    }

    public boolean isCacheable() {
        return cacheable;
    }

    public CompletableFuture<Boolean> assignToPlayer(final OfflinePlayer player) {
        final QuestAssignEvent event = new QuestAssignEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return null;

        return Quests.getDatabaseHelper().assignQuestToPlayer(player, this);
    }

    public CompletableFuture<Document> getData(final Player player) {
        return Quests.getDatabaseHelper().getQuestData(player, getId());
    }

    public final void tick(final Player player) {
        if (cacheable) {
            cacheableTick(player);
            return;
        }

        if (!didProgress(player))
            return;

        Quests.getDatabaseHelper().getQuestProgress(player, getId()).thenAccept(progress -> {
            final double delta = progressed(player);

            if (delta == 0d)
                return;

            progress += delta;
            if (progress > 100)
                progress = 100d;

            Quests.getDatabaseHelper().setQuestProgress(player, getId(), progress);
        });
    }

    private void cacheableTick(final Player player) {
        if (!didProgress(player))
            return;

        final double delta = progressed(player);

        if (delta == 0d)
            return;

        progress += delta;
        if (progress >= 100) {
            completed(player);

            progress = 100;
        }
    }

    public void save(final Player player) {
        if (!cacheable) return;

        Quests.getDatabaseHelper().setQuestProgress(player, getId(), progress);
    }

    /**
     * Triggered every 10 ticks (half a second) for every player that has the quest.
     * @return If the player progressed
     */
    protected abstract boolean didProgress(final Player player);

    /**
     * Triggered every 10 ticks (half a second) for every player that has the quest if Quest#didProgress was true.
     * Saves to database every minute if quest is cacheable.
     * Or else, saves to database every 10 ticks.
     * @return The value to add to the progress (0, 100)
     * @see Quest#didProgress
     */
    protected abstract float progressed(final Player player);

    /**
     * Triggered when quest is completed
     */
    protected abstract void completed(final Player player);
}
