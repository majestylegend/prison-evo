package net.stickmix.prisonevo.utils;

import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.user.permission.UserPermission;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.data.GamePlayer;
import net.stickmix.prisonevo.plot.PvPManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class TeleportManager {

    private final static Map<Player, Location> teleporting = new HashMap<>();

    static {
        Bukkit.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST)
            public void onDamage(EntityDamageByEntityEvent e) {
                if (e.isCancelled()) {
                    return;
                }
                if (e.getEntity() instanceof Player) {
                    if (PvPManager.isProtected(e.getEntity())) {
                        e.setCancelled(true);
                        return;
                    }
                    cancelTeleportation((Player) e.getEntity());
                }
            }
        }, PrisonEvo.getInstance());
    }

    public static void teleport(Player player, Location location) {
        GamePlayer gamePlayer = PrisonEvo.getInstance().getAthenaManager().get(player);
        UserPermission perms = GameApi.getUserManager().get(player.getName()).getPermission();

        if (perms.isAdministrator()) {
            player.teleport(location);
            gamePlayer.sendMessage("&aВы были успешно телепортированы.");
            return;
        }

        if (AntiPvPQuit.CACHE.containsKey(player)) {
            gamePlayer.sendMessage("&cТелепортация во время PvP запрещена.");
            return;
        }
        if (teleporting.containsKey(player)) {
            gamePlayer.sendMessage("&cВы уже телепортируетесь.");
            return;
        }

        int delay = Modifiers.getTeleportDelay();
        gamePlayer.sendMessage("Телепортация начинается...");
        gamePlayer.sendMessage("&eВы будете телепортированы через &a%dс&e. Пожалуйста, не двигайтесь.", delay);
        teleporting.put(player, player.getLocation().clone());
        new BukkitRunnable() {

            int timer = delay;
            final Location playerLocation = player.getLocation().clone();

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancelTeleportation(player);
                    cancel();
                    return;
                }
                if (!teleporting.containsKey(player)) {
                    cancelTeleportation(player);
                    cancel();
                    return;
                }
                if (playerLocation.getWorld() != player.getWorld()) {
                    cancelTeleportation(player);
                    cancel();
                    return;
                }
                if (playerLocation.distance(player.getLocation()) > 0.5D) {
                    cancelTeleportation(player);
                    cancel();
                    return;
                }
                if (--timer <= 0) {
                    if (teleporting.remove(player) != null) {
                        player.teleport(location);
                        gamePlayer.sendMessage("&aВы были успешно телепортированы.");
                    }
                    cancel();
                }
            }
        }.runTaskTimer(PrisonEvo.getInstance(), 0, 20L);
    }

    public static void teleport(GamePlayer gamePlayer, Location location) {
        teleport(gamePlayer.getHandle(), location);
    }

    public static void cancelTeleportation(Player player) {
        if (teleporting.remove(player) != null) {
            PrisonEvo.getInstance().getAthenaManager().get(player).sendMessage("&cВаша телепортация отменена.");
        }
    }

}
