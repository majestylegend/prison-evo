package net.stickmix.prisonevo.commands;

import net.stickmix.game.api.command.Command;
import net.stickmix.game.api.command.CommandHandler;
import net.stickmix.game.api.command.Description;
import net.stickmix.game.api.command.Usage;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.utils.TeleportManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("spawn")
@Description("телепортироваться на спавн")
@Usage("/spawn")
public class SpawnCommand {

    @CommandHandler
    public void handle(CommandSender sender, String[] args) {
        TeleportManager.teleport((Player) sender, PrisonEvo.SPAWN);
    }
}
