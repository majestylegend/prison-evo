package net.stickmix.prisonevo.data;

import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.user.permission.UserPermission;
import net.stickmix.prisonevo.MainClass;
import net.stickmix.prisonevo.player.GamePlayer;
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

public class TeleportData {

    private final static Map<Player, Location> teleporting = new HashMap<>();

    static {
        Bukkit.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST)
            public void onDamage(EntityDamageByEntityEvent e) {
                if (e.isCancelled()) {
                    return;
                }
                if (e.getEntity() instanceof Player) {
                    /*if (PvPManager.isProtected(e.getEntity())) {
                        e.setCancelled(true);
                        return;
                    }*/
                    cancelTeleportation((Player) e.getEntity());
                }
            }
        }, MainClass.getInstance());
    }

    public static void teleport(Player player, Location location) {
        GamePlayer gamePlayer = MainClass.getInstance().getAthenaManager().get(player.getName());
        UserPermission perms = GameApi.getUserManager().get(player.getName()).getPermission();

        if (perms.isAdministrator()) {
            player.teleport(location);
            gamePlayer.sendMessage("&aВы были успешно телепортированы.");
            return;
        }

        if (CombatData.CACHE.containsKey(player)) {
            gamePlayer.sendMessage("&cТелепортация во время PvP запрещена.");
            return;
        }

        if (teleporting.containsKey(player)) {
            gamePlayer.sendMessage("&cВы уже телепортируетесь.");
            return;
        }

        int delay = 7;
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
        }.runTaskTimer(MainClass.getInstance(), 0, 20L);
    }

    public static void teleport(GamePlayer gamePlayer, Location location) {
        teleport(gamePlayer.getHandle(), location);
    }

    public static void cancelTeleportation(Player player) {
        if (teleporting.remove(player) != null) {
            MainClass.getInstance().getAthenaManager().get(player.getName()).sendMessage("&cВаша телепортация отменена.");
        }
    }

}
