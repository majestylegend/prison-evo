package net.stickmix.prisonevo.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DamageHelper {

    public static void clearDamage(LivingEntity le, double amount) {
        if (le.getHealth() <= amount) {
            EntityDamageEvent event = new EntityDamageEvent(le, EntityDamageEvent.DamageCause.MAGIC, amount);
            Bukkit.getPluginManager().callEvent(event);
        } else {
            le.setHealth(le.getHealth() - amount);
            le.damage(0.01D);
        }
    }

    public static double getPlayerDamage(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        double damage = UtilItem.calculateItemDamage(hand);
        if (hand.getType() == Material.GOLD_SWORD) {
            damage += 3D;
        }
        return damage;
    }

    public static double getPlayerProtection(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        double damage = 0D;
        for (ItemStack stack : armor) {
            damage += getProtectionByEnchantment(stack);
        }
        return damage;
    }

    public static double getProtectionByEnchantment(ItemStack itemStack) {
        if (itemStack == null) {
            return 0D;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return 0D;
        }
        if (!meta.hasEnchant(Enchantment.PROTECTION_ENVIRONMENTAL)) {
            return 0D;
        }
        int level = meta.getEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
        if (level < 6) {
            return 0D;
        }
        return (level - 5) * 0.18D;
    }

}
