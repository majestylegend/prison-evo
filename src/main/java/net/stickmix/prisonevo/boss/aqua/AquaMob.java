package net.stickmix.prisonevo.boss.aqua;

import lombok.Getter;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.bar.BossBar;
import net.stickmix.game.api.phantom.entity.PhantomHologram;
import net.stickmix.prisonevo.boss.SimpleBoss;
import net.stickmix.prisonevo.entity.PhantomIntelligentEntity;
import net.stickmix.prisonevo.entity.event.PhantomEntityDamagePlayerEvent;
import net.stickmix.prisonevo.entity.logic.aggressive.AggressiveLogicFlag;
import net.stickmix.prisonevo.entity.logic.aggressive.PhantomEntityAggressiveLogic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Map;

public class AquaMob extends SimpleBoss {

    private final AquaLordScript script;
    @Getter
    private final PhantomHologram displayName;

    public AquaMob(AquaLordScript script, Location location, double healthModifier) {
        super(EntityType.GUARDIAN, (script.isHeroic() ? 125 : 50) * healthModifier, location, null);
        this.script = script;

        this.displayName = GameApi.getPhantomEntityFactory().createHologram(getNameWithHealth());
        this.displayName.setLocation(location.clone().subtract(0, 0.5D, 0));

        setLogic(new AquaMobLogic(this));

        getLogic().aggressiveFlag(AggressiveLogicFlag.MOVEMENT_SPEED_MODIFIER, getLogic().getMovementSpeedModifier() * 0.4F)
                .aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.CLOSEST)
                .aggressiveFlag(AggressiveLogicFlag.ATTACK_SPEED, getLogic().getAttackSpeed() << 1)
                .aggressiveFlag(AggressiveLogicFlag.AGGRESSION_RANGE, 4)
                .aggressiveFlag(AggressiveLogicFlag.ATTACK_RANGE, 4)
                .aggressiveFlag(AggressiveLogicFlag.LAZY_TARGET_SEARCH, false)
                .aggressiveFlag(AggressiveLogicFlag.DAMAGE, 0D);
        if (script.isHeroic()) {
            getLogic().aggressiveFlag(AggressiveLogicFlag.DAMAGE, 7D);
        }
    }

    @Override
    public boolean damage(double value) {
        if (super.damage(value)) {
            die(false);
            script.playSound(Sound.ENTITY_IRONGOLEM_DEATH);
            return true;
        }
        script.playSound(Sound.ENTITY_IRONGOLEM_HURT);
        if (displayName.isSpawned()) {
            displayName.setText(getNameWithHealth());
        }
        return false;
    }

    @Override
    public PhantomEntityAggressiveLogic getLogic() {
        return (PhantomEntityAggressiveLogic) super.getLogic();
    }

    @Override
    public void spawnBoss() {
        spawn(true);
        displayName.spawn(true);
    }

    @Override
    public void despawnBoss() {
        if (isSpawned()) {
            invalidate();
        }
        if (displayName.isSpawned()) {
            displayName.invalidate();
        }
    }

    @Override
    public void teleportWithName(Location location) {
        teleport(location);
        displayName.teleport(getLocation().clone().subtract(0, 0.45D, 0));
    }

    @Override
    public BossBar getBar() {
        return null;
    }

    @Override
    public Map<Player, Double> getDamageDealers() {
        return getLogic().getDamageDealers();
    }

    public void die(boolean self) {
        if (isSpawned()) {
            invalidate();
        }
        if (displayName.isSpawned()) {
            displayName.invalidate();
        }
        script.playSound(Sound.ENTITY_ELDER_GUARDIAN_DEATH);
        if (self) {
            script.onSlaveSelfkilled(this);
        } else {
            script.onSlaveKilled(this);
        }
    }

    private String getNameWithHealth() {
        return String.format("&9&lСлуга под водой &c&l%.1f ❤", getHealth());
    }

    private static class AquaMobLogic extends PhantomEntityAggressiveLogic {

        public AquaMobLogic(PhantomIntelligentEntity creature) {
            super(creature);
        }

        @Override
        public void process() {
            AquaMob creature = (AquaMob) getCreature();
            if (!creature.isSpawned()) {
                return;
            }
            if (super.getCurrentTarget() == null) {
                super.setCurrentTarget(super.findTarget());
            }
            if (super.isValidTarget(super.getCurrentTarget()) && checkDistanceToTarget(super.getCurrentTarget())) {
                this.attack(getCurrentTarget());
            } else {
                super.setCurrentTarget(null);
            }
            if (creature.getLocation().distance(AquaLordScript.SPAWN_LOCATION) <= 1D) {
                creature.die(true);
                return;
            }
            super.getMovementAlgorithm().processMovement(creature, getMovementSpeedModifier(), AquaLordScript.SPAWN_LOCATION);
            creature.getDisplayName().teleport(creature.getLocation().clone().subtract(0, 0.5D, 0));
        }

        @Override
        public boolean attack(Player target) {
            if (!canAttack(target))
                return false;
            PhantomEntityDamagePlayerEvent event = new PhantomEntityDamagePlayerEvent(getCreature(), target, getDamage());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
            target.damage(event.getDamage());
            super.setLastAttackTime(System.currentTimeMillis());
            return true;
        }
    }
}
