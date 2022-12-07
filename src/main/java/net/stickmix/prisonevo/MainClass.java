package net.stickmix.prisonevo;

import com.google.common.collect.Sets;
import lombok.Getter;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.command.CommandManager;
import net.stickmix.prisonevo.athena.AthenaManager;
import net.stickmix.prisonevo.command.CommandSpawn;
import net.stickmix.prisonevo.data.ScoreboardData;
import net.stickmix.prisonevo.listeners.GeneralListener;
import net.villenium.os.command.*;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class MainClass extends JavaPlugin {
    @Getter
    private static MainClass instance;
    @Getter
    private AthenaManager athenaManager;
    @Getter
    private ScoreboardData scoreboardData;

    @Getter
    private Location spawn;

    @Override
    public void onEnable() { //test
        instance = this;
        athenaManager = new AthenaManager();
        scoreboardData = new ScoreboardData();
        spawn = new Location(Bukkit.getWorld("ShaftMine"), 17002, 137, 2977);

        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new GeneralListener(), this);

        initializeCommands();
        preloadWorlds();
        Bukkit.clearRecipes();
    }

    private void initializeCommands() {
        CommandManager manager = GameApi.getCommandManager();
        manager.registerCommand(CommandSpawn.class);
    }

    private void preloadWorlds() {
        Set<String> worlds = Sets.newHashSet(
                "ShaftMine",
                "boss1",
                "boss2",
                "boss3",
                "boss4",
                "boss5",
                "boss6",
                "boss7",
                "boss8",
                "boss9"
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
        Bukkit.getWorlds().forEach(world -> world.getEntities().forEach(Entity::remove));
    }
}
