package zip.sodium.quests.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zip.sodium.quests.Quests;

import java.util.List;

public class QuestXP implements TabExecutor {
    public QuestXP(final PluginCommand command) {
        command.setTabCompleter(this);
        command.setExecutor(this);
    }

    private boolean sendError(@NotNull final CommandSender sender, @NotNull final String... messages) {
        sender.sendMessage(messages);
        return false;
    }

    private Double parseDouble(@NotNull final String string) {
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException e) {
            return null;
        }
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

                final Double parsedDouble = parseDouble(args[2]);
                if (parsedDouble == null)
                    return sendError(sender, ChatColor.RED + "Expected a valid number!");

                Quests.getDatabaseHelper().addXPToPlayer(player, parsedDouble).thenAccept(success -> {
                    if (!success) {
                        sender.sendMessage(ChatColor.RED + "Failed to add the specified amount of xp to the specified player.");
                        return;
                    }

                    sender.sendMessage(ChatColor.GREEN + "Successfully added the specified amount of xp to the specified player.");
                });
            }

            case "remove" -> {
                if (!sender.hasPermission("zip.sodium.quests.questxp.remove"))
                    return sendError(sender, ChatColor.RED + "You do not have the permission to do this!");

                final Double parsedDouble = parseDouble(args[2]);
                if (parsedDouble == null)
                    return sendError(sender, ChatColor.RED + "Expected a valid number!");

                Quests.getDatabaseHelper().removeXPFromPlayer(player, parsedDouble).thenAccept(success -> {
                    if (!success) {
                        sender.sendMessage(ChatColor.RED + "Failed to remove the specified amount of xp from the specified player.");
                        return;
                    }

                    sender.sendMessage(ChatColor.GREEN + "Successfully removed the specified amount of xp from the specified player.");
                });
            }

            default -> {
                return sendError(sender, ChatColor.RED + "Expected valid subcommand! Valid subcommands are \"add\" and \"remove\".");
            }
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1)
            return List.of("add", "remove");
        if (args.length == 2)
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();

        return null;
    }
}
