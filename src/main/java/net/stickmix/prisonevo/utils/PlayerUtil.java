package net.stickmix.prisonevo.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PlayerUtil {

    public static void dropItems(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; ++i) {
            ItemStack is = contents[i];
            if (is == null || is.getType() == Material.NETHER_STAR) {
                continue;
            }
            ItemMeta im = is.getItemMeta();
            List<String> lore = im.getLore();
            if (lore != null && lore.get(lore.size() - 1).equals(ItemUtils.SAFE_ITEM)) {
                continue;
            }
            player.getWorld().dropItem(player.getLocation(), is);
            contents[i] = null;
        }
        player.getInventory().setContents(contents);
    }
}
