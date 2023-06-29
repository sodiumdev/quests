package zip.sodium.quests.db;

import com.google.common.collect.Lists;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import zip.sodium.quests.event.QuestXPUpdateEvent;
import zip.sodium.quests.quest.Quest;
import zip.sodium.quests.quest.QuestRegistery;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DataHelper implements DatabaseHelper {
    private final MongoDatabase database;
    private final JavaPlugin plugin;

    public DataHelper(final JavaPlugin plugin, final MongoDatabase database) {
        this.database = database;
        this.plugin = plugin;
    }

    private MongoCollection<Document> getPlayerDataCollection() {
        return database.getCollection("playerData");
    }

    private MongoCollection<Document> getQuestProgressCollection() {
        return database.getCollection("questProgress");
    }

    @Override
    public CompletableFuture<Boolean> addXPToPlayer(OfflinePlayer player, double xp) {
        return CompletableFuture.supplyAsync(() -> {
            final double oldXP;
            try {
                oldXP = getPlayerXP(player).get();
            } catch (InterruptedException | ExecutionException e) {
                return false;
            }

            final QuestXPUpdateEvent event = new QuestXPUpdateEvent(player, oldXP, oldXP + xp);

            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.getPluginManager().callEvent(event);
                }
            }.runTask(plugin);

            final Document playerData = getPlayerDataCollection()
                    .findOneAndUpdate(Filters.eq("uuid", player.getUniqueId().toString()), Updates.inc("xp", xp));

            return playerData != null;
        });
    }

    @Override
    public CompletableFuture<Boolean> removeXPFromPlayer(OfflinePlayer player, double xp) {
        return addXPToPlayer(player, -xp);
    }

    @Override
    public CompletableFuture<Quest[]> getQuestsAssignedToPlayer(OfflinePlayer player) {
        return CompletableFuture.supplyAsync(() -> Lists.newArrayList(getQuestProgressCollection()
                        .find(new Document("uuid", player.getUniqueId().toString()))
                        .cursor())
                .stream()
                .map(document -> QuestRegistery.getQuest(document.getString("quest_id")))
                .toArray(Quest[]::new));
    }

    @Override
    public CompletableFuture<Boolean> playerHasXPAccount(OfflinePlayer player) {
        return CompletableFuture.supplyAsync(() -> getPlayerDataCollection().countDocuments(new Document("uuid", player.getUniqueId().toString())) != 0);
    }

    @Override
    public CompletableFuture<Boolean> createXPAccount(OfflinePlayer player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (playerHasXPAccount(player).get()) return false;
            } catch (InterruptedException | ExecutionException ignored) {}

            final Document document = new Document();
            document.put("uuid", player.getUniqueId().toString());
            document.put("xp", 0.0d);

            getPlayerDataCollection().insertOne(document);

            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> assignQuestToPlayer(OfflinePlayer player, Quest quest) {
        return CompletableFuture.supplyAsync(() -> {
            final Document document = new Document();
            document.put("uuid", player.getUniqueId().toString());
            document.put("quest_id", quest.getId());
            document.put("progress", 0.0d);

            getQuestProgressCollection().insertOne(document);

            return true;
        });
    }

    @Override
    public CompletableFuture<Document> getQuestData(OfflinePlayer player, String questId) {
        return CompletableFuture.supplyAsync(() -> getQuestProgressCollection()
                .find(Filters.and(
                        Filters.eq("uuid", player.getUniqueId().toString()),
                        Filters.eq("quest_id", questId))
                ).first());
    }

    @Override
    public CompletableFuture<Double> getQuestProgress(OfflinePlayer player, String questId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final Document data = getQuestData(player, questId).get();

                return data.getDouble("progress");
            } catch (InterruptedException | ExecutionException ignored) {}

            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> setQuestProgress(OfflinePlayer player, String questId, double progress) {
        return CompletableFuture.supplyAsync(() -> getQuestProgressCollection().findOneAndUpdate(
                Filters.and(
                        Filters.eq("uuid", player.getUniqueId().toString()),
                        Filters.eq("quest_id", questId)
                ),
                Updates.set("progress", progress)
        ) != null);
    }

    @Override
    public CompletableFuture<Double> getPlayerXP(OfflinePlayer player) {
        return CompletableFuture.supplyAsync(() -> Objects.requireNonNull(
                getPlayerDataCollection()
                        .find(
                                Filters.eq("uuid", player.getUniqueId().toString())
                        ).first(),
                "Player \"" + player.getName() + "\" does not have an XP account!").getDouble("xp"));
    }
}
