package net.stickmix.prisonevo.commands;

import net.stickmix.game.api.command.*;
import net.stickmix.game.api.util.ChatUtil;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.data.DeathMessage;
import net.stickmix.prisonevo.data.GamePlayer;
import org.bukkit.command.CommandSender;

@Command("deathmessage")
@Description("Предсмертное сообщение")
@Usage("/deathmessage")
@Aliases("dm")
public class DeathMessageCommand {

    @Subcommand("change")
    @Description("сменить предсмертное сообщение.")
    @Usage("/deathmessage change")
    public void handleChange(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cУкажите предсмертное сообщение."));
            return;
        }
        String message = String.join(" ", args).trim();
        if (message.isEmpty() || message.length() > 64) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cСообщение не может быть пустым или длинее 64 символов."));
            return;
        }
        GamePlayer player = PrisonEvo.getInstance().getAthenaManager().get(sender.getName());
        DeathMessage deathMessage = player.getDeathMessage();
        if (!deathMessage.hasFeature()) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cУ вас нет возможности менять предсмертное сообщение."));
            return;
        }
        if (deathMessage.hasCooldown()) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cНельзя менять сообщение чаще, чем раз в 15 минут."));
            return;
        }
        deathMessage.changeMessage(message);
        player.sendMessage("&aВы успешно изменили предсмертное сообщение.");
    }

    @Subcommand("clear")
    @Description("удалить предсмертное сообщение.")
    @Usage("/deathmessage clear")
    public void handleClear(CommandSender sender, String[] args) {
        GamePlayer player = PrisonEvo.getInstance().getAthenaManager().get(sender.getName());
        DeathMessage message = player.getDeathMessage();
        if (!message.hasFeature()) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cУ вас нет возможности удалять предсмертное сообщение."));
            return;
        }
        message.clear();
        player.sendMessage("&aВы успешно удалили предсмертное сообщение.");
    }
}
