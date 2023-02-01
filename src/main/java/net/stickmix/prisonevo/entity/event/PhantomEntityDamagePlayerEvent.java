package net.stickmix.prisonevo.entity.event;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_12_R1.DamageSource;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.stickmix.prisonevo.entity.PhantomIntelligentEntity;
import net.stickmix.prisonevo.utils.DamageHelper;
import net.stickmix.prisonevo.utils.UtilReflect;
import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class PhantomEntityDamagePlayerEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Setter
    @Getter
    private boolean cancelled = false;
    @Getter
    private final PhantomIntelligentEntity damager;
    @Getter
    private final Player victim;
    @Getter
    @Setter
    private double damage;
    @Getter
    private Map<EntityDamageEvent.DamageModifier, Double> modifiers;
    private Map<EntityDamageEvent.DamageModifier, Function<Double, Double>> modifierFunctions;

    public PhantomEntityDamagePlayerEvent(PhantomIntelligentEntity damager, Player target, double damage) {
        this.damager = damager;
        this.victim = target;
        this.damage = damage;
    }

    public double getFinalDamage() {
        if (this.modifiers == null) {
            this.setupModifiers();
        }

        double damage = 0.0D;
        EntityDamageEvent.DamageModifier[] var3 = EntityDamageEvent.DamageModifier.values();

        for (EntityDamageEvent.DamageModifier modifier : var3) {
            damage += this.getDamage(modifier);
        }
        damage -= DamageHelper.getPlayerProtection(victim);

        return Math.max(0.1D, damage);
    }

    public void setDamage(EntityDamageEvent.DamageModifier modifier, double damage) {
        Validate.notNull(modifier, "Cannot have null DamageModifier");
        if (this.modifiers == null) {
            this.setupModifiers();
        }

        if (damage == 0.0D) {
            this.modifiers.remove(modifier);
        } else {
            this.modifiers.put(modifier, damage);
        }

    }

    public double getDamage(EntityDamageEvent.DamageModifier modifier) {
        Validate.notNull(modifier, "Cannot have null DamageModifier");
        return this.modifiers.getOrDefault(modifier, 0.0D);
    }

    private void setupModifiers() {
        boolean human = this.victim.getType() == EntityType.PLAYER;
        float f = (float) this.damage;
        DamageSource damagesource = DamageSource.CACTUS;
        Function<Double, Double> blockingF = (dam) ->
                human && this.victim.isBlocking() && dam > 0.0D ? -(dam - (1.0D + dam) * 0.5D) : 0.0D;
        float blockingModifier = blockingF.apply((double) f).floatValue();
        f += blockingModifier;
        EntityPlayer el = ((CraftPlayer) this.victim).getHandle();
        Function<Double, Double> armorF = (dam) -> {
            try {
                double armorModifier = (double) (Float) UtilReflect.getClass(EntityLiving.class).invoke(el, "applyArmorModifier", new Object[]{damagesource, dam.floatValue()});
                return armorModifier - dam;
            } catch (Throwable var5) {
                return 0.0D;
            }
        };
        float armorModifier = armorF.apply((double) f).floatValue();
        f += armorModifier;
        Function<Double, Double> resistanceF = (dam) -> {
            if (victim.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
                int i = (victim.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
                int j = 25 - i;
                float f1 = dam.floatValue() * (float) j;
                return -(dam - (double) (f1 / 25.0F));
            } else {
                return -0.0D;
            }
        };
        float resistanceModifier = resistanceF.apply((double) f).floatValue();
        f += resistanceModifier;
        Function<Double, Double> magicF = (dam) -> {
            try {
                double magicModifier = (double) (Float) UtilReflect.getClass(EntityLiving.class).invoke(el, "applyMagicModifier", new Object[]{damagesource, dam.floatValue()});
                return magicModifier - dam;
            } catch (Throwable var5) {
                return 0.0D;
            }
        };
        float magicModifier = magicF.apply((double) f).floatValue();
        f += magicModifier;
        Function<Double, Double> absorptionF = (dam) ->
                -Math.max(dam - Math.max(dam - (double) el.getAbsorptionHearts(), 0.0D), 0.0D);
        float absorptionModifier = absorptionF.apply((double) f).floatValue();
        this.setupModifierMaps(this.damage, blockingModifier, armorModifier, resistanceModifier, magicModifier, absorptionModifier, blockingF, armorF, resistanceF, magicF, absorptionF);
    }

    private void setupModifierMaps(double rawDamage, double blocking, double armor, double resistance, double magic, double absorption, Function<Double, Double> blockingF, Function<Double, Double> armorF, Function<Double, Double> resistanceF, Function<Double, Double> magicF, Function<Double, Double> absorptionF) {
        this.modifiers = new EnumMap<>(EntityDamageEvent.DamageModifier.class);
        this.modifierFunctions = new EnumMap<>(EntityDamageEvent.DamageModifier.class);
        this.mapModifier(EntityDamageEvent.DamageModifier.BASE, rawDamage, (damage) -> 0.0D);
        if (this.victim.getType() == EntityType.PLAYER) {
            this.mapModifier(EntityDamageEvent.DamageModifier.BLOCKING, blocking, blockingF);
        }

        this.mapModifier(EntityDamageEvent.DamageModifier.ARMOR, armor, armorF);
        this.mapModifier(EntityDamageEvent.DamageModifier.RESISTANCE, resistance, resistanceF);
        this.mapModifier(EntityDamageEvent.DamageModifier.MAGIC, magic, magicF);
        this.mapModifier(EntityDamageEvent.DamageModifier.ABSORPTION, absorption, absorptionF);
    }

    private void mapModifier(EntityDamageEvent.DamageModifier modifier, double value, Function<Double, Double> mapper) {
        this.modifiers.put(modifier, value);
        this.modifierFunctions.put(modifier, mapper);
    }

    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
