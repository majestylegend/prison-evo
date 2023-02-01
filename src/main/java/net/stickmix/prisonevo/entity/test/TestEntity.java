package net.stickmix.prisonevo.entity.test;

import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.entity.PhantomIntelligentEntity;
import net.stickmix.prisonevo.entity.logic.aggressive.PhantomEntityAggressiveLogic;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

public class TestEntity extends PhantomIntelligentEntity {

    public TestEntity(EntityType type, double health, Location spawnLocation) {
        super(type, health, spawnLocation);
        setLogic(new TestEntityLogic(this));
        new BukkitRunnable() {

            @Override
            public void run() {
                if (!isSpawned()) {
                    cancel();
                    return;
                }
                getLogic().process();
            }
        }.runTaskTimer(PrisonEvo.getInstance(), 1L, 1L);
    }

    @Override
    public PhantomEntityAggressiveLogic getLogic() {
        return (PhantomEntityAggressiveLogic) super.getLogic();
    }

    public class TestEntityLogic extends PhantomEntityAggressiveLogic {

        public TestEntityLogic(PhantomIntelligentEntity creature) {
            super(creature);
        }
    }
}
