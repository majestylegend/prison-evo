package net.stickmix.prisonevo.boss.blaze;

import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.bar.BossBar;
import net.stickmix.game.api.phantom.entity.PhantomHologram;
import net.stickmix.prisonevo.boss.SimpleBoss;
import net.stickmix.prisonevo.entity.logic.LogicFlag;
import net.stickmix.prisonevo.entity.logic.aggressive.AggressiveLogicFlag;
import net.stickmix.prisonevo.entity.logic.aggressive.PhantomEntityAggressiveLogic;
import net.stickmix.prisonevo.entity.movement.MovementManager;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Map;

public class ProtectingCrystal extends SimpleBoss {

    private final BlazeKingScript script;
    private final PhantomHologram displayName;

    public ProtectingCrystal(BlazeKingScript script, Location location) {
        super(EntityType.MAGMA_CUBE, script.isHeroic() ? 500 : 300, location, null);
        this.script = script;
        this.displayName = GameApi.getPhantomEntityFactory().createHologram(getNameWithHealth());
        this.displayName.setLocation(location.clone().subtract(0, 0.5D, 0));

        setLogic(new PhantomEntityAggressiveLogic(this));

        getLogic().aggressiveFlag(AggressiveLogicFlag.DAMAGE, script.isHeroic() ? 9D : 3D)
                .aggressiveFlag(AggressiveLogicFlag.ATTACK_RANGE, 5)
                .aggressiveFlag(AggressiveLogicFlag.AGGRESSION_RANGE, 5)
                .aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.MOST_RELEVANT)
                .logicFlag(LogicFlag.MOVEMENT_ALGORITHM, MovementManager.DONT_MOVE);
    }

    @Override
    public PhantomEntityAggressiveLogic getLogic() {
        return (PhantomEntityAggressiveLogic) super.getLogic();
    }

    @Override
    public boolean damage(double value) {
        if (super.damage(value)) {
            despawnBoss();
            return true;
        }
        displayName.setText(getNameWithHealth());
        return false;
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
        script.onCrystalKilled(getLocation());
    }

    @Override
    public void teleportWithName(Location location) {
        teleport(location);
        displayName.teleport(location.clone().subtract(0, 0.5D, 0));
    }

    @Override
    public BossBar getBar() {
        return null;
    }

    @Override
    public Map<Player, Double> getDamageDealers() {
        return getLogic().getDamageDealers();
    }

    private String getNameWithHealth() {
        return String.format("%s %.1f ❤", BlazeKingScript.BOSS_NAME, getHealth());
    }
}
