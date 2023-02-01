package net.stickmix.prisonevo.entity.logic;

import lombok.Getter;
import net.stickmix.prisonevo.entity.PhantomIntelligentEntity;
import net.stickmix.prisonevo.entity.movement.MovementAlgorithm;
import org.bukkit.entity.Player;

public abstract class PhantomEntityLogic {

    private final Object[] flags;

    @Getter
    private final PhantomIntelligentEntity creature;

    public PhantomEntityLogic(PhantomIntelligentEntity creature) {
        this.creature = creature;
        int total = LogicFlag.values().length;
        this.flags = new Object[total];
        for (int i = 0; i < total; ++i) {
            this.flags[i] = LogicFlag.values()[i].getDefaultValue();
        }
    }

    public PhantomEntityLogic logicFlag(LogicFlag flag, Object value) {
        this.flags[flag.ordinal()] = value;
        return this;
    }

    private Object getFlagValue(LogicFlag flag) {
        return this.flags[flag.ordinal()];
    }

    public boolean isFlying() {
        return (boolean) getFlagValue(LogicFlag.FLYING);
    }

    public MovementAlgorithm getMovementAlgorithm() {
        return (MovementAlgorithm) getFlagValue(LogicFlag.MOVEMENT_ALGORITHM);
    }

    /**
     * Метод, вызываемый каждый тик.
     */
    public abstract void process();

    public abstract void onSpawn();

    public abstract void onDespawn();

    /**
     * Метод, вызываемый, когда данной фейковой сущности наносится урон.
     *
     * @param damager тот, кто наносит урон.
     * @param damage  величина урона.
     */
    public abstract void registerDamage(Player damager, double damage);
}
