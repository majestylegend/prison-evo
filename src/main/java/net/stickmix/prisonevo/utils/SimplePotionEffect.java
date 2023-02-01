package net.stickmix.prisonevo.utils;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Константин
 */
public class SimplePotionEffect extends PotionEffect {

    public SimplePotionEffect(PotionEffectType type, int level) {
        this(type, level, -1);
    }

    public SimplePotionEffect(PotionEffectType type, int level, double durationInSeconds) {
        super(type, durationInSeconds == -1 ? 1200000 : (int) (durationInSeconds * 20), level - 1);
    }
}
