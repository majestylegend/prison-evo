package net.stickmix.prisonevo.mob;

import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.entity.PhantomIntelligentEntity;
import net.stickmix.prisonevo.entity.logic.aggressive.AggressiveLogicFlag;
import net.stickmix.prisonevo.entity.logic.aggressive.PhantomEntityAggressiveLogic;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class Silverfish extends PhantomIntelligentEntity {

    public Silverfish(Location spawnLocation) {
        super(EntityType.SILVERFISH, 200D, spawnLocation);

        setLogic(new PhantomEntityAggressiveLogic(this));

        getLogic()
                .aggressiveFlag(AggressiveLogicFlag.DAMAGE, 40D)
                .aggressiveFlag(AggressiveLogicFlag.MOVEMENT_SPEED_MODIFIER, 2F)
                .aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.CLOSEST)
                .aggressiveFlag(AggressiveLogicFlag.ATTACK_RANGE, 3)
                .aggressiveFlag(AggressiveLogicFlag.AGGRESSION_RANGE, 50);
    }

    @Override
    public PhantomEntityAggressiveLogic getLogic() {
        return (PhantomEntityAggressiveLogic) super.getLogic();
    }

    @Override
    public boolean damage(double value) {
        if (super.damage(value)) {
            LivingEntity target = getLogic().findMostDamageTarget();
            if (target != null) {
                PrisonEvo.getInstance().getAthenaManager().get(target.getName()).changeShards(5);
            }
            return true;
        }
        return false;
    }
}
