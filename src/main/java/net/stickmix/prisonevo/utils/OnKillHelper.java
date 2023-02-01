package net.stickmix.prisonevo.utils;

import lombok.Data;
import net.stickmix.prisonevo.plot.PvPManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class OnKillHelper implements Listener {

    private final BiFunction<Player, Player, Void> function;
    private final static Map<Player, LastDamageInfo> damages = new HashMap<>();
    private static long maxPvPTime;

    public OnKillHelper(BiFunction<Player, Player, Void> onDamage, long maxPvPTime) {
        this.function = onDamage;
        OnKillHelper.maxPvPTime = maxPvPTime;
    }

    public static Player getDamager(Player victim) {
        LastDamageInfo info = damages.get(victim);
        if (info == null || System.currentTimeMillis() - info.getTime() > maxPvPTime) {
            return null;
        }
        return info.getDamager().get();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            Player victim = (Player) e.getEntity(), damager = null;
            if (e.getDamager() instanceof Player) {
                damager = (Player) e.getDamager();
            } else if (e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player) {
                damager = (Player) ((Projectile) e.getDamager()).getShooter();
            }
            if (damager == null) {
                return;
            }
            if (PvPManager.isProtected(victim)) {
                e.setCancelled(true);
                return;
            }
            damages.put(victim, new LastDamageInfo(new WeakReference<>(damager), System.currentTimeMillis()));
            if (function != null && victim.getHealth() - e.getFinalDamage() <= 0D) {
                function.apply(victim, damager);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        damages.remove(e.getPlayer());
    }

    @Data
    private static class LastDamageInfo {

        private final WeakReference<Player> damager;
        private final long time;

    }

}
