package net.stickmix.prisonevo.utils;

import com.google.common.collect.MapMaker;
import net.stickmix.game.api.util.ChatUtil;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.plot.PvPManager;
import net.villenium.os.util.RListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AntiPvPQuit extends RListener {

    public final static Map<Player, Long> CACHE = new MapMaker().weakKeys().makeMap();
    private static int time;

    public AntiPvPQuit(int pvpTimeInSeconds) {
        time = pvpTimeInSeconds;
        String msg = ChatUtil.prefixed("AntiPvP", "&aВы вышли из режима боя.");
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PrisonEvo.getInstance(), () -> {
            long time = System.currentTimeMillis() - AntiPvPQuit.time * 1000L;
            Set<Player> freed = new HashSet<>();
            CACHE.forEach((p, l) -> {
                if (l >= time) {
                    return;
                }
                freed.add(p);
                if (!p.isOnline()) {
                    return;
                }
                p.sendMessage(msg);
            });
            freed.forEach(CACHE::remove);
        }, 20, 20);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) {
            return;
        }
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
            String msg = ChatUtil.prefixed("AntiPvP", "&cВы вошли в режим боя. Не выходите в течение &e%d &cсекунд.", time);
            long current = System.currentTimeMillis();
            if (CACHE.put(victim, current) == null) {
                victim.sendMessage(msg);
            }
            if (CACHE.put(damager, current) == null) {
                damager.sendMessage(msg);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (CACHE.remove(player) != null) {
            String msg = ChatUtil.prefixed("AntiPvP", "&aВы вышли из режима боя.");
            player.sendMessage(msg);
        }
    }

}
