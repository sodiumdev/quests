package zip.sodium.quests.quest;

import zip.sodium.quests.Quests;

import java.util.HashMap;

public class QuestRegistery {
    private static final HashMap<String, Quest> registery = new HashMap<>();

    public static void register(Quest quest) {
        if (registery.containsKey(quest.getId())) {
            Quests.getPlugin(Quests.class).getLogger().warning("Quest with id \"" + quest.getId() + "\" already exists!");
            return;
        }

        registery.put(quest.getId(), quest);
    }

    public static Quest getQuest(final String id) {
        return registery.get(id);
    }
}
