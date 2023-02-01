package net.stickmix.prisonevo.utils.serialization;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;


public interface LocationsSerializer {

    /**
     * Сериализовать локацию в строку.
     *
     * @param location локация.
     * @return строку - сериализованную локацию.
     */
    default String serializeLocation(Location location) {
        if (location == null) {
            return null;
        }
        float yaw = location.getYaw(), pitch = location.getPitch();
        String sloc = location.getWorld().getName() + " " + location.getX() + " " + location.getY() + " " + location.getZ();
        if (yaw != 0F || pitch != 0F) {
            sloc += " " + yaw + " " + pitch;
        }
        return sloc;
    }

    /**
     * Десереализовать локацию из строки.
     *
     * @param sloc  сериализованная локация.
     * @param world мир, в которую нужно отобразить полученную локацию.
     * @return локацию.
     */
    static Location deserializeLocation(String sloc, World world) {
        if (sloc == null) {
            return null;
        }
        String[] split = sloc.split(" ");
        if (world == null) {
            world = Bukkit.getWorld(split[0]);
            if (world == null) {
                world = Bukkit.createWorld(new WorldCreator(split[0]));
            }
        }
        Location loc = new Location(
                world,
                Double.parseDouble(split[1]),
                Double.parseDouble(split[2]),
                Double.parseDouble(split[3])
        );
        if (split.length == 6) {
            loc.setYaw(Float.parseFloat(split[4]));
            loc.setPitch(Float.parseFloat(split[5]));
        }
        return loc;
    }

}
