package net.stickmix.prisonevo.command;

import net.stickmix.game.api.command.*;
import net.stickmix.game.api.user.permission.PermissionGroup;
import net.stickmix.game.api.util.ChatUtil;
import net.stickmix.game.api.util.Prefix;
import net.stickmix.prisonevo.MainClass;
import net.stickmix.prisonevo.data.TeleportData;
import net.villenium.os.VilleniumOS;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command(value = "spawn")
@Description("Телепортация на спавн")
@OnlyFor(Sender.PLAYER)
public class CommandSpawn {

    @CommandHandler
    public void execute(CommandSender sender, String[] args) {
        TeleportData.teleport((Player) sender, MainClass.getInstance().getSpawn());
    }
}
