package net.stickmix.prisonevo.plot;

import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.data.GamePlayer;
import net.stickmix.prisonevo.utils.serialization.LocationsSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlotManager {

    private final static Map<Integer, Plot> plots = new ConcurrentHashMap<>();

    public static void initialize() {
        World world = Bukkit.getWorld("world");
        FileConfiguration config = PrisonEvo.getInstance().getConfig();
        config.getConfigurationSection("mines").getKeys(false).forEach(key -> {
            String prefix = "mines." + key + ".";
            int level = config.getInt(prefix + "level");
            Location start = LocationsSerializer.deserializeLocation(config.getString(prefix + "start"), world);
            Location end = LocationsSerializer.deserializeLocation(config.getString(prefix + "end"), world);
            Location spawn = LocationsSerializer.deserializeLocation(config.getString(prefix + "spawn"), world);
            Map<Resource, Integer> blocks = Arrays.stream(config.getString(prefix + "resources").split(" "))
                    .filter(res -> !res.isEmpty())
                    .map(res -> res.split("-"))
                    .collect(Collectors.toMap(res -> Resource.valueOf(res[0]), res -> Integer.parseInt(res[1])));
            Plot mine = new Plot(level, start, end, spawn, blocks);
            plots.put(level, mine);
            mine.reset();
        });
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PrisonEvo.getInstance(), () -> {
            plots.values().stream()
                    .filter(plot -> {
                        long resetTime = 120_000L;
                        if (plot.getLevel() > 70) {
                            resetTime = 60_000L;
                        }
                        return System.currentTimeMillis() - plot.getLastReset() > resetTime;
                    })
                    .forEach(Plot::reset);
        }, 0, 20L * 5);
    }

    public static Collection<Plot> getPlots() {
        return Collections.unmodifiableCollection(plots.values());
    }

    public static Plot getPlotByLevel(int level) {
        return plots.get((level & 1) == 0 ? level - 1 : level);
    }

    public static Plot getPlotByLoc(Location location) {
        return plots.values().stream().filter(p -> p.contains(location)).findFirst().orElse(null);
    }

    public static boolean isInAnyPlot(Location loc) {
        return plots.values().stream().anyMatch(p -> p.contains(loc));
    }

    public static boolean isInAnyPlot(Block block) {
        return plots.values().stream().anyMatch(p -> p.contains(block));
    }

    public static boolean isAllowBreak(GamePlayer gamePlayer, Location location) {
        return plots.values().stream()
                .filter(plot -> plot.getCuboid().contains(location))
                .findFirst()
                .filter(plot -> {
                    int level = gamePlayer.getLevel();
                    return plot.getLevel() <= level || gamePlayer.asUser().getPermission().isAdministrator();
                })
                .isPresent();
    }

    public static boolean isAllowBreakInPlot(GamePlayer gamePlayer, Location location) {
        return plots.values().stream().anyMatch(plot -> {
            if (!plot.getCuboid().contains(location)) {
                return false;
            }
            int level = gamePlayer.getLevel();
            return plot.getLevel() <= level || gamePlayer.asUser().getPermission().isAdministrator();
        });
    }
}
