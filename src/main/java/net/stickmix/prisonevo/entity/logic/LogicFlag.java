package net.stickmix.prisonevo.entity.logic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.stickmix.prisonevo.entity.movement.MovementManager;

@RequiredArgsConstructor
public enum LogicFlag {

    FLYING(false),
    MOVEMENT_ALGORITHM(MovementManager.GO_THROUGH_EVERYTHING);

    @Getter
    private final Object defaultValue;
}
