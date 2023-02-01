package net.stickmix.prisonevo.plot;

import lombok.Data;
import net.stickmix.prisonevo.EntityManager;
import net.stickmix.prisonevo.entity.PhantomIntelligentEntity;
import net.stickmix.prisonevo.utils.Cuboid;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Data
public class Plot {

    private final int level;
    private final Cuboid cuboid;
    private final Location spawn;
    private final List<Resource> resources;
    private long lastReset = 0L;

    public Plot(int level, Location start, Location end, Location spawn, Map<Resource, Integer> blocks) {
        this.level = level;
        this.cuboid = Cuboid.fromPointLocations(start, end);
        this.spawn = spawn;
        this.resources = new ArrayList<>();

        blocks.forEach((itemPrice, integer) -> {
            for (int i = 0; i < integer; i++) {
                resources.add(itemPrice);
            }
        });
    }

    public Collection<Player> getPlayersInThisPlot() {
        return cuboid.getWorld().getPlayers().stream().filter(this::contains).collect(Collectors.toSet());
    }

    public void teleportPlayersToSpawn() {
        getPlayersInThisPlot().forEach(p -> p.teleport(spawn));
    }

    public void reset() {
        this.lastReset = System.currentTimeMillis();
        teleportPlayersToSpawn();
        EntityManager.getEntities().stream()
                .filter(e -> contains(e.getLocation()))
                .forEach(PhantomIntelligentEntity::invalidate);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        cuboid.blockStream().forEach(block -> {
            Resource price = resources.get(random.nextInt(resources.size()));
            block.setType(price.getBlock());
        });
    }

    public boolean contains(Location loc) {
        return cuboid.contains(loc);
    }

    public boolean contains(Block block) {
        return contains(block.getLocation());
    }

    public boolean contains(Entity entity) {
        return contains(entity.getLocation());
    }

    public void applyEffects(Player player) {
        if (level >= 71 && level <= 85) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 60, 0));
        }
        if (level >= 71 && level <= 73) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));
        }
    }

    @Data
    public static class ProbableResource {

        private final Resource resource;
        private final int probability;

    }

}
