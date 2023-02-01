package net.stickmix.prisonevo.boss.argus;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.boss.BossScript;
import net.stickmix.prisonevo.boss.BossSpell;
import net.stickmix.prisonevo.data.GamePlayer;
import net.stickmix.prisonevo.entity.logic.aggressive.AggressiveLogicFlag;
import net.stickmix.prisonevo.utils.DamageHelper;
import net.villenium.os.util.Task;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class ArgusScript extends BossScript {

    public static final String BOSS_NAME = "&b&lАргус Порабощенный";
    public static final World WORLD = Bukkit.getWorld("prison_boss5");
    public final static Location
            CENTER = loc(-4.5D, 108.0D, -2.5D, 0F, 0F);
    private final static Location CENTER_FLYING = loc(-4.5D, 115.0D, -2.5D, 0F, 0F);
    private final static Location PLAYERS_TELEPORTATION_POINT = loc(-4.5D, 110.0D, -22.5D, 0F, 0F);

    private final List<Location> PRISON_LOCATIONS = Lists.newArrayList(
            loc(-4.5, 118.0, 11.5),
            loc(-4.5, 120.0, -10.5)
    );

    static {
        WORLD.setGameRuleValue("doDaylightCycle", "false");
        WORLD.setTime(0);
    }

    public final BossSpell
            THROW_AWAY = new BossSpell(20, 17) {
        @Override
        public void process() {
            Vector velocity = new Vector(0D, 1D, 0D);
            getPlayersInvolved().forEach(p -> p.setVelocity(velocity));
            Task.schedule(() -> getPlayersInvolved().forEach(p -> {
                p.setVelocity(CENTER.clone().subtract(p.getLocation()).toVector().setY(0D));
                DamageHelper.clearDamage(p, isHeroic() ? 6D : 4D);
            }), 20L);
            sendMessagePrefixed(BOSS_NAME, "Прочь отсюда!");
        }
    }, DARKNESS = new BossSpell(30, 25) {
        @Override
        public void process() {
            getPlayersInvolved().forEach(p -> {
                DamageHelper.clearDamage(p, isHeroic() ? 6D : 4D);
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 9));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 3));
            });
            sendMessagePrefixed(BOSS_NAME, "Ваши глаза и ноги слабы. Вы будете страдать и молить о пощаде, но я не буду слушать..");
        }
    }, KNOCK_SWORD = new BossSpell(25, 20) {
        @Override
        public void process() {
            getPlayersInvolved().forEach(p -> {
                PlayerInventory inventory = p.getInventory();
                int heldItemSlot = inventory.getHeldItemSlot();
                int randomSlot;
                do {
                    randomSlot = ThreadLocalRandom.current().nextInt(35);
                } while (randomSlot == heldItemSlot);
                ItemStack randomItem = inventory.getItem(randomSlot);
                ItemStack hand = inventory.getItemInMainHand();
                if (hand.getType() == Material.NETHER_STAR) {
                    return;
                }
                inventory.setItem(randomSlot, hand);
                inventory.setItem(heldItemSlot, randomItem);
            });
            sendMessagePrefixed(BOSS_NAME, "Вы и ваше оружие ничто! Ахахахахах");
        }
    }, KILL = new BossSpell(55, 50) {
        @Override
        public void process() {
            List<Player> players = getPlayersInvolved();
            players.removeIf(player -> player.getGameMode() == GameMode.CREATIVE);
            Player random = players.get(ThreadLocalRandom.current().nextInt(players.size()));
            random.getWorld().strikeLightningEffect(random.getLocation());
            ItemStack[] contents = random.getInventory().getContents();
            boolean kill = true;
            for (int i = 0; i < contents.length; i++) {
                ItemStack content = contents[i];
                if (content == null) {
                    continue;
                }
                if (content.getType() == Material.TOTEM) {
                    kill = false;
                    if (content.getAmount() > 1) {
                        content.setAmount(content.getAmount() - 1);
                    } else {
                        contents[i] = null;
                    }
                    break;
                }
            }
            random.getInventory().setContents(contents);
            if (kill) {
                random.damage(200);
                sendMessagePrefixed(BOSS_NAME, "Один герой пал. Следом за ним падете и вы!");
            } else {
                PrisonEvo.getInstance().getAthenaManager().get(random).sendMessage("&bТотен бессмертия спас вас от гибели..");
                sendMessagePrefixed(BOSS_NAME, "&4Аааах, проклятый тотем! Зря вы его сюда принесли!");
            }
        }
    }, SOCIAL = new BossSpell(40, 35) {
        @Override
        public void process() {
            List<Player> players = getPlayersInvolved();
            players.removeIf(player -> player.getGameMode() == GameMode.CREATIVE);
            if (players.isEmpty()) {
                return;
            }
            Player target = players.get(ThreadLocalRandom.current().nextInt(players.size()));
            argus.getLogic()
                    .aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.MOST_RELEVANT)
                    .setCurrentTarget(target);
            GamePlayer gamePlayer = PrisonEvo.getInstance().getAthenaManager().get(target);
            gamePlayer.sendMessage("&4На вас наложено проклятье социума.");
            sendMessagePrefixed(BOSS_NAME, "%s &cхорошо послужит мне..", gamePlayer.asUser().getFullDisplayName());
            new BukkitRunnable() {

                int tick = 0;

                @Override
                public void run() {
                    if (getPhase() != Phase.RUNNING) {
                        cancel();
                        return;
                    }
                    if (!target.getWorld().equals(CENTER.getWorld())) {
                        cancel();
                        return;
                    }
                    if (tick++ == 15) {
                        cancel();
                        return;
                    }
                    Location center = target.getLocation().clone().add(0, 1.5, 0);
                    for (double phi = 0; phi <= Math.PI; phi += Math.PI / 15) {
                        double y = Math.cos(phi) + 1.5;
                        for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 30) {
                            double x = Math.cos(theta) * Math.sin(phi);
                            double z = Math.sin(theta) * Math.sin(phi);

                            center.add(x, y, z);
                            center.getWorld().spawnParticle(Particle.REDSTONE, center, 0);
                            center.subtract(x, y, z);
                        }
                    }
                    target.getWorld().getNearbyPlayers(target.getLocation(), 4, p -> p != target).forEach(p -> p.damage(isHeroic() ? 13D : 7D));
                }
            }.runTaskTimer(PrisonEvo.getInstance(), 20L, 5L);
        }
    }, FAST_ATTACKS = new BossSpell(20, 17) {

        private int taskId = 0;

        @Override
        public void process() {
            argus.getLogic()
                    .aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.NONE)
                    .setCurrentTarget(null);
            argus.teleportWithName(CENTER_FLYING);
            List<Player> targets = new ArrayList<>(getPlayersInvolved());
            targets.removeIf(player -> player.getGameMode() == GameMode.CREATIVE);
            if (targets.isEmpty()) {
                return;
            }
            Collections.shuffle(targets);
            AtomicInteger series = new AtomicInteger();
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(PrisonEvo.getInstance(), () -> {
                if (getPhase() != Phase.RUNNING) {
                    Bukkit.getScheduler().cancelTask(taskId);
                    return;
                }
                if (targets.isEmpty()) {
                    Bukkit.getScheduler().cancelTask(taskId);
                    return;
                }
                Player target;
                if (series.incrementAndGet() == 10) {
                    target = targets.remove(0);
                    series.set(0);
                    if (target.getWorld() == WORLD) {
                        argus.teleportWithName(target.getLocation());
                        argus.getLogic().attack(target);
                    }
                } else {
                    target = targets.get(0);
                }
                argus.getLogic().aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.MOST_RELEVANT);
                if (target.getWorld() == WORLD) {
                    argus.getLogic().setCurrentTarget(target);
                    argus.teleportWithName(target.getLocation());
                }
            }, 0L, 1L);
            sendMessagePrefixed(BOSS_NAME, "Конец настал.");
        }
    }, PRISON = new BossSpell(60, 50) {
        @Override
        public void process() {
            Location location = PRISON_LOCATIONS.get(ThreadLocalRandom.current().nextBoolean() ? 0 : 1);
            List<Player> players = new ArrayList<>(getPlayersInvolved());
            players.removeIf(player -> player.getGameMode() == GameMode.CREATIVE);
            if (players.isEmpty()) {
                return;
            }
            Player target = players.get(ThreadLocalRandom.current().nextInt(players.size()));
            target.teleport(location);
            sendMessagePrefixed(BOSS_NAME, "Я запру вас, как заперли однажды меня...");
            Bukkit.getScheduler().scheduleSyncDelayedTask(PrisonEvo.getInstance(), () -> {
                if (target.getWorld() == WORLD) {
                    target.teleport(CENTER_FLYING);
                }
            }, 20L * 10);
        }
    };

    private Argus argus;
    @Getter
    @Setter
    private int battlePhase;

    private long lastTpUse = 0;

    public ArgusScript() {
        super(1500, PLAYERS_TELEPORTATION_POINT);
        CENTER.getBlock().setType(Material.GOLD_BLOCK);
        Task.schedule(() -> {
            if (getPhase() != Phase.RUNNING)
                return;
            Collection<Player> players = getPlayersInvolved();
            if (players.isEmpty()) {
                switchPhase(Phase.WAITING);
                return;
            }
            if (battlePhase < 3 && players.size() < 3) {
                getPlayersInvolved().forEach(player -> player.damage(200));
            }
        }, 20L, 20L);
    }

    @Override
    public void startTheGame() {
        argus = new Argus(this, CENTER_FLYING);
        argus.spawnBoss();
        lastTpUse = 0L;
        battlePhase = 1;
        sendMessagePrefixed(BOSS_NAME, "Моя тюрьма станет вашей могилой, герои...");
        THROW_AWAY.cast(this);
        THROW_AWAY.setLastUsage(System.currentTimeMillis());
    }

    @Override
    public void endTheGame() {
        argus.despawnBoss();
        argus = null;
    }

    @Override
    public int getHeroicLevel() {
        return 80;
    }

    public void castTpSpell() {
        if (System.currentTimeMillis() - lastTpUse <= 5_000) {
            return;
        }
        Player target = argus.getLogic().getCurrentTarget();
        if (target != null && target.getWorld() == WORLD && target.getLocation().distance(argus.getLocation()) > 5) {
            argus.teleportWithName(target.getLocation());
            getPlayersInvolved().forEach(player -> player.damage(6D));
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
}
