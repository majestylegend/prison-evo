package net.stickmix.prisonevo.entity.movement;

import net.stickmix.game.api.phantom.entity.PhantomEntity;
import org.bukkit.Location;

@FunctionalInterface
public interface MovementAlgorithm {

    /**
     * Метод, вызываемый каждый тик.
     *
     * @param creature      сущность, которая должна перемещаться по закону этого алгоритма.
     * @param speedModifier множитель скорости.
     * @param destination   конечная точка прибытия.
     */
    void processMovement(PhantomEntity creature, float speedModifier, Location destination);
}
