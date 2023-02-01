package net.stickmix.prisonevo.commands;

import net.stickmix.game.api.command.Command;
import net.stickmix.game.api.command.CommandHandler;
import net.stickmix.game.api.command.Description;
import net.stickmix.game.api.command.Usage;
import net.stickmix.game.api.command.access.CommandAccess;
import net.stickmix.game.api.command.access.CommandAccessResult;
import net.stickmix.game.api.user.permission.UserPermission;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.data.GamePlayer;
import net.stickmix.prisonevo.data.Perks;
import net.stickmix.prisonevo.plot.Resource;
import net.stickmix.prisonevo.utils.Modifiers;
import net.stickmix.prisonevo.utils.NumberUtil;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Command("sellall")
@Description("продать все содержимое инвентаря")
@Usage("/sell")
public class SellAllCommand implements CommandAccess {

    @CommandHandler
    public void handle(CommandSender sender, String[] args) {
        sell((Player) sender);
    }

    public static void sell(Player p) {
        ItemStack[] contents = p.getInventory().getContents();
        long money = 0;
        for (int i = 0; i < contents.length; ++i) {
            ItemStack is = contents[i];
            if (is == null)
                continue;
            Resource res = Resource.getByItemMaterial(is.getType());
            if (res == null)
                continue;
            money += res.getCost() * is.getAmount();
            contents[i] = null;
        }
        GamePlayer gamePlayer = PrisonEvo.getInstance().getAthenaManager().get(p);
        Inventory backpack = gamePlayer.getBackpack();
        if (backpack != null) {
            ItemStack[] back = backpack.getContents();
            for (ItemStack is : back) {
                if (is == null)
                    continue;
                Resource res = Resource.getByItemMaterial(is.getType());
                if (res == null)
                    continue;
                money += res.getCost() * is.getAmount();
            }
            backpack.clear();
        }
        Perks perks = gamePlayer.getPerks();
        if (perks.hasPerk(Perks.Perk.MONEY_IV))
            money *= 1.5F;
        else if (perks.hasPerk(Perks.Perk.MONEY_III))
            money *= 1.3F;
        else if (perks.hasPerk(Perks.Perk.MONEY_II))
            money *= 1.2F;
        else if (perks.hasPerk(Perks.Perk.MONEY_I))
            money *= 1.1F;
        if (money == 0) {
            gamePlayer.sendMessage("&cУ тебя нет ничего на продажу.");
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F);
            return;
        }
        p.getInventory().setContents(contents);
        gamePlayer.changeBalance(money);
        gamePlayer.sendMessage("&aТы продал добра на &b%s&a.", NumberUtil.formatMoney(money));
        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1F, 1F);
    }

    @Override
    public CommandAccessResult hasAccess(UserPermission permission) {
        return Modifiers.isSellall() || permission.isAdministrator()
                ? null
                : new CommandAccessResult("&cна сервере нет ни одного игрока группы &3&lRICH");
    }
}
