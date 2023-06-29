package zip.sodium.quests;

import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import zip.sodium.quests.command.QuestXPCommand;
import zip.sodium.quests.quest.Quest;
import zip.sodium.quests.quest.QuestRegistery;
import zip.sodium.quests.tabcompleter.QuestXPTabCompleter;

public class Quests extends JavaPlugin implements Listener {
    private MongoClient conn;
    private static MongoDatabase database;

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
        database = conn.getDatabase("QuestData");

        getCommand("questxp").setExecutor(new QuestXPCommand());
        getCommand("questxp").setTabCompleter(new QuestXPTabCompleter());

        Bukkit.getPluginManager().registerEvents(this, this);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    getQuestProgressCollection().find(Filters.eq("uuid", player.getUniqueId().toString())).subscribe(new Subscriber<>() {
                        @Override
                        public void onSubscribe(Subscription s) {
                            s.request(1);
                        }

                        @Override
                        public void onNext(final Document document) {
                            QuestRegistery.getQuest(document.getString("quest_id")).tick(player);
                        }

                        @Override
                        public void onError(Throwable t) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
                }
            }
        }.runTaskTimer(this, 0L, 10L);
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        final Player player = e.getPlayer();

        getPlayerDataCollection().countDocuments(Filters.eq("uuid", player.getUniqueId().toString())).subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(Long aLong) {
                if (aLong != 0)
                    return;

                final Document document = new Document();
                document.put("uuid", player.getUniqueId().toString());
                document.put("xp", 0.0d);

                getPlayerDataCollection().insertOne(document).subscribe(new EmptySubscriber<>());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    public static MongoCollection<Document> getQuestProgressCollection() {
        return database.getCollection("questProgress");
    }

    public static MongoCollection<Document> getPlayerDataCollection() {
        return database.getCollection("playerData");
    }

    @Override
    public void onDisable() {
        conn.close();
    }
}
