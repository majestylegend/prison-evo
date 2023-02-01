package net.stickmix.prisonevo.entity.logic.aggressive;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AggressiveLogicFlag {

    ATTACK_ONLY_PLAYERS(true),
    AGGRESSION_RANGE(16),
    TARGETS_SEARCH_ALGORITHM(TargetSearchAlgorithm.MOST_DAMAGE),
    ATTACK_SPEED(500L), //Задержка в миллисекундах между атаками
    ATTACK_RANGE(2),
    DAMAGE(2D),
    MOVEMENT_SPEED_MODIFIER(1F),
    LAZY_TARGET_SEARCH(true);   //Если false, то будет искать новую цель каждую секунду, даже когда она уже есть (true оптимальнее)

    @Getter
    private final Object defaultValue;

    public enum TargetSearchAlgorithm {
        MOST_DAMAGE, CLOSEST, MOST_RELEVANT, NONE
    }
}
