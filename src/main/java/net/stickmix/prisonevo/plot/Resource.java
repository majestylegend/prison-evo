package net.stickmix.prisonevo.plot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@Getter
@AllArgsConstructor
public enum Resource {

    LEAVE(Material.LEAVES, Material.LEAVES, 1),
    LEAVE2(Material.LEAVES_2, Material.LEAVES_2, 4),
    WOOD(Material.WOOD_STAIRS, Material.WOOD_STAIRS, 30),
    WOOD2(Material.WOOD, Material.SAPLING, 40),
    COBBLESTONE(Material.COBBLESTONE, Material.COBBLESTONE, 120),
    MOSSY(Material.MOSSY_COBBLESTONE, Material.MOSSY_COBBLESTONE, 270),
    STONE(Material.STONE, Material.STONE, 530),
    COAL(Material.COAL_ORE, Material.COAL, 780),
    IRON(Material.IRON_ORE, Material.IRON_INGOT, 980),
    LAPIS(Material.LAPIS_ORE, Material.LAPIS_ORE, 1300),
    REDSTONE(Material.REDSTONE_ORE, Material.REDSTONE, 2000),
    REDSTONE2(Material.GLOWING_REDSTONE_ORE, Material.REDSTONE, 2000),
    GOLD(Material.GOLD_ORE, Material.GOLD_INGOT, 3000),
    DIAMOND(Material.DIAMOND_ORE, Material.DIAMOND, 7500),
    GEMS(Material.REDSTONE_BLOCK, Material.REDSTONE_BLOCK, 10000),
    EMERALD(Material.EMERALD_ORE, Material.EMERALD, 15000),
    APALITE(Material.QUARTZ_BLOCK, Material.QUARTZ_BLOCK, 22500),
    OBSIDIAN(Material.OBSIDIAN, Material.OBSIDIAN, 80000),
    NETHER(Material.NETHERRACK, Material.NETHERRACK, 35000),
    NETHERBRICK(Material.NETHER_BRICK, Material.NETHER_BRICK_ITEM, 75000),
    QUARTZ(Material.QUARTZ_ORE, Material.QUARTZ, 150000),
    REDBRICK(Material.RED_NETHER_BRICK, Material.RED_NETHER_BRICK, 300000),
    ENDSTONE(Material.ENDER_STONE, Material.ENDER_STONE, 500000),
    PRISMARINE(Material.PRISMARINE, Material.PRISMARINE, 750000),
    RUNIC_BLOCK(Material.PURPUR_BLOCK, Material.PURPUR_BLOCK, 1000000),
    DIRT(Material.DIRT, Material.DIRT, 1000000),
    GRASS(Material.GRASS, Material.GRASS, 1500000),
    MYCELIUM(Material.MYCEL, Material.MYCEL, 2500000),
    GRAVEL(Material.GRAVEL, Material.GRAVEL, 3500000),
    SAND(Material.SAND, Material.SAND, 5000000),
    CLAY(Material.CLAY, Material.CLAY_BALL, 6500000),
    SNOW(Material.SNOW_BLOCK, Material.SNOW_BLOCK, 8000000),
    SOULSAND(Material.SOUL_SAND, Material.SOUL_SAND, 13000000),
    POWDER(Material.CONCRETE_POWDER, Material.CONCRETE_POWDER, 18500000),
    STAINED_CLAY(Material.STAINED_CLAY, Material.STAINED_CLAY, 25000000),
    CONCRETE(Material.CONCRETE, Material.CONCRETE, 37500000),
    LAPISBLOCK(Material.LAPIS_BLOCK, Material.LAPIS_BLOCK, 45000000),
    GOLDBLOCK(Material.GOLD_BLOCK, Material.GOLD_BLOCK, 60000000),
    DIAMONDBLOCK(Material.DIAMOND_BLOCK, Material.DIAMOND_BLOCK, 80000000),
    LOG(Material.LOG, Material.LOG, 950000000),
    LOG2(Material.LOG_2, Material.LOG_2, 110000000);

    public static Resource[] VALUES = values();

    private final Material block, item;
    private final long cost;

    public static Resource getByBlockMaterial(Material block) {
        for (Resource res : VALUES) {
            if (res.getBlock() == block) {
                return res;
            }
        }
        return null;
    }

    public static Resource getByItemMaterial(Material item) {
        for (Resource res : VALUES) {
            if (res.getItem() == item) {
                return res;
            }
        }
        return null;
    }

}
