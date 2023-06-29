package zip.sodium.quests.command;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import zip.sodium.quests.quest.Quest;

public class QuestXPCommand implements CommandExecutor {
    private boolean sendError(@NotNull final CommandSender sender, @NotNull final String... messages) {
        sender.sendMessage(messages);
        return false;
    }

    private double parseDouble(@NotNull CommandSender sender, @NotNull final String string) {
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException e) {
            sender.sendMessage("");
        }

        return 0;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1)
            return sendError(sender, ChatColor.RED + "Expected subcommand! Valid subcommands are \"add\" and \"remove\".");

        if (args.length < 2)
            return sendError(sender, ChatColor.RED + "Expected player name!");
        final String playerName = args[1];
        final Player player = Bukkit.getPlayer(playerName);
        if (player == null)
            return sendError(sender, ChatColor.RED + "Expected valid player name!");

        if (args.length < 3)
            return sendError(sender, ChatColor.RED + "Expected amount of xp!");

        switch (args[0]) {
            case "add" -> {
                if (!sender.hasPermission("zip.sodium.quests.questxp.add"))
                    return sendError(sender, ChatColor.RED + "You do not have the permission to do this!");

                Quest.addXP(player, parseDouble(sender, args[2])).subscribe(new Subscriber<>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(1);
                    }

                    @Override
                    public void onNext(Document document) {}

                    @Override
                    public void onError(Throwable t) {}

                    @Override
                    public void onComplete() {
                        sender.sendMessage(ChatColor.GREEN + "Successfully added the specified amount of xp to the specified player.");
                    }
                });
            }

            case "remove" -> {
                if (!sender.hasPermission("zip.sodium.quests.questxp.remove"))
                    return sendError(sender, ChatColor.RED + "You do not have the permission to do this!");

                Quest.addXP(player, -parseDouble(sender, args[2])).subscribe(new Subscriber<>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(1);
                    }

                    @Override
                    public void onNext(Document document) {}

                    @Override
                    public void onError(Throwable t) {}

                    @Override
                    public void onComplete() {
                        sender.sendMessage(ChatColor.GREEN + "Successfully removed the specified amount of xp from the specified player.");
                    }
                });
            }

            default -> {
                return sendError(sender, ChatColor.RED + "Expected valid subcommand! Valid subcommands are \"add\" and \"remove\".");
            }
        }

        return true;
    }
}
