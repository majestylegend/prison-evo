package net.stickmix.prisonevo.boss.keeper;

import com.google.common.collect.Lists;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.boss.BossScript;
import net.stickmix.prisonevo.boss.BossSpell;
import net.stickmix.prisonevo.utils.DamageHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ForestKeeperScript extends BossScript {

    private final static World WORLD = Bukkit.getWorld("prison_boss1");
    public final static String BOSS_NAME = "&2&lХранитель Леса";

    private static Location loc(double x, double y, double z, float yaw, float pitch) {
        Location loc = loc(x, y, z);
        loc.setYaw(yaw);
        loc.setPitch(pitch);
        return loc;
    }

    private static Location loc(double x, double y, double z) {
        return new Location(WORLD, x, y, z);
    }

    private final static Location PLAYERS_TELEPORTATION_POINT = loc(49.62762631374888, 101.00526449764175, 0.47043208797968505, -269.85f, -0.750237f);
    public final static Location CENTER = loc(0, 99, 0);


    private final List<Location> SPAWN_LOCS = Lists.newArrayList(
            loc(0, 100, 0),
            loc(0.1, 100, 0),
            loc(0, 100, 0.1),
            loc(0.1, 100, 0.1)
    );

    private Location previous;
    private long lastUse = System.currentTimeMillis();

    public final BossSpell LIFT_UP = new BossSpell(25, 20) {
        @Override
        public void process() {
            List<Player> players = getPlayersInvolved();
            if (players.isEmpty()) {
                return;
            }
            Location location = CENTER.clone().add(0, 1, 0);
            players.forEach(player -> player.teleport(location));
            sendMessagePrefixed(BOSS_NAME, "Интересно, выживете ли вы на этот раз? ВВЕРХ!");
            sendMessagePrefixed(BOSS_NAME, "ПРОЧЬ ОТСЮДА! НАВСЕГДА!");
            Bukkit.getScheduler().runTaskLater(PrisonEvo.getInstance(), () -> getPlayersInvolved().forEach(player -> {
                player.setVelocity(CENTER.clone().subtract(player.getLocation()).toVector());
            }), 20L);
        }
    };

    private ForestKeeper mob;

    public ForestKeeperScript() {
        super(900, PLAYERS_TELEPORTATION_POINT);
        CENTER.getBlock().setType(Material.GOLD_BLOCK);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PrisonEvo.getInstance(), () -> {
            if (getPhase() != Phase.RUNNING) {
                return;
            }
            if (getPlayersInvolved().isEmpty()) {
                switchPhase(Phase.WAITING);
            }
        }, 20L, 20L);
    }

    @Override
    public void startTheGame() {
        Location location = SPAWN_LOCS.get(ThreadLocalRandom.current().nextInt(SPAWN_LOCS.size()));
        previous = location;
        mob = new ForestKeeper(this, location);
        mob.spawnBoss();
        sendMessagePrefixed(BOSS_NAME, "Посетители? Отлично! Я давно ожидаю гостей.");
    }

    @Override
    public void endTheGame() {
        mob.despawnBoss();
        mob = null;
    }

    @Override
    public int getHeroicLevel() {
        return 40;
    }

    public void castTpSpell() {
        if (System.currentTimeMillis() - lastUse < (isHeroic() ? 4000 : 2000)) {
            return;
        }
        lastUse = System.currentTimeMillis();
        if (getPlayersInvolved().isEmpty()) {
            return;
        }
        getPlayersInvolved().forEach(target -> {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1));
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
        });
        String phrase;
        switch (ThreadLocalRandom.current().nextInt(3)) {
            case 0:
                phrase = "Попрощайтесь со своим другом!";
                break;
            case 1:
                phrase = "Осмелились заявиться ко мне? Лучше бы сначала позаботились о ближних своих, хах!";
                break;
            default:
                phrase = "Оглянитесь: неужели это то, чего заслуживают ваши братья?";
                break;
        }
        sendMessagePrefixed(BOSS_NAME, phrase);
        Location location;
        do {
            location = SPAWN_LOCS.get(ThreadLocalRandom.current().nextInt(SPAWN_LOCS.size()));
        } while (location.equals(previous));
        previous = location;
        location.getWorld().strikeLightningEffect(location);
        mob.teleportWithName(location);
        Location finalLocation = location;
        Collection<Player> players = getPlayersInvolved().stream()
                .filter(player -> player.getLocation().distance(finalLocation) <= 4)
                .collect(Collectors.toSet());
        if (players.isEmpty()) {
            getPlayersInvolved().forEach(player -> {
                if (isHeroic()) {
                    DamageHelper.clearDamage(player, 4D);
                } else {
                    player.damage(3D);
                }
            });
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL && e.getEntity().getWorld() == CENTER.getWorld()) {
            e.setCancelled(true);
        }
    }
}
