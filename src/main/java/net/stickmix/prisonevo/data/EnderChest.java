package net.stickmix.prisonevo.data;

import lombok.AllArgsConstructor;
import net.stickmix.game.api.athena.annotation.Id;
import net.stickmix.prisonevo.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class EnderChest {

    @Id
    private final String name;
    private String contents;

    public void fillEnderChest() {
        Inventory enderChest = Bukkit.getPlayerExact(name).getEnderChest();
        ItemStack[] contents = ItemUtils.stringToArray(this.contents, new ItemStack[27]);
        enderChest.setContents(contents);
    }

    public void saveAfterClose() {
        Inventory enderChest = Bukkit.getPlayerExact(name).getEnderChest();
        this.contents = ItemUtils.arrayToString(enderChest.getContents());
    }
}
