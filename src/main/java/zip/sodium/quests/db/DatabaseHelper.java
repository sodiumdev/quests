package zip.sodium.quests.db;

import org.bson.Document;
import org.bukkit.OfflinePlayer;
import zip.sodium.quests.quest.Quest;

import java.util.concurrent.CompletableFuture;

public interface DatabaseHelper {
    CompletableFuture<Boolean> addXPToPlayer(final OfflinePlayer player, double xp);
    CompletableFuture<Boolean> removeXPFromPlayer(final OfflinePlayer player, double xp);
    CompletableFuture<Quest[]> getQuestsAssignedToPlayer(final OfflinePlayer player);
    CompletableFuture<Boolean> playerHasXPAccount(final OfflinePlayer player);
    CompletableFuture<Boolean> createXPAccount(final OfflinePlayer player);
    CompletableFuture<Boolean> assignQuestToPlayer(final OfflinePlayer player, final Quest quest);
    CompletableFuture<Document> getQuestData(final OfflinePlayer player, final String questId);
    CompletableFuture<Double> getQuestProgress(final OfflinePlayer player, final String questId);
    CompletableFuture<Boolean> setQuestProgress(final OfflinePlayer player, final String questId, final double progress);
    CompletableFuture<Boolean> setQuestData(final OfflinePlayer player, final String questId, final Document data);
    CompletableFuture<Double> getPlayerXP(final OfflinePlayer player);
}
