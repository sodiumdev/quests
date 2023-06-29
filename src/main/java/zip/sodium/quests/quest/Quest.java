package zip.sodium.quests.quest;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import zip.sodium.quests.EmptySubscriber;
import zip.sodium.quests.Quests;
import zip.sodium.quests.event.QuestAssignEvent;
import zip.sodium.quests.event.QuestXPUpdateEvent;

public abstract class Quest {
    private final String id;

    private final boolean cacheable;

    private int cacheCount = 0;
    private final int maximumCacheCount;

    public Quest(String id, boolean cacheable, int maximumCacheCount) {
        this.id = id;
        this.cacheable = cacheable;
        this.maximumCacheCount = maximumCacheCount;
    }

    public Quest(String id) {
        this.id = id;
        this.cacheable = false;
        this.maximumCacheCount = 0;
    }

    public String getId() {
        return id;
    }

    public boolean isCacheable() {
        return cacheable;
    }

    public int getMaximumCacheCount() {
        return maximumCacheCount;
    }

    public Publisher<InsertOneResult> assignToPlayer(final OfflinePlayer player) {
        final QuestAssignEvent event = new QuestAssignEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return null;

        final Document document = new Document();
        document.put("uuid", player.getUniqueId().toString());
        document.put("quest_id", getId());
        document.put("progress", 0.0d);

        return Quests.getPlayerDataCollection().insertOne(document);
    }

    public Publisher<Document> getData(final Player player) {
        return Quests.getQuestProgressCollection().find(Filters.and(Filters.eq("uuid", player.getUniqueId().toString()), Filters.eq("quest_id", id)));
    }

    public static Publisher<Document> addXP(final OfflinePlayer player, double xp) {
        final QuestXPUpdateEvent event = new QuestXPUpdateEvent(player, xp);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return null;

        return Quests.getPlayerDataCollection().findOneAndUpdate(Filters.eq("uuid", player.getUniqueId().toString()), Updates.inc("xp", xp));
    }

    public static Publisher<Document> getPlayerData(final OfflinePlayer player) {
        return Quests.getPlayerDataCollection().find(Filters.eq("uuid", player.getUniqueId().toString()));
    }

    public final void tick(final Player player) {
        if (!didProgress(player))
            return;

        getData(player).subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(final Document document) {
                double progress = document.getDouble("progress");

                progress += progressed(player);
                if (progress > 100)
                    progress = 100;

                final double finalProgress = progress;
                if (cacheCount < maximumCacheCount) {
                    cacheCount++;
                    return;
                }

                cacheCount = 0;

                Quests.getQuestProgressCollection().updateOne(Filters.and(Filters.eq("uuid", player.getUniqueId().toString()), Filters.eq("quest_id", id)), Updates.set("progress", progress)).subscribe(new Subscriber<>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(1);
                    }

                    @Override
                    public void onNext(final UpdateResult updateResult) {}

                    @Override
                    public void onError(Throwable t) {}

                    @Override
                    public void onComplete() {
                        if (finalProgress >= 100) {
                            Quests.getQuestProgressCollection().deleteOne(Filters.and(Filters.eq("uuid", player.getUniqueId().toString()), Filters.eq("quest_id", id))).subscribe(new EmptySubscriber<>());

                            completed(player);
                        }
                    }
                });
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * Triggered every 10 ticks (half a second) for every player that has the quest.
     * @return If the player progressed
     */
    protected abstract boolean didProgress(final Player player);

    /**
     * Triggered every 10 ticks (half a second) for every player that has the quest if Quest#didProgress was true.
     * @return The value to add to the progress (0, 100)
     * @see Quest#didProgress
     */
    protected abstract float progressed(final Player player);

    /**
     * Triggered when quest is completed
     */
    protected abstract void completed(Player player);
}
