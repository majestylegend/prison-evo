package net.stickmix.prisonevo.plot;

import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.utils.Cuboid;
import net.stickmix.prisonevo.utils.serialization.LocationsSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;

import java.util.HashSet;
import java.util.Set;

public class PvPManager {


    private final static Set<Cuboid> PROTECTED = new HashSet<>();

    public static void initialize() {
        World world = Bukkit.getWorld("world");
        FileConfiguration config = PrisonEvo.getInstance().getConfig();
        config.getConfigurationSection("regions").getKeys(false).forEach(key -> {
            String prefix = "regions." + key + ".";
            Location firstPoint = LocationsSerializer.deserializeLocation(config.getString(prefix + "firstPoint"), world);
            Location secondPoint = LocationsSerializer.deserializeLocation(config.getString(prefix + "secondPoint"), world);
            PROTECTED.add(Cuboid.fromPointLocations(firstPoint, secondPoint));
        });
        PROTECTED.add(Cuboid.fromWorldAndCoordinates(world, -18, 170, -28, 47, 86, 34));
        PROTECTED.add(Cuboid.fromWorldAndCoordinates(world, 346, 170, -34, 281, 86, 37));
        PROTECTED.add(Cuboid.fromWorldAndCoordinates(world, -265, 176, -21, -333, 80, 52));
        PROTECTED.add(Cuboid.fromWorldAndCoordinates(world, -336, 175, -315, -260, 80, -244));
        PROTECTED.add(Cuboid.fromWorldAndCoordinates(world, -679, 122, -215, -614, 44, -286));
        PROTECTED.add(Cuboid.fromWorldAndCoordinates(world, -682, 125, -514, -613, 44, -587));
        PROTECTED.add(Cuboid.fromWorldAndCoordinates(world, 208, 120, 311, 299, 33, 405));
        PROTECTED.add(Cuboid.fromWorldAndCoordinates(world, 275, 125, 601, 202, 33, 697));
        PROTECTED.add(Cuboid.fromWorldAndCoordinates(world, -532, 125, -832, -598, 35, -896));
        PROTECTED.add(Cuboid.fromWorldAndCoordinates(world, -838, 125, -832, -898, 35, -898));
    }

    public static boolean isProtected(Entity entity) {
        return PROTECTED.stream().anyMatch(c -> c.contains(entity));
    }

}
