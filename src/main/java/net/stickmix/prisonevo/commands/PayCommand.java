package net.stickmix.prisonevo.commands;

import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.command.Command;
import net.stickmix.game.api.command.CommandHandler;
import net.stickmix.game.api.command.Description;
import net.stickmix.game.api.command.Usage;
import net.stickmix.game.api.util.ChatUtil;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.data.GamePlayer;
import net.stickmix.prisonevo.utils.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

@Command("pay")
@Description("передать деньги другому игроку")
@Usage("/pay")
public class PayCommand {

    @CommandHandler
    public void handle(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cУкажите имя игрока и сумму."));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cИгрок не в сети."));
            return;
        }
        if (target.getName().equalsIgnoreCase(sender.getName())) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cВы не можете организовать перевод денег самому себе!"));
            return;
        }
        long sum = 0;
        try {
            sum = Long.parseLong(args[1]);
        } catch (NumberFormatException ignored) {
        }
        if (sum <= 0) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cУказана неверная сумма."));
            return;
        }
        if (sum < 10) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cВы не можете отправить сумму меньше &b10$"));
            return;
        }
        GamePlayer gamePlayer = PrisonEvo.getInstance().getAthenaManager().get(sender.getName());
        if (gamePlayer.getBalance() < sum) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cУ вас нет столько денег!"));
            return;
        }
        if (!GameApi.getFloodControl().checkAndAdd(sender.getName(), "pay", 1, 10, TimeUnit.SECONDS)) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cНельзя так часто использовать эту команду"));
            return;
        }
        gamePlayer.changeBalance(-sum);
        long com = (long) (sum * 0.1);
        long subtracted = sum - com;
        GamePlayer wrap = PrisonEvo.getInstance().getAthenaManager().get(target);
        wrap.changeBalance(subtracted);
        sender.sendMessage(ChatUtil.colorize("&aВы отправили %s &a%s. Комиссия перевода: %s (10%%).",
                wrap.asUser().getFullDisplayName(),
                NumberUtil.formatMoney(sum),
                NumberUtil.formatMoney(com)));
        wrap.sendMessage(
                "%s &aотправил вам %s (вы получите %s, т.к. комиссия перевода - %s (10%%)).",
                gamePlayer.asUser().getFullDisplayName(), NumberUtil.formatMoney(sum),
                NumberUtil.formatMoney(subtracted), NumberUtil.formatMoney(com)
        );
    }
}
