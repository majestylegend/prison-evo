package net.stickmix.prisonevo.listeners;

import lombok.val;
import net.stickmix.prisonevo.PrisonEvo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class DailyChestListener implements Listener {
    private static final Location CHEST_LOCATION = new Location(Bukkit.getWorld("world"), 17002, 135, 2966);

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        val player = event.getPlayer();
        val block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (block.getType() != Material.CHEST) {
            return;
        }
        if (!block.getLocation().equals(CHEST_LOCATION)) {
            return;
        }
        event.setCancelled(true);
        val gamePlayer = PrisonEvo.getInstance().getAthenaManager().get(player);
        val dailyChest = gamePlayer.getDailyChest();
        if (!dailyChest.isAvailable()) {
            gamePlayer.sendMessage(
                    "&cЕжедневная награда еще недоступна, подождите &b%s&a!",
                    dailyChest.getRemainingTime()
            );
            return;
        }
        dailyChest.takeAward();
    }
}
