package net.stickmix.prisonevo.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.var;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.phantom.entity.PhantomEntityAnimations;
import net.stickmix.game.api.phantom.entity.PhantomEntityInteraction;
import net.stickmix.game.api.phantom.entity.PhantomEquippableEntity;
import net.stickmix.prisonevo.EntityManager;
import net.stickmix.prisonevo.entity.event.PlayerDamagePhantomEntityEvent;
import net.stickmix.prisonevo.entity.logic.PhantomEntityLogic;
import net.stickmix.prisonevo.utils.DamageHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class PhantomIntelligentEntity implements PhantomEquippableEntity {

    private final PhantomEquippableEntity handle;
    @Getter
    private final double maxHealth;
    @Getter
    @Setter
    private PhantomEntityLogic logic;

    @Getter
    private double health;

    public PhantomIntelligentEntity(EntityType type, double health, Location spawnLocation) {
        this.handle = GameApi.getPhantomEntityFactory().createEquippableEntity(type);
        this.maxHealth = this.health = health;

        setLocation(spawnLocation);
        setInteraction(new PhantomEntityInteraction() {
            @Override
            public void onLeftClick(Player player) {
                if (!GameApi.getFloodControl().checkAndAdd(player.getName(), "damage", 1, 1, 450, TimeUnit.MILLISECONDS)) {
                    return;
                }
                var damage = DamageHelper.getPlayerDamage(player);
                val event = new PlayerDamagePhantomEntityEvent(player, PhantomIntelligentEntity.this, damage);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return;
                }
                damage = event.getDamage();
                damage(damage);
                if (logic != null) {
                    logic.registerDamage(player, damage);
                }
            }

            @Override
            public void onRightClick(Player player) {

            }
        });

        EntityManager.add(this);
    }

    public boolean damage(double value) {
        PhantomEntityAnimations animations = getAnimations();
        if (health <= 0D) {
            animations.playAnimationDamage();
            animations.playAnimationHit();
            return false;
        }
        if ((this.health -= value) <= 0D) {
            animations.playAnimationDeath();
            this.invalidate();
            return true;
        }
        animations.playAnimationDamage();
        animations.playAnimationHit();
        return false;
    }

    @Override
    public int getID() {
        return handle.getID();
    }

    @Override
    public World getWorld() {
        return handle.getWorld();
    }

    @Override
    public Location getLocation() {
        return handle.getLocation();
    }

    @Override
    public EntityType getType() {
        return handle.getType();
    }

    @Override
    public Collection<Player> getViewers() {
        return handle.getViewers();
    }

    @Override
    public void show(Player player) {
        handle.show(player);
    }

    @Override
    public void hide(Player player) {
        handle.hide(player);
    }

    @Override
    public boolean isVisibleFor(Player player) {
        return handle.isVisibleFor(player);
    }

    @Override
    public void spawn(boolean autoVisible) {
        handle.spawn(autoVisible);
        if (logic != null) {
            logic.onSpawn();
        }
    }

    @Override
    public void despawn() {
        handle.despawn();
        if (logic != null) {
            logic.onDespawn();
        }
    }

    @Override
    public boolean isSpawned() {
        return handle.isSpawned();
    }

    @Override
    public void invalidate() {
        handle.invalidate();
        EntityManager.remove(this);
    }

    @Override
    public void lookAt(Location location) {
        handle.lookAt(location);
    }

    @Override
    public void moveWithoutBodyRotation(double dx, double dy, double dz) {
        handle.moveWithBodyRotation(dx, dy, dz);
    }

    @Override
    public void move(double dx, double dy, double dz) {
        handle.move(dx, dy, dz);
    }

    @Override
    public void teleport(Location location) {
        handle.teleport(location);
    }

    @Override
    public PhantomEntityAnimations getAnimations() {
        return handle.getAnimations();
    }

    @Override
    public PhantomEntityInteraction getInteraction() {
        return handle.getInteraction();
    }

    @Override
    public void setInteraction(PhantomEntityInteraction interaction) {
        handle.setInteraction(interaction);
    }

    @Override
    public ItemStack getHand() {
        return handle.getHand();
    }

    @Override
    public ItemStack getHelmet() {
        return handle.getHelmet();
    }

    @Override
    public ItemStack getChestplate() {
        return handle.getChestplate();
    }

    @Override
    public ItemStack getLeggings() {
        return handle.getLeggings();
    }

    @Override
    public ItemStack getBoots() {
        return handle.getBoots();
    }

    @Override
    public void setHand(ItemStack hand) {
        handle.setHand(hand);
    }

    @Override
    public void setHelmet(ItemStack helmet) {
        handle.setHelmet(helmet);
    }

    @Override
    public void setChestplate(ItemStack chestplate) {
        handle.setChestplate(chestplate);
    }

    @Override
    public void setLeggings(ItemStack leggings) {
        handle.setLeggings(leggings);
    }

    @Override
    public void setBoots(ItemStack boots) {
        handle.setBoots(boots);
    }
}
