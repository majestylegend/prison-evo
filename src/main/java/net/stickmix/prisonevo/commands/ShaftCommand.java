package net.stickmix.prisonevo.commands;

import net.stickmix.game.api.command.*;
import net.stickmix.game.api.util.ChatUtil;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.data.GamePlayer;
import net.stickmix.prisonevo.plot.Plot;
import net.stickmix.prisonevo.plot.PlotManager;
import net.stickmix.prisonevo.utils.Level;
import net.stickmix.prisonevo.utils.TeleportManager;
import org.bukkit.command.CommandSender;

@Command("shaft")
@Description("телепортироваться на шахту")
@Usage("/shaft")
@Aliases("mine")
public class ShaftCommand {

    @CommandHandler
    public void handle(CommandSender sender, String[] args) {
        GamePlayer gamePlayer = PrisonEvo.getInstance().getAthenaManager().get(sender.getName());
//        Bonuses bonuses = gamePlayer.getBonuses();
//        if (bonuses.isActive(Bonus.ACCESS_TO_SIX_UPPER_SHAFTS)) {
//            boost += 6;
//        } else if (bonuses.isActive(Bonus.ACCESS_TO_FOUR_UPPER_SHAFTS)) {
//            boost += 4;
//        }

        int gamePlayerLevel = gamePlayer.getLevel();
        int maxLevel = Level.getMaxLevel();
        int level = Math.min(gamePlayerLevel, maxLevel);
        if (args.length >= 1) {
            try {
                String arg = args[0];
                level = Integer.parseInt(arg);
                if (level < 0) {
                    level = Integer.parseInt(arg.substring(1));
                }
            } catch (NumberFormatException ignored) {
            }
            if (level > gamePlayerLevel) {
                level = gamePlayerLevel;
            }
            if (level > maxLevel) {
                level = maxLevel;
            }
        }
        Plot mine = PlotManager.getPlotByLevel(level);
        if (mine == null) {
            sender.sendMessage(ChatUtil.prefixed("PrisonEvo", "&cНа сервере для вас нет доступных шахт."));
            return;
        }
        TeleportManager.teleport(gamePlayer, mine.getSpawn());
    }
}
