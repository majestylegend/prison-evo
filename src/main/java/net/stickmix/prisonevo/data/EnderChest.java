package net.stickmix.prisonevo.data;

import lombok.AllArgsConstructor;
import net.stickmix.game.api.athena.annotation.Id;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class EnderChest {

    @Id
    private final String name;
    private ItemStack[] contents;

    public void fillEnderChest() {
        Inventory enderChest = Bukkit.getPlayerExact(name).getEnderChest();
        enderChest.setContents(contents);
    }

    public void saveAfterClose() {
        Inventory enderChest = Bukkit.getPlayerExact(name).getEnderChest();
        contents = enderChest.getContents();

    }
}
