package net.stickmix.prisonevo.entity.movement;

import net.stickmix.game.api.phantom.entity.PhantomEntity;
import org.bukkit.Location;

public class MovementManager {

    /**
     * Алгоритм, при котором сущность может спокойно проходить через любые препятствия.
     */
    public static MovementAlgorithm GO_THROUGH_EVERYTHING = (creature, speedModifier, destination) -> {
        final float speedPerSecond = 2 * speedModifier, speed = speedPerSecond / 20F;
        if (creature.getWorld() != destination.getWorld()) {
            return;
        }
        if (creature.getLocation().distance(destination) <= 0.7D) {
            return;
        }
        creature.lookAt(destination);
        Location delta = destination.clone().subtract(creature.getLocation());
        double length = delta.length();
        if (delta.length() > speed) {
            delta.setX(delta.getX() / length);
            delta.setY(delta.getY() / length);
            delta.setZ(delta.getZ() / length);
            delta.multiply(speed);
        }
        creature.moveWithBodyRotation(delta.getX(), delta.getY(), delta.getZ());
    };

    public static MovementAlgorithm DONT_MOVE = (creature, speedModifier, destination) -> {
    };

    public static void move(PhantomEntity entity, float speedModifier, Location destination) {
        GO_THROUGH_EVERYTHING.processMovement(entity, speedModifier, destination);
    }
}
