package net.stickmix.prisonevo.commands;

import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.command.Command;
import net.stickmix.game.api.command.Description;
import net.stickmix.game.api.command.Subcommand;
import net.stickmix.game.api.command.Usage;
import net.stickmix.game.api.command.access.CommandAccess;
import net.stickmix.game.api.command.access.CommandAccessResult;
import net.stickmix.game.api.user.User;
import net.stickmix.game.api.user.permission.PermissionGroup;
import net.stickmix.game.api.user.permission.UserPermission;
import net.stickmix.game.api.util.ChatUtil;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.data.GamePlayer;
import net.stickmix.prisonevo.items.EvoItem;
import net.stickmix.prisonevo.items.EvoItemsManager;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

@Command("items")
@Description("управление предметами")
@Usage("/items")
public class ItemsCommand implements CommandAccess {
    @Subcommand("add")
    @Description("выдать предмет.")
    @Usage("/items add")
    public void handleAdd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cУкажите имя игрока и айди предмета(ов)."));
            return;
        }
        GamePlayer player = PrisonEvo.getInstance().getAthenaManager().get(args[0]);
        User dmsPlayer = GameApi.getUserManager().get(args[0]);
        for (int i = 1; i < args.length; i++) {
            int id;
            try {
                id = Integer.parseInt(args[i]);
            } catch (NumberFormatException ignored) {
                return;
            }
            EvoItem item = EvoItemsManager.getItem(id);
            if (item == null) {
                sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cПредмета с ID &b%d&c не существует.", id));
                return;
            }
            if (player == null) {
                sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cИгрок не найден.", id));
                return;
            } else {
                player.getEvoItems().grantItem(id);
            }
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&eПредмет \"&a%s&e\" выдан игроку %s&e.",
                    item.getItem().getItemMeta().getDisplayName(),
                    dmsPlayer.getFullDisplayName()));
        }
    }

    @Subcommand("remove")
    @Description("забрать предмет.")
    @Usage("/items remove")
    public void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cУкажите имя игрока и айди предмета(ов)."));
            return;
        }
        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException ignored) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cАйди должен быть числом."));
            return;
        }
        EvoItem item = EvoItemsManager.getItem(id);
        if (item == null) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cТакого предмета не существует."));
            return;
        }
        GamePlayer player = PrisonEvo.getInstance().getAthenaManager().get(args[0]);
        if (player == null) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cИгрок не найден.", id));
            return;
        } else {
            player.getEvoItems().removeItem(id);
        }
        sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&eПредмет \"&a%s&e\" забран у игрока %s&e.",
                item.getItem().getItemMeta().getDisplayName(),
                GameApi.getUserManager().get(player.getHandle()).getFullDisplayName()));
    }

    @Subcommand("check")
    @Description("посмотреть вещи игрока.")
    @Usage("/items check")
    public void handleCheck(CommandSender sender, String[] args) {
        GamePlayer player = PrisonEvo.getInstance().getAthenaManager().get(args[0]);
        if (player == null) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cИгрок не найден."));
        }
        List<String> collect = player.getEvoItems().getBought().stream()
                .map(EvoItemsManager::getItem)
                .map(EvoItem::getItem)
                .map(ItemStack::getItemMeta)
                .filter(ItemMeta::hasDisplayName)
                .map(ItemMeta::getDisplayName)
                .collect(Collectors.toList());
        sender.sendMessage(String.format("Предметы %s§e:", GameApi.getUserManager().get(player.getHandle()).getFullDisplayName()));
        collect.forEach(sender::sendMessage);
    }

    @Override
    public CommandAccessResult hasAccess(UserPermission permission) {
        return permission.isHeadAdministrator() ? null : new CommandAccessResult(PermissionGroup.HEAD_ADMIN);
    }
}
