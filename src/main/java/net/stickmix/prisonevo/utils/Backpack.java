package net.stickmix.prisonevo.utils;

import lombok.Data;
import net.stickmix.prisonevo.data.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class Backpack {

    private final static BackpackInfo[] BACKPACKS = new BackpackInfo[]{
            new BackpackInfo(0, 0),
            new BackpackInfo(9, 50),
            new BackpackInfo(18, 75),
            new BackpackInfo(27, 125),
            new BackpackInfo(36, 250),
            new BackpackInfo(45, 500),
            new BackpackInfo(54, 1000)
    };

    public static Inventory createBackpack(GamePlayer info) {
        if (info.getBackpackLevel() == 0)
            return null;
        BackpackInfo backpackInfo = BACKPACKS[info.getBackpackLevel()];
        return Bukkit.createInventory(null, backpackInfo.getSize(), "Рюкзак (Ур. " + info.getBackpackLevel() + ")");
    }

    public static int getPrice(int id) {
        return BACKPACKS[id].getPrice();
    }

    public static int getSize(int id) {
        return BACKPACKS[id].getSize();
    }

    public static boolean isValid(int id) {
        return id < BACKPACKS.length;
    }

    @Data
    private static class BackpackInfo {

        private final int size;
        private final int price;

    }

}
