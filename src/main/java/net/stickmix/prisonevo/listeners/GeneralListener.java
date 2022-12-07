package net.stickmix.prisonevo.listeners;

import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.user.UserManager;
import net.stickmix.prisonevo.MainClass;
import net.stickmix.prisonevo.player.GamePlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GeneralListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        player.setGameMode(GameMode.SURVIVAL);
        GamePlayer gamePlayer = MainClass.getInstance().getAthenaManager().get(player.getName());
        MainClass.getInstance().getScoreboardData().setup(gamePlayer);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        onQuit(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        onQuit(event.getPlayer());
    }

    private void onQuit(Player player) {
        GamePlayer gamePlayer = MainClass.getInstance().getAthenaManager().get(player.getName());
        MainClass.getInstance().getAthenaManager().getGamePlayerObjectPool().save(player.getName(), true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        event.setCancelled(true);
    }

}
