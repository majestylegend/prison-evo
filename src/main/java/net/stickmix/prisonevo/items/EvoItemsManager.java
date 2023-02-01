package net.stickmix.prisonevo.items;

import com.google.common.collect.Lists;
import net.stickmix.game.api.item.GameItemStack;
import net.stickmix.game.api.item.GameItemStackMetaBuilder;
import net.stickmix.prisonevo.utils.ItemBuilder;
import net.stickmix.prisonevo.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EvoItemsManager {

    private static final Map<Integer, EvoItem> ITEMS = new HashMap<>();
    public static final List<Integer> ZEROS = new ArrayList<>();
    public static final List<String> DUPE = new ArrayList<>();

    public static void initialize() {
        load(1, 0, 2, 5000, new GameItemStack(Material.WOOD_SWORD, "&7Деревянный меч"));
        load(2, 1, 3, 50000, new GameItemStack(Material.WOOD_SWORD, "&7Деревянный меч &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.DAMAGE_ALL, 1).build()));
        load(3, 2, 4, 500000, new GameItemStack(Material.STONE_SWORD, "&aКаменный меч"));
        load(4, 3, 5, 5000000, new GameItemStack(Material.STONE_SWORD, "&aКаменный меч &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.DAMAGE_ALL, 1).build()));
        load(5, 4, 6, 50000000, new GameItemStack(Material.IRON_SWORD, "&9Железный меч"));
        load(6, 5, 7, 250000000, new GameItemStack(Material.IRON_SWORD, "&9Железный меч &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.DAMAGE_ALL, 1).build()));
        load(7, 6, 8, 500000000, new GameItemStack(Material.DIAMOND_SWORD, "&5Алмазный меч"));
        load(8, 7, 9, 2500000000L, new GameItemStack(Material.DIAMOND_SWORD, "&5Алмазный меч &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.DAMAGE_ALL, 1).build()));
        load(9, 8, 10, 10000000000L, new GameItemStack(Material.DIAMOND_SWORD, "&5Алмазный меч &c&l++", new GameItemStackMetaBuilder().enchantment(Enchantment.DAMAGE_ALL, 2).build()));
        load(10, 9, 11, 40000000000L, new GameItemStack(Material.DIAMOND_SWORD, "&5Алмазный меч &c&l+++", new GameItemStackMetaBuilder().enchantment(Enchantment.DAMAGE_ALL, 3).build()));
        load(11, 10, 12, 150000000000L, new GameItemStack(Material.DIAMOND_SWORD, "&5Алмазный меч &c&l++++", new GameItemStackMetaBuilder().enchantment(Enchantment.DAMAGE_ALL, 4).build()));
        load(12, 11, 13, 300000000000L, new GameItemStack(Material.DIAMOND_SWORD, "&5Клинок тьмы &c&l+++++", new GameItemStackMetaBuilder().enchantment(Enchantment.DAMAGE_ALL, 5).build()));
        load(13, 12, 14, 500000000000L, new GameItemStack(Material.DIAMOND_SWORD, "&5Клинок тьмы &4&l6+", new GameItemStackMetaBuilder().enchantment(Enchantment.DAMAGE_ALL, 6).build()));
        load(14, 13, 15, 750000000000L, new GameItemStack(Material.DIAMOND_SWORD, "&5Клинок тьмы &4&l7+", new GameItemStackMetaBuilder().enchantment(Enchantment.DAMAGE_ALL, 7).build()));
        load(15, 14, 16, 1000000000000L, new GameItemStack(Material.DIAMOND_SWORD, "&5Клинок тьмы &4&l8+", new GameItemStackMetaBuilder().enchantment(Enchantment.DAMAGE_ALL, 8).build()));
        load(16, 15, 17, 2500000000000L, new GameItemStack(Material.DIAMOND_SWORD, "&5Клинок тьмы &4&l9+", new GameItemStackMetaBuilder().enchantment(Enchantment.DAMAGE_ALL, 9).build()));
        load(17, 16, 146, 5_000_000_000_000L, new GameItemStack(Material.DIAMOND_SWORD, "&5Кровавый меч &4&l10+", new GameItemStackMetaBuilder().enchantment(Enchantment.DAMAGE_ALL, 10).build()));

        load(18, 0, 22, 1000, new GameItemStack(Material.LEATHER_HELMET, "&7Кожаный шлем"));
        load(19, 0, 23, 1000, new GameItemStack(Material.LEATHER_CHESTPLATE, "&7Кожаный нагрудник"));
        load(20, 0, 24, 1000, new GameItemStack(Material.LEATHER_LEGGINGS, "&7Кожаные штаны"));
        load(21, 0, 25, 1000, new GameItemStack(Material.LEATHER_BOOTS, "&7Кожаные ботинки"));
        load(22, 18, 26, 10000, new GameItemStack(Material.LEATHER_HELMET, "&7Кожаный шлем &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build()));
        load(23, 19, 27, 10000, new GameItemStack(Material.LEATHER_CHESTPLATE, "&7Кожаный нагрудник &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build()));
        load(24, 20, 28, 10000, new GameItemStack(Material.LEATHER_LEGGINGS, "&7Кожаные штаны &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build()));
        load(25, 21, 29, 10000, new GameItemStack(Material.LEATHER_BOOTS, "&7Кожаные ботинки &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build()));
        load(26, 22, 30, 100000, new GameItemStack(Material.GOLD_HELMET, "&aЗолотой шлем"));
        load(27, 23, 31, 100000, new GameItemStack(Material.GOLD_CHESTPLATE, "&aЗолотой нагрудник"));
        load(28, 24, 32, 100000, new GameItemStack(Material.GOLD_LEGGINGS, "&aЗолотые штаны"));
        load(29, 25, 33, 100000, new GameItemStack(Material.GOLD_BOOTS, "&aЗолотые ботинки"));
        load(30, 26, 34, 1000000, new GameItemStack(Material.GOLD_HELMET, "&aЗолотой шлем &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build()));
        load(31, 27, 35, 1000000, new GameItemStack(Material.GOLD_CHESTPLATE, "&aЗолотой нагрудник &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build()));
        load(32, 28, 36, 1000000, new GameItemStack(Material.GOLD_LEGGINGS, "&aЗолотые штаны &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build()));
        load(33, 29, 37, 1000000, new GameItemStack(Material.GOLD_BOOTS, "&aЗолотые ботинки &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build()));
        load(34, 30, 38, 10000000, new GameItemStack(Material.IRON_HELMET, "&9Железный шлем"));
        load(35, 31, 39, 10000000, new GameItemStack(Material.IRON_CHESTPLATE, "&9Железный нагрудник"));
        load(36, 32, 40, 10000000, new GameItemStack(Material.IRON_LEGGINGS, "&9Железные штаны"));
        load(37, 33, 41, 10000000, new GameItemStack(Material.IRON_BOOTS, "&9Железные ботинки"));
        load(38, 34, 42, 50000000, new GameItemStack(Material.IRON_HELMET, "&9Железный шлем &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build()));
        load(39, 35, 43, 50000000, new GameItemStack(Material.IRON_CHESTPLATE, "&9Железный нагрудник &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build()));
        load(40, 36, 44, 50000000, new GameItemStack(Material.IRON_LEGGINGS, "&9Железные штаны &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build()));
        load(41, 37, 45, 50000000, new GameItemStack(Material.IRON_BOOTS, "&9Железные ботинки &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build()));
        load(42, 38, 46, 100000000, new GameItemStack(Material.DIAMOND_HELMET, "&5Алмазный шлем"));
        load(43, 39, 47, 100000000, new GameItemStack(Material.DIAMOND_CHESTPLATE, "&5Алмазный нагрудник"));
        load(44, 40, 48, 100000000, new GameItemStack(Material.DIAMOND_LEGGINGS, "&5Алмазные штаны"));
        load(45, 41, 49, 100000000, new GameItemStack(Material.DIAMOND_BOOTS, "&5Алмазные ботинки"));
        load(46, 42, 50, 500000000, new GameItemStack(Material.DIAMOND_HELMET, "&5Алмазный шлем &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build()));
        load(47, 43, 51, 500000000, new GameItemStack(Material.DIAMOND_CHESTPLATE, "&5Алмазный нагрудник &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build()));
        load(48, 44, 52, 500000000, new GameItemStack(Material.DIAMOND_LEGGINGS, "&5Алмазные штаны &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build()));
        load(49, 45, 53, 500000000, new GameItemStack(Material.DIAMOND_BOOTS, "&5Алмазные ботинки &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build()));
        load(50, 46, 54, 2000000000, new GameItemStack(Material.DIAMOND_HELMET, "&5Алмазный шлем &c&l++", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build()));
        load(51, 47, 55, 2000000000, new GameItemStack(Material.DIAMOND_CHESTPLATE, "&5Алмазный нагрудник &c&l++", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build()));
        load(52, 48, 56, 2000000000, new GameItemStack(Material.DIAMOND_LEGGINGS, "&5Алмазные штаны &c&l++", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build()));
        load(53, 49, 57, 2000000000, new GameItemStack(Material.DIAMOND_BOOTS, "&5Алмазные ботинки &c&l++", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build()));
        load(54, 50, 58, 8000000000L, new GameItemStack(Material.DIAMOND_HELMET, "&5Алмазный шлем &c&l+++", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3).build()));
        load(55, 51, 59, 8000000000L, new GameItemStack(Material.DIAMOND_CHESTPLATE, "&5Алмазный нагрудник &c&l+++", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3).build()));
        load(56, 52, 60, 8000000000L, new GameItemStack(Material.DIAMOND_LEGGINGS, "&5Алмазные штаны &c&l+++", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3).build()));
        load(57, 53, 61, 8000000000L, new GameItemStack(Material.DIAMOND_BOOTS, "&5Алмазные ботинки &c&l+++", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3).build()));
        load(58, 54, 62, 30000000000L, new GameItemStack(Material.DIAMOND_HELMET, "&5Алмазный шлем &c&l++++", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4).build()));
        load(59, 55, 63, 30000000000L, new GameItemStack(Material.DIAMOND_CHESTPLATE, "&5Алмазный нагрудник &c&l++++", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4).build()));
        load(60, 56, 64, 30000000000L, new GameItemStack(Material.DIAMOND_LEGGINGS, "&5Алмазные штаны &c&l++++", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4).build()));
        load(61, 57, 65, 30000000000L, new GameItemStack(Material.DIAMOND_BOOTS, "&5Алмазные ботинки &c&l++++", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4).build()));
        load(62, 58, 66, 150000000000L, new GameItemStack(Material.DIAMOND_HELMET, "&5Алмазный шлем &4&l5+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5).build()));
        load(63, 59, 67, 150000000000L, new GameItemStack(Material.DIAMOND_CHESTPLATE, "&5Алмазный нагрудник &4&l5+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5).build()));
        load(64, 60, 68, 150000000000L, new GameItemStack(Material.DIAMOND_LEGGINGS, "&5Алмазные штаны &4&l5+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5).build()));
        load(65, 61, 69, 150000000000L, new GameItemStack(Material.DIAMOND_BOOTS, "&5Алмазные ботинки &4&l5+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5).build()));
        load(66, 62, 70, 200000000000L, new GameItemStack(Material.DIAMOND_HELMET, "&5Алмазный шлем &4&l6+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 6).build()));
        load(67, 63, 71, 200000000000L, new GameItemStack(Material.DIAMOND_CHESTPLATE, "&5Алмазный нагрудник &4&l6+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 6).build()));
        load(68, 64, 72, 200000000000L, new GameItemStack(Material.DIAMOND_LEGGINGS, "&5Алмазные штаны &4&l6+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 6).build()));
        load(69, 65, 73, 200000000000L, new GameItemStack(Material.DIAMOND_BOOTS, "&5Алмазные ботинки &4&l6+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 6).build()));
        load(70, 66, 74, 500000000000L, new GameItemStack(Material.DIAMOND_HELMET, "&5Алмазный шлем &4&l7+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 7).build()));
        load(71, 67, 75, 500000000000L, new GameItemStack(Material.DIAMOND_CHESTPLATE, "&5Алмазный нагрудник &4&l7+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 7).build()));
        load(72, 68, 76, 500000000000L, new GameItemStack(Material.DIAMOND_LEGGINGS, "&5Алмазные штаны &4&l7+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 7).build()));
        load(73, 69, 77, 500000000000L, new GameItemStack(Material.DIAMOND_BOOTS, "&5Алмазные ботинки &4&l7+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 7).build()));
        load(74, 70, 138, 1000000000000L, new GameItemStack(Material.DIAMOND_HELMET, "&5Алмазный шлем &4&l8+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 8).build()));
        load(75, 71, 139, 1000000000000L, new GameItemStack(Material.DIAMOND_CHESTPLATE, "&5Алмазный нагрудник &4&l8+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 8).build()));
        load(76, 72, 140, 1000000000000L, new GameItemStack(Material.DIAMOND_LEGGINGS, "&5Алмазные штаны &4&l8+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 8).build()));
        load(77, 73, 141, 1000000000000L, new GameItemStack(Material.DIAMOND_BOOTS, "&5Алмазные ботинки &4&l8+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 8).build()));

        load(78, 0, 79, 250, new GameItemStack(Material.SHEARS, "&7Ножницы"));
        load(79, 78, 80, 500, new GameItemStack(Material.SHEARS, "&7Ножницы &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 1).build()));
        load(80, 79, 0, 750, new GameItemStack(Material.SHEARS, "&7Ножницы &c&l++", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 2).build()));

        load(81, 0, 82, 1500, new GameItemStack(Material.WOOD_AXE, "&7Деревянный топор"));
        load(82, 81, 83, 2500, new GameItemStack(Material.STONE_AXE, "&aКаменный топор"));
        load(83, 82, 84, 5000, new GameItemStack(Material.STONE_AXE, "&aКаменный топор &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 1).build()));
        load(84, 83, 151, 10000, new GameItemStack(Material.STONE_AXE, "&aКаменный топор &c&l++", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 2).build()));

        load(85, 0, 86, 40000, new GameItemStack(Material.WOOD_PICKAXE, "&7Деревянная кирка"));
        load(86, 85, 87, 50000, new GameItemStack(Material.WOOD_PICKAXE, "&7Деревянная кирка &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 1).build()));
        load(87, 86, 88, 60000, new GameItemStack(Material.WOOD_PICKAXE, "&7Деревянная кирка &c&l++", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 2).build()));
        load(88, 87, 89, 75000, new GameItemStack(Material.WOOD_PICKAXE, "&7Деревянная кирка &c&l+++", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 3).build()));
        load(89, 88, 90, 100000, new GameItemStack(Material.STONE_PICKAXE, "&aКаменная кирка"));
        load(90, 89, 91, 125000, new GameItemStack(Material.STONE_PICKAXE, "&aКаменная кирка &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 1).build()));
        load(91, 90, 92, 250000, new GameItemStack(Material.STONE_PICKAXE, "&aКаменная кирка &c&l++", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 2).build()));
        load(92, 91, 93, 500000, new GameItemStack(Material.IRON_PICKAXE, "&9Железная кирка"));
        load(93, 92, 94, 750000, new GameItemStack(Material.IRON_PICKAXE, "&9Железная кирка &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 1).build()));
        load(94, 93, 95, 1000000, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка"));
        load(95, 94, 96, 2000000, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 1).build()));
        load(96, 95, 97, 3000000, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &c&l++", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 2).build()));
        load(97, 96, 98, 5000000, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &c&l+++", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 3).build()));
        load(98, 97, 99, 7000000, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &c&l++++", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 4).build()));
        load(99, 98, 100, 10000000, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &c&l+++++", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 5).build()));
        load(100, 99, 101, 100000000, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l6+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 6).build()));
        load(101, 100, 102, 1000000000, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l7+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 7).build()));
        load(102, 101, 103, 10000000000L, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l8+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 8).build()));
        load(103, 102, 104, 25000000000L, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l9+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 9).build()));
        load(104, 103, 105, 50000000000L, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l10+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 10).build()));
        load(105, 104, 106, 75000000000L, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l11+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 11).build()));
        load(106, 105, 107, 100000000000L, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l12+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 12).build()));
        load(107, 106, 108, 125000000000L, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l13+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 13).build()));
        load(108, 107, 109, 150000000000L, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l14+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 14).build()));
        load(109, 108, 110, 175000000000L, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l15+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 15).build()));
        load(110, 109, 111, 200000000000L, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l16+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 16).build()));
        load(111, 110, 112, 225000000000L, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l17+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 17).build()));
        load(112, 111, 113, 250000000000L, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l18+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 18).build()));
        load(113, 112, 114, 275000000000L, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l19+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 19).build()));
        load(114, 113, 115, 300000000000L, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l20+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 20).build()));
        load(115, 114, 116, 400000000000L, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l21+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 21).build()));
        load(116, 115, 117, 500000000000L, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l22+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 22).build()));
        load(117, 116, 118, 600000000000L, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l23+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 23).build()));
        load(118, 117, 119, 750000000000L, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l24+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 24).build()));
        load(119, 118, 0, 1_000_000_000_000L, new GameItemStack(Material.DIAMOND_PICKAXE, "&5Алмазная кирка &4&l25+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 25).build()));

        load(120, 0, 121, 25000000000L, new GameItemStack(Material.IRON_SPADE, "&9Железная лопата"));
        load(121, 120, 122, 50000000000L, new GameItemStack(Material.DIAMOND_SPADE, "&5Алмазная лопата"));
        load(122, 121, 123, 75000000000L, new GameItemStack(Material.DIAMOND_SPADE, "&5Алмазная лопата &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 1).build()));
        load(123, 122, 124, 100000000000L, new GameItemStack(Material.DIAMOND_SPADE, "&5Алмазная лопата &c&l++", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 2).build()));
        load(124, 123, 125, 150000000000L, new GameItemStack(Material.DIAMOND_SPADE, "&5Алмазная лопата &c&l+++", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 3).build()));
        load(125, 124, 126, 200000000000L, new GameItemStack(Material.DIAMOND_SPADE, "&5Алмазная лопата &c&l++++", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 4).build()));
        load(126, 125, 127, 250000000000L, new GameItemStack(Material.DIAMOND_SPADE, "&5Алмазная лопата &c&l+++++", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 5).build()));
        load(127, 126, 128, 300000000000L, new GameItemStack(Material.DIAMOND_SPADE, "&5Алмазная лопата &4&l6+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 6).build()));
        load(128, 127, 129, 400000000000L, new GameItemStack(Material.DIAMOND_SPADE, "&5Алмазная лопата &4&l7+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 7).build()));
        load(129, 128, 130, 500000000000L, new GameItemStack(Material.DIAMOND_SPADE, "&5Алмазная лопата &4&l8+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 8).build()));
        load(130, 129, 131, 750000000000L, new GameItemStack(Material.DIAMOND_SPADE, "&5Алмазная лопата &4&l9+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 9).build()));
        load(131, 130, 132, 1_000_000_000_000L, new GameItemStack(Material.DIAMOND_SPADE, "&5Алмазная лопата &4&l10+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 10).build()));
        load(132, 131, 133, 2_500_000_000_000L, new GameItemStack(Material.DIAMOND_SPADE, "&5Алмазная лопата &4&l11+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 11).build()));
        load(133, 132, 134, 4_000_000_000_000L, new GameItemStack(Material.DIAMOND_SPADE, "&5Алмазная лопата &4&l12+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 12).build()));
        load(134, 133, 135, 6_000_000_000_000L, new GameItemStack(Material.DIAMOND_SPADE, "&5Алмазная лопата &4&l13+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 13).build()));
        load(135, 134, 136, 8_000_000_000_000L, new GameItemStack(Material.DIAMOND_SPADE, "&5Алмазная лопата &4&l14+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 14).build()));
        load(136, 135, 0, 10_000_000_000_000L, new GameItemStack(Material.DIAMOND_SPADE, "&5Алмазная лопата &4&l15+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 15).build()));

        load(137, 0, 0, 0L, new GameItemStack(Material.STICK, "&6&lПосох героического призыва", Lists.newArrayList(
                "&7&oПри клике по блоку призыва босса",
                "&7&oпризывает его героическую, то есть",
                "&7&oзначительно более усиленную его версию."
        )));

        load(138, 74, 142, 1_500_000_000_000L, new GameItemStack(Material.DIAMOND_HELMET, "&5Алмазный шлем &4&l9+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 9).build()));
        load(139, 75, 143, 1_500_000_000_000L, new GameItemStack(Material.DIAMOND_CHESTPLATE, "&5Алмазный нагрудник &4&l9+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 9).build()));
        load(140, 76, 144, 1_500_000_000_000L, new GameItemStack(Material.DIAMOND_LEGGINGS, "&5Алмазные штаны &4&l9+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 9).build()));
        load(141, 77, 145, 1_500_000_000_000L, new GameItemStack(Material.DIAMOND_BOOTS, "&5Алмазные ботинки &4&l9+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 9).build()));
        load(142, 138, 0, 3_000_000_000_000L, new GameItemStack(Material.DIAMOND_HELMET, "&5Алмазный шлем &4&l10+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 10).build()));
        load(143, 139, 0, 3_000_000_000_000L, new GameItemStack(Material.DIAMOND_CHESTPLATE, "&5Алмазный нагрудник &4&l10+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 10).build()));
        load(144, 140, 0, 3_000_000_000_000L, new GameItemStack(Material.DIAMOND_LEGGINGS, "&5Алмазные штаны &4&l10+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 10).build()));
        load(145, 141, 0, 3_000_000_000_000L, new GameItemStack(Material.DIAMOND_BOOTS, "&5Алмазные ботинки &4&l10+", new GameItemStackMetaBuilder().enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 10).build()));
        load(146, 17, 147, 7_500_000_000_000L, new GameItemStack(Material.DIAMOND_SWORD, "&5Кровавый меч &4&l11+", new GameItemStackMetaBuilder().enchantment(Enchantment.DAMAGE_ALL, 11).build()));
        load(147, 146, 148, 10_000_000_000_000L, new GameItemStack(Material.DIAMOND_SWORD, "&5Кровавый меч &4&l12+", new GameItemStackMetaBuilder().enchantment(Enchantment.DAMAGE_ALL, 12).build()));
        load(148, 147, 149, 15_000_000_000_000L, new GameItemStack(Material.DIAMOND_SWORD, "&5Кровавый меч &4&l13+", new GameItemStackMetaBuilder().enchantment(Enchantment.DAMAGE_ALL, 13).build()));
        load(149, 148, 150, 20_000_000_000_000L, new GameItemStack(Material.DIAMOND_SWORD, "&5Кровавый меч &4&l14+", new GameItemStackMetaBuilder().enchantment(Enchantment.DAMAGE_ALL, 14).build()));
        load(150, 149, 0, 32_000_000_000_000L, new GameItemStack(Material.GOLD_SWORD, "&6&lИспепелитель", Lists.newArrayList(
                        "&7Сила света I",
                        "",
                        "&7Со временем воитель и его оружие стали единым целым. ",
                        "&7Имя \"&6&lИспепелитель&7\" стало легендой.",
                        "&7Теперь его носит не только клинок, но и его владелец. "
                ), new GameItemStackMetaBuilder()
                        .enchantment(Enchantment.DAMAGE_ALL, 24)
                        .enchantment(Enchantment.SWEEPING_EDGE, 1)
                        .flag(ItemFlag.HIDE_ENCHANTS)
                        .build())
        );
        load(151, 84, 152, 50_000_000_000L, new GameItemStack(Material.IRON_AXE, "&9Железный топор"));
        load(152, 151, 153, 90_000_000_000L, new GameItemStack(Material.DIAMOND_AXE, "&5Алмазный топор"));
        load(153, 152, 154, 200_000_000_000L, new GameItemStack(Material.DIAMOND_AXE, "&5Алмазный топор &c&l+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 1).build()));
        load(154, 153, 155, 350_000_000_000L, new GameItemStack(Material.DIAMOND_AXE, "&5Алмазный топор &c&l++", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 2).build()));
        load(155, 154, 156, 550_000_000_000L, new GameItemStack(Material.DIAMOND_AXE, "&5Алмазный топор &c&l+++", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 3).build()));
        load(156, 155, 157, 800_000_000_000L, new GameItemStack(Material.DIAMOND_AXE, "&5Алмазный топор &c&l++++", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 4).build()));
        load(157, 156, 158, 2_000_000_000_000L, new GameItemStack(Material.DIAMOND_AXE, "&5Алмазный топор &c&l5+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 5).build()));
        load(158, 157, 159, 6_000_000_000_000L, new GameItemStack(Material.DIAMOND_AXE, "&5Алмазный топор &c&l6+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 6).build()));
        load(159, 158, 160, 11_000_000_000_000L, new GameItemStack(Material.DIAMOND_AXE, "&5Алмазный топор &c&l7+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 7).build()));
        load(160, 159, 161, 17_000_000_000_000L, new GameItemStack(Material.DIAMOND_AXE, "&5Алмазный топор &c&l8+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 8).build()));
        load(161, 160, 162, 25_000_000_000_000L, new GameItemStack(Material.DIAMOND_AXE, "&5Алмазный топор &c&l9+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 9).build()));
        load(162, 161, 0, 32_000_000_000_000L, new GameItemStack(Material.DIAMOND_AXE, "&5Алмазный топор &c&l10+", new GameItemStackMetaBuilder().enchantment(Enchantment.DIG_SPEED, 10).build()));
    }

    public static EvoItem getItem(int id) {
        return ITEMS.get(id);
    }

    public static Map<Integer, EvoItem> getItems() {
        return Collections.unmodifiableMap(ITEMS);
    }

    private static void load(int id, int previous, int next, long price, ItemStack is) {
        ItemStack build = ItemBuilder.fromItem(is)
                .withItemMeta()
                .addBlankLore()
                .addLore(ItemUtils.SAFE_ITEM)
                .addItemFlags(
                        ItemFlag.HIDE_ATTRIBUTES,
                        ItemFlag.HIDE_DESTROYS,
                        ItemFlag.HIDE_PLACED_ON,
                        ItemFlag.HIDE_POTION_EFFECTS,
                        ItemFlag.HIDE_UNBREAKABLE
                )
                .setUnbreakable(true)
                .and().build();
        if (previous == 0 && price != 0L) {
            ZEROS.add(id);
        }
        ITEMS.put(id, new EvoItem(id, price, build, previous, next));
    }

}
