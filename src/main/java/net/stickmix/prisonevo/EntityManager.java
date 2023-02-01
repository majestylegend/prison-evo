package net.stickmix.prisonevo;

import com.google.common.collect.Sets;
import net.stickmix.prisonevo.entity.PhantomIntelligentEntity;
import net.stickmix.prisonevo.entity.logic.PhantomEntityLogic;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class EntityManager {

    private static final Set<PhantomIntelligentEntity> ENTITIES = Sets.newConcurrentHashSet();

    private static boolean initialized = false;

    public static void add(PhantomIntelligentEntity entity) {
        ENTITIES.add(entity);
        if (!initialized) {
            initialized = true;
            new BukkitRunnable() {

                @Override
                public void run() {
                    if (ENTITIES.isEmpty()) {
                        cancel();
                        return;
                    }
                    ENTITIES.forEach(e -> {
                        PhantomEntityLogic logic = e.getLogic();
                        if (logic != null) {
                            logic.process();
                        }
                    });
                }
            }.runTaskTimer(PrisonEvo.getInstance(), 1L, 1L);
        }
    }

    public static void remove(PhantomIntelligentEntity entity) {
        ENTITIES.remove(entity);
        initialized = !ENTITIES.isEmpty();
    }

    public static Collection<PhantomIntelligentEntity> getEntities() {
        return Collections.unmodifiableSet(ENTITIES);
    }
}
