package zip.sodium.quests;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import zip.sodium.quests.command.QuestXP;
import zip.sodium.quests.db.DataHelper;
import zip.sodium.quests.db.DatabaseHelper;
import zip.sodium.quests.quest.Quest;

import java.util.Objects;

public class Quests extends JavaPlugin implements Listener {
    private MongoClient conn;

    private static DatabaseHelper DATABASE_HELPER;

    public static DatabaseHelper getDatabaseHelper() {
        return DATABASE_HELPER;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        final String connectionString = getConfig().getString("connectionString");
        if (connectionString == null) {
            getLogger().severe("\"connectionString\" in config.yml is null!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        conn = MongoClients.create(connectionString);
        final MongoDatabase database = conn.getDatabase("QuestData");

        DATABASE_HELPER = new DataHelper(this, database);

        final PluginCommand command = Objects.requireNonNull(getCommand("questxp"), "\"questxp\" command does not exist in plugin.yml!");
        new QuestXP(command);

        Bukkit.getPluginManager().registerEvents(this, this);

        // Runs every half a second
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    DATABASE_HELPER.getQuestsAssignedToPlayer(player).thenAccept((quests) -> {
                        for (Quest quest : quests)
                            quest.tick(player);
                    });
                }
            }
        }.runTaskTimer(this, 0L, 10L);

        // Runs every minute
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    DATABASE_HELPER.getQuestsAssignedToPlayer(player).thenAccept((quests) -> {
                        for (Quest quest : quests) {
                            if (!quest.isCacheable())
                                continue;

                            quest.save(player);
                        }
                    });
                }
            }
        }.runTaskTimer(this, 0L, 1200L);
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        final Player player = e.getPlayer();

        DATABASE_HELPER.playerHasXPAccount(player).thenAccept((success) -> {
            if (!success) return;

            DATABASE_HELPER.createXPAccount(player);
        });
    }

    @Override
    public void onDisable() {
        conn.close();
    }
}
