package net.stickmix.prisonevo.entity.logic.aggressive;

import com.google.common.collect.MapMaker;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.stickmix.prisonevo.entity.PhantomIntelligentEntity;
import net.stickmix.prisonevo.entity.event.PhantomEntityDamagePlayerEvent;
import net.stickmix.prisonevo.entity.logic.LogicFlag;
import net.stickmix.prisonevo.entity.logic.PhantomEntityLogic;
import net.stickmix.prisonevo.entity.movement.MovementAlgorithm;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class PhantomEntityAggressiveLogic extends PhantomEntityLogic {

    private final Object[] flags;

    @Setter
    @Getter
    private Player currentTarget;

    @Setter
    @Getter
    private long lastAttackTime = 0L;

    @Getter
    private final Map<Player, Double> damageDealers = new MapMaker().weakKeys().makeMap();

    public PhantomEntityAggressiveLogic(PhantomIntelligentEntity creature) {
        super(creature);
        int total = AggressiveLogicFlag.values().length;
        this.flags = new Object[total];
        for (int i = 0; i < total; ++i) {
            this.flags[i] = AggressiveLogicFlag.values()[i].getDefaultValue();
        }
    }

    public PhantomEntityAggressiveLogic aggressiveFlag(AggressiveLogicFlag flag, Object value) {
        this.flags[flag.ordinal()] = value;
        return this;
    }

    @Override
    public PhantomEntityAggressiveLogic logicFlag(LogicFlag flag, Object value) {
        super.logicFlag(flag, value);
        return this;
    }

    public Object getFlagValue(AggressiveLogicFlag flag) {
        return this.flags[flag.ordinal()];
    }

    public void registerDamage(Player damager, double damage) {
        this.damageDealers.put(damager, getDamageDealtBy(damager) + damage);
    }

    public double getDamageDealtBy(Player player) {
        return damageDealers.getOrDefault(player, 0D);
    }

    public boolean isAttackingOnlyPlayers() {
        return (boolean) getFlagValue(AggressiveLogicFlag.ATTACK_ONLY_PLAYERS);
    }

    public int getAggressionRange() {
        return (int) getFlagValue(AggressiveLogicFlag.AGGRESSION_RANGE);
    }

    protected AggressiveLogicFlag.TargetSearchAlgorithm getTargetSearchAlgorithm() {
        return (AggressiveLogicFlag.TargetSearchAlgorithm) getFlagValue(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM);
    }

    public boolean isFindingMostDamageTargets() {
        return getTargetSearchAlgorithm() == AggressiveLogicFlag.TargetSearchAlgorithm.MOST_DAMAGE;
    }

    public boolean isFindingClosestTargets() {
        return getTargetSearchAlgorithm() == AggressiveLogicFlag.TargetSearchAlgorithm.CLOSEST;
    }

    public boolean isFindingMostRelevantTargets() {
        return getTargetSearchAlgorithm() == AggressiveLogicFlag.TargetSearchAlgorithm.MOST_RELEVANT;
    }

    public boolean isNotFindingTargets() {
        return getTargetSearchAlgorithm() == AggressiveLogicFlag.TargetSearchAlgorithm.NONE;
    }

    public boolean isTargetSearchLazy() {
        return (boolean) getFlagValue(AggressiveLogicFlag.LAZY_TARGET_SEARCH);
    }

    public int getAttackRange() {
        return (int) getFlagValue(AggressiveLogicFlag.ATTACK_RANGE);
    }

    public float getMovementSpeedModifier() {
        return (float) getFlagValue(AggressiveLogicFlag.MOVEMENT_SPEED_MODIFIER);
    }

    /**
     * Метод получения скорости атаки.
     *
     * @return возвращает минимально возможную задержку в миллисекундах между двумя атаками.
     */
    public long getAttackSpeed() {
        return (long) getFlagValue(AggressiveLogicFlag.ATTACK_SPEED);
    }

    public double getDamage() {
        return (double) getFlagValue(AggressiveLogicFlag.DAMAGE);
    }

    /**
     * Проверка задержки относительно предыдущей атаки: может ли быть нанесена новая атака.
     *
     * @return true, если с момента последней атаки прошло больше времени, чем задержка между атаками.
     */
    public boolean canAttack() {
        return System.currentTimeMillis() - lastAttackTime > getAttackSpeed();
    }

    /**
     * Проверка задержки относительно предыдущей атаки и расстояния до заданной живой сущности, а также
     * ее игрового режима, если это игрок.
     *
     * @param le атакуемая живая сущность.
     * @return true, если с момента последней атаки прошло больше времени, чем задержка между атаками,
     * и расстояние до атакуемой цели не превышает максимально возможного.
     */
    public boolean canAttack(Player le) {
        if (canAttack() && getCreature().getLocation().distance(le.getLocation()) <= getAttackRange()) {
            return le.getGameMode() == GameMode.SURVIVAL;
        }
        return false;
    }

    public boolean attack(Player target) {
        if (!canAttack(target)) {
            return false;
        }
        val event = new PhantomEntityDamagePlayerEvent(getCreature(), target, getDamage());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        getCreature().lookAt(target);
        getCreature().getAnimations().playAnimationHand();
        target.damage(event.getFinalDamage());
        this.lastAttackTime = System.currentTimeMillis();
        return true;
    }

    protected boolean isValidTarget(Player possibleTarget) {
        return possibleTarget != null && possibleTarget.isValid() && !possibleTarget.isDead();
    }

    protected boolean checkDistanceToTarget(Player target) {
        Location me = getCreature().getLocation(), him = target.getLocation();
        if (me.getWorld() != him.getWorld()) {
            return false;
        }
        return me.distance(him) <= getAggressionRange();
    }

    @Override
    public void process() {
        if (!getCreature().isSpawned()) {
            return;
        }
        MovementAlgorithm movement = getMovementAlgorithm();
        if (updateTarget() == null) {
            return;
        }
        movement.processMovement(getCreature(), getMovementSpeedModifier(), this.currentTarget.getLocation());
        attack(this.currentTarget);
    }

    @Override
    public void onSpawn() {

    }

    @Override
    public void onDespawn() {
        damageDealers.clear();
        this.currentTarget = null;
    }

    private long lastLookingForATarget = 0L;

    protected Player updateTarget() {
        if (this.currentTarget == null || !isTargetSearchLazy()) {
            this.currentTarget = findTarget();
        }
        if (!isValidTarget(this.currentTarget) || !checkDistanceToTarget(this.currentTarget)) {
            this.currentTarget = null;
            return null;
        }
        return this.currentTarget;
    }

    protected Player findTarget() {
        long current = System.currentTimeMillis();
        if (current - lastLookingForATarget < 1000L) {
            return this.currentTarget;
        }
        lastLookingForATarget = current;
        if (isFindingClosestTargets()) {
            return findClosestTarget();
        }
        if (isFindingMostDamageTargets()) {
            return findMostDamageTarget();
        }
        if (isFindingMostRelevantTargets()) {
            return findMostRelevantTarget();
        }
        return null;
    }

    protected Player findClosestTarget() {
        return getAttackableEntitiesInAggressionRange().stream()
                .min(Comparator.comparingDouble(le -> le.getLocation().distance(getCreature().getLocation()))
                ).orElse(null);
    }

    public Player findMostDamageTarget() {
        return getAttackableEntitiesInAggressionRange().stream()
                .max(Comparator.comparingDouble(this::getDamageDealtBy))
                .orElse(null);
    }

    /**
     * Поиск цели из радиуса агрессии с минимальным кол-вом здоровья.
     *
     * @return null, если такую цель не удалось найти, иначе ее саму.
     */
    protected Player findMostRelevantTarget() {
        return getAttackableEntitiesInAggressionRange().stream()
                .min(Comparator.comparingDouble(Damageable::getHealth)).orElse(null);
    }

    protected Collection<Player> getAttackableEntitiesInAggressionRange() {
        if (isAttackingOnlyPlayers()) {
            return getPlayersInAggressionRange();
        }
        return getEntitiesInAggressionRange();
    }

    protected Collection<Player> getEntitiesInAggressionRange() {
        return getNearbyEntities(Player.class);
    }

    protected Collection<Player> getPlayersInAggressionRange() {
        return getNearbyEntities(Player.class).stream()
                .filter(p -> p.getGameMode() == GameMode.SURVIVAL)
                .collect(Collectors.toSet());
    }

    private <T extends Player> Collection<T> getNearbyEntities(Class<T> clazz) {
        Location me = getCreature().getLocation();
        int aggressionRange = (int) Math.pow(this.getAggressionRange(), 2); //микрооптимизации для неизвлечения корня
        return getCreature().getWorld().getEntitiesByClass(clazz).stream()
                .filter(this::isValidTarget)
                .filter(e -> e.getLocation().distanceSquared(me) <= aggressionRange)
                .collect(Collectors.toSet());
    }
}
