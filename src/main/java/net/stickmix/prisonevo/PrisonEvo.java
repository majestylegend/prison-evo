package net.stickmix.prisonevo;

import com.google.common.collect.Sets;
import lombok.Getter;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.command.CommandManager;
import net.stickmix.prisonevo.athena.AthenaManager;
import net.stickmix.prisonevo.commands.*;
import net.stickmix.prisonevo.items.EvoItemsManager;
import net.stickmix.prisonevo.listeners.DailyChestListener;
import net.stickmix.prisonevo.listeners.GeneralListener;
import net.stickmix.prisonevo.listeners.PlotListener;
import net.stickmix.prisonevo.plot.PlotManager;
import net.stickmix.prisonevo.plot.PvPManager;
import net.stickmix.prisonevo.utils.AntiPvPQuit;
import net.stickmix.prisonevo.utils.Modifiers;
import net.stickmix.prisonevo.utils.OnKillHelper;
import net.stickmix.transfer.network.packet.Packet7ServerConnect;
import net.villenium.os.service.transfer.NetworkManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

@Getter
public class PrisonEvo extends JavaPlugin {

    @Getter
    private static PrisonEvo instance;
    @Getter
    private AthenaManager athenaManager;
    public static Location SPAWN;
    @Getter
    private static boolean loaded = false;

    @Override
    public void onEnable() {
        instance = this;

        preloadWorlds();
        Bukkit.clearRecipes();

        EvoItemsManager.initialize();
        PlotManager.initialize();
        PvPManager.initialize();
        Modifiers.initialize();

        initializeCommands();

        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new GeneralListener(), this);
        manager.registerEvents(new OnKillHelper(null, 5000L), this);
        manager.registerEvents(new PlotListener(), this);
        manager.registerEvents(new DailyChestListener(), this);
        new AntiPvPQuit(5);

        athenaManager = new AthenaManager();

        loaded = true;
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach((p) -> {
            NetworkManager.send(new Packet7ServerConnect(p.getName(), "hub", true));
        });
    }

    private void initializeCommands() {
        CommandManager manager = GameApi.getCommandManager();
        manager.registerCommand(DeathMessageCommand.class);
        manager.registerCommand(ItemsCommand.class);
        manager.registerCommand(PayCommand.class);
        manager.registerCommand(SellAllCommand.class);
        manager.registerCommand(ShaftCommand.class);
        manager.registerCommand(SpawnCommand.class);
    }

    private void preloadWorlds() {
        Set<String> worlds = Sets.newHashSet(
                "prison_arena",
                "prison_boss1",
                "prison_boss2",
                "prison_boss3",
                "prison_boss4",
                "prison_boss5",
                "prison_boss6",
                "world"
        );

        worlds.forEach(worldName -> {
            WorldCreator wc = new WorldCreator(worldName);
            wc.generatorSettings("3;minecraft:air;2");
            wc.type(WorldType.FLAT);
            wc.generateStructures(false);
            World world = wc.createWorld();
            world.setGameRuleValue("announceAdvancements", "false");
            world.setGameRuleValue("doMobSpawning", "false");
            world.getEntities().forEach(Entity::remove);
        });
        World spawn = Bukkit.getWorld("world");
        SPAWN = new Location(spawn, 17002.433236987057, 137, 2977.553714138595, -0.348662f, -0.74993f);
        /*Lists.newArrayList(
                new Location(spawn, 229.5, 94.0, -87.5),
                new Location(spawn, 223.5, 94.0, -87.5)
        ).forEach(location -> location.getBlock().setType(Material.ENDER_CHEST));
        Lists.newArrayList(
                new Location(spawn, 227.5, 93.0, -77.5),
                new Location(spawn, 225.5, 93.0, -77.5)
        ).forEach(location -> location.getBlock().setType(Material.HOPPER));*/

        Bukkit.getWorlds().forEach(world -> world.getEntities().forEach(Entity::remove));
    }
}
