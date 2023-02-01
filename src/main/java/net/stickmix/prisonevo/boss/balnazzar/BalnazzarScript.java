package net.stickmix.prisonevo.boss.balnazzar;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.boss.BossScript;
import net.stickmix.prisonevo.boss.BossSpell;
import net.stickmix.prisonevo.entity.PhantomIntelligentEntity;
import net.stickmix.prisonevo.entity.event.PlayerDamagePhantomEntityEvent;
import net.stickmix.prisonevo.utils.Cuboid;
import net.villenium.os.util.Task;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BalnazzarScript extends BossScript {

    public static final String BOSS_NAME = "&4&lБальназзар, Повелитель Тьмы";
    public static final World WORLD = Bukkit.getWorld("prison_boss6");
    public final static Location
            CENTER = loc(0.5, 94.0, 0.5, 0F, 0F),
            CENTER_FLYING = loc(0.5, 95.0, 0.5, 0F, 0F),
            PLAYERS_TELEPORTATION_POINT = loc(0.5, 97.0, -17.5, 0F, 0F);

    private final static List<Location> SLAVES_LOCS = Lists.newArrayList(
            loc(0, 96, 7),
            loc(-9, 96, 2),
            loc(-4, 96, -4),
            loc(3, 96, -5)
    );

    public final BossSpell
            BLOOD_CURSE = new BossSpell(60, 50) {
        @Override
        public void process() {
            bloodCurse.clear();
            List<Player> players = getPlayersInvolved();
            bloodCurse.addAll(players);
            sendMessage("&4На вас наложе проклятие крови! Все ваши атаки будут наносить вам урон!");
            Task.schedule(bloodCurse::clear, 200L);
        }
    }, SPAWN_SLAVES = new BossSpell(70, 60) {
        @Override
        public void process() {
            sendMessagePrefixed(BOSS_NAME, "Вы думали я тут один? Я не дурак.");
            for (int i = 0; i < 4; i++) {
                BalnazzarSlave slave = new BalnazzarSlave(BalnazzarScript.this, SLAVES_LOCS.get(i));
                slave.spawnBoss();
                slaves.add(slave);
            }
            sendMessagePrefixed(BOSS_NAME, "Испепелите здесь все, слуги мои!");
        }
    }, SLEEP = new BossSpell(40, 35) {
        @Override
        public void process() {
            List<Player> players = getPlayersInvolved();
            Collections.shuffle(players);
            if (players.isEmpty()) {
                return;
            }
            sleep = players.get(0);
            if (!sleep.isOnGround()) {
                sleep = null;
                return;
            }
            sendMessagePrefixed(BOSS_NAME, "%s&4, я сломлю твою волю, ты в моей власти!", PrisonEvo.getInstance().getAthenaManager().get(sleep).asUser().getFullDisplayName());
            Task.schedule(() -> sleep = null, 200L);
        }
    }, VORTEX = new BossSpell(90, 75) {
        @Override
        public void process() {
            Collection<Player> players = balnazzar.getLocation().getNearbyPlayers(8);
            if (players.isEmpty()) {
                return;
            }
            Map<Player, Long> times = new HashMap<>();
            long startTime = System.currentTimeMillis();
            players.forEach(player -> times.put(player, startTime));
            Map<Player, Location> centers = new HashMap<>();
            Map<Player, Integer> sides = new HashMap<>();
            AtomicInteger taskID = new AtomicInteger();
            taskID.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(PrisonEvo.getInstance(), () -> {
                if (System.currentTimeMillis() - startTime >= (isHeroic() ? 9000L : 6000L)) {
                    Bukkit.getScheduler().cancelTask(taskID.get());
                    return;
                }
                players.removeIf(entity -> !isValidForBattle(entity, times));
                if (players.isEmpty()) {
                    Bukkit.getScheduler().cancelTask(taskID.get());
                    return;
                }
                players.forEach(target -> {
                    target.damage(isHeroic() ? 21 : 15);
                    if (isValidForBattle(target, times)) {
                        Location center = centers.get(target);
                        Vector velocity;
                        if (center == null) {
                            center = target.getLocation();
                            centers.put(target, center);
                            sides.put(target, 0);
                            velocity = getVelocity(3);
                        } else {
                            int side = sides.get(target);
                            velocity = getVelocity(side);
                            sides.put(target, (side + 1) % 4);
                        }
                        target.setVelocity(velocity);

                    }
                });
            }, 1L, 5L));
        }

        private Vector getVelocity(int side) {
            int dx = side & 1, dz = 1 - dx;
            int mod = (side & 2) == 2 ? 1 : -1;
            return new Vector(dx * mod, .25D, dz * mod);
        }

        private boolean isValidForBattle(Player player, Map<Player, Long> times) {
            if (!player.isOnline()) {
                return false;
            }
            if (player.getWorld() != balnazzar.getWorld()) {
                return false;
            }
            return balnazzar.getLocation().distance(player.getLocation()) <= 8
                    && System.currentTimeMillis() - times.getOrDefault(player, 0L) < (isHeroic() ? 9000L : 6000L);
        }
    }, SWAP = new BossSpell(15, 10) {
        @Override
        public void process() {
            List<Player> players = getPlayersInvolved();
            if (players.isEmpty()) {
                return;
            }
            Collections.shuffle(players);
            Player target = players.get(0);
            Location location = target.getLocation().clone();
            target.teleport(balnazzar.getLocation());
            balnazzar.teleport(location);
            sendMessagePrefixed(BOSS_NAME, "Ахахаха!");
        }
    };

    private final Set<Player> bloodCurse = new HashSet<>();
    private final Set<BalnazzarSlave> slaves = Sets.newConcurrentHashSet();
    private Player sleep;

    @Getter
    private Balnazzar balnazzar;

    @Getter
    @Setter
    private int battlePhase;
    private long lastTpUse;

    public BalnazzarScript() {
        super(1500, PLAYERS_TELEPORTATION_POINT);
        CENTER.getBlock().setType(Material.GOLD_BLOCK);
        Cuboid.fromWorldAndCoordinates(CENTER.getWorld(), 2, 98, -19, -2, 99, -19).blockStream().forEach(block -> block.setType(Material.BARRIER));
        Task.schedule(() -> {
            if (getPhase() != Phase.RUNNING) {
                return;
            }
            Collection<Player> players = getPlayersInvolved();
            if (players.isEmpty()) {
                switchPhase(Phase.WAITING);
            }
            if (players.size() < 3 && battlePhase == 1) {
                getPlayersInvolved().forEach(player -> player.damage(200));
            }
        }, 20L, 20L);
    }

    @Override
    public void startTheGame() {
        balnazzar = new Balnazzar(this, CENTER_FLYING);
        balnazzar.spawnBoss();
        battlePhase = 1;
        lastTpUse = 0L;
        slaves.clear();
        sleep = null;
        sendMessagePrefixed(BOSS_NAME, "Ваша судьба была давно предрешена. Мной.");
    }

    @Override
    public void endTheGame() {
        balnazzar.despawnBoss();
        balnazzar = null;
        slaves.forEach(BalnazzarSlave::despawnBoss);
        slaves.clear();
    }

    @Override
    public int getHeroicLevel() {
        return 90;
    }

    public void castTpSpell() {
        if (System.currentTimeMillis() - lastTpUse <= 5_000) {
            return;
        }
        Player target = balnazzar.getLogic().getCurrentTarget();
        if (target != null && target.getWorld() == WORLD && target.getLocation().distance(balnazzar.getLocation()) > 5) {
            balnazzar.teleportWithName(target.getLocation());
            getPlayersInvolved().forEach(player -> player.damage(25D));
        }
        lastTpUse = System.currentTimeMillis();
    }

    private static Location loc(double x, double y, double z, float yaw, float pitch) {
        Location loc = loc(x, y, z);
        loc.setYaw(yaw);
        loc.setPitch(pitch);
        return loc;
    }

    private static Location loc(double x, double y, double z) {
        return new Location(WORLD, x, y, z);
    }

    public void onSlaveKilled(BalnazzarSlave slave) {
        if (!slaves.remove(slave)) {
            return;
        }
        if (slaves.isEmpty()) {
            sendMessagePrefixed(BOSS_NAME, "Довольно! Ни один из нас не умрет от этого проклятого клинка. Вы... умрете!");
        }
    }

    @EventHandler
    public void onDamage(PlayerDamagePhantomEntityEvent event) {
        Player damager = event.getDamager();
        if (bloodCurse.contains(damager)) {
            damager.damage(event.getDamage());
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (sleep == null) {
            return;
        }
        if (!sleep.isOnline()) {
            sleep = null;
        }
        if (sleep == event.getPlayer()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBalnazzarDamage(PlayerDamagePhantomEntityEvent event) {
        PhantomIntelligentEntity victim = event.getVictim();
        if (!(victim instanceof Balnazzar)) {
            return;
        }
        if (!slaves.isEmpty()) {
            event.setCancelled(true);
        }
        Player player = event.getDamager();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() != Material.GOLD_SWORD) {
            event.setCancelled(true);
            PrisonEvo.getInstance().getAthenaManager().get(player).sendMessage("&cВаши оружие бесполезно против этого босса!");
        }
    }
}
