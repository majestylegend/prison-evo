package net.stickmix.prisonevo.listeners;

import com.google.common.collect.Lists;
import net.stickmix.game.api.item.GameItemStack;
import net.stickmix.prisonevo.MainClass;
import net.stickmix.prisonevo.data.MenuData;
import net.stickmix.prisonevo.player.GamePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class GeneralListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        GamePlayer gamePlayer = MainClass.getInstance().getAthenaManager().get(player.getName());
        MainClass.getInstance().getScoreboardData().setup(gamePlayer);

        if (!player.getInventory().contains(Material.NETHER_STAR)) {
            player.getInventory().setItem(8, new GameItemStack(Material.NETHER_STAR, "&a&lМеню", Lists.newArrayList("&7Нажми, чтобы открыть менюшку (:")));
        }

        player.teleport(MainClass.getInstance().getSpawn());
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
    public void onInteractStar(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        if (item.getType() == Material.NETHER_STAR) {
            new MenuData(event.getPlayer());
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        event.setCancelled(true);
    }

}
