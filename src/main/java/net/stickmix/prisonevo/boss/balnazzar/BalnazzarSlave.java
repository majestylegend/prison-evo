package net.stickmix.prisonevo.boss.balnazzar;

import lombok.Getter;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.bar.BossBar;
import net.stickmix.game.api.phantom.entity.PhantomEntityInteraction;
import net.stickmix.game.api.phantom.entity.PhantomHologram;
import net.stickmix.prisonevo.boss.SimpleBoss;
import net.stickmix.prisonevo.entity.PhantomIntelligentEntity;
import net.stickmix.prisonevo.entity.logic.aggressive.AggressiveLogicFlag;
import net.stickmix.prisonevo.entity.logic.aggressive.PhantomEntityAggressiveLogic;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Map;

public class BalnazzarSlave extends SimpleBoss {

    private final BalnazzarScript script;
    @Getter
    private final PhantomHologram displayName;

    public BalnazzarSlave(BalnazzarScript script, Location location) {
        super(EntityType.CAVE_SPIDER, 500, location, null);
        this.script = script;

        displayName = GameApi.getPhantomEntityFactory().createHologram("&c&lСлуга Бальназзара");
        displayName.setLocation(location.clone().subtract(0, 0.4D, 0));

        displayName.setInteraction(new PhantomEntityInteraction() {
            @Override
            public void onLeftClick(Player player) {
                Balnazzar balnazzar = script.getBalnazzar();
                if (balnazzar != null) {
                    balnazzar.getInteraction().onLeftClick(player);
                }
            }

            @Override
            public void onRightClick(Player player) {

            }
        });

        setLogic(new BalnazzarSlaveLogic(this));

        getLogic().aggressiveFlag(AggressiveLogicFlag.AGGRESSION_RANGE, 50)
                .aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.MOST_RELEVANT)
                .aggressiveFlag(AggressiveLogicFlag.MOVEMENT_SPEED_MODIFIER, 2.5F)
                .aggressiveFlag(AggressiveLogicFlag.DAMAGE, 55D)
                .aggressiveFlag(AggressiveLogicFlag.LAZY_TARGET_SEARCH, false)
                .aggressiveFlag(AggressiveLogicFlag.ATTACK_SPEED, 700L);
    }

    @Override
    public boolean damage(double value) {
        if (super.damage(value)) {
            script.onSlaveKilled(this);
            despawnBoss();
            script.playSound(Sound.ENTITY_WITHER_DEATH);
            return true;
        }
        script.playSound(Sound.ENTITY_WITHER_HURT);
        return false;
    }

    @Override
    public void spawnBoss() {
        spawn(true);
        displayName.spawn(true);
    }

    @Override
    public void despawnBoss() {
        invalidate();
        displayName.invalidate();
    }

    @Override
    public void teleportWithName(Location location) {
        teleport(location);
        displayName.teleport(location.clone().subtract(0, 0.4D, 0));
    }

    @Override
    public BossBar getBar() {
        return null;
    }

    @Override
    public Map<Player, Double> getDamageDealers() {
        return getLogic().getDamageDealers();
    }

    @Override
    public BalnazzarSlaveLogic getLogic() {
        return (BalnazzarSlaveLogic) super.getLogic();
    }

    public static class BalnazzarSlaveLogic extends PhantomEntityAggressiveLogic {

        public BalnazzarSlaveLogic(PhantomIntelligentEntity creature) {
            super(creature);
        }

        @Override
        public void process() {
            super.process();

            ((BalnazzarSlave) getCreature()).getDisplayName().teleport(getCreature().getLocation().clone().subtract(0, 0.4D, 0));
        }
    }
}
