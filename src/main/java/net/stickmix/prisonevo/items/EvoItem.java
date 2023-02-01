package net.stickmix.prisonevo.items;

import lombok.Data;
import org.bukkit.inventory.ItemStack;

@Data
public class EvoItem {

    private final int id;
    private final long price;
    private final ItemStack item;
    private final int previous, next;

}
