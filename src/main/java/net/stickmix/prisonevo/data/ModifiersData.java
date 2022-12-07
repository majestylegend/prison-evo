package net.stickmix.prisonevo.data;

import java.util.function.UnaryOperator;

public class ModifiersData {

    private static float MODIFIER = 0F;
    private static int TELEPORT_DELAY = 10;

    public static float getModifier() {
        return MODIFIER;
    }

    public static int getTeleportDelay() {
        return TELEPORT_DELAY;
    }

    public static UnaryOperator<Long> getLongModifier() {
        return value -> (long) (value - (value / 100 * MODIFIER));
    }

    public static UnaryOperator<Integer> getIntModifier() {
        return value -> (int) (value - (value / 100 * MODIFIER));
    }
}
