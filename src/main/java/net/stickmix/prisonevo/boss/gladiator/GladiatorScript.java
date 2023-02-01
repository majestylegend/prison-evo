package net.stickmix.prisonevo.boss.gladiator;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.Title;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.boss.BossScript;
import net.stickmix.prisonevo.boss.BossSpell;
import net.stickmix.prisonevo.entity.logic.aggressive.AggressiveLogicFlag;
import net.villenium.os.util.AlgoUtil;
import net.villenium.os.util.Task;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GladiatorScript extends BossScript {

    private final static World WORLD = Bukkit.getWorld("prison_boss4");
    public final static String BOSS_NAME = "&6&lДиаваль, Последний Гладиатор";

    private static Location loc(double x, double y, double z, float yaw, float pitch) {
        Location loc = loc(x, y, z);
        loc.setYaw(yaw);
        loc.setPitch(pitch);
        return loc;
    }

    private static Location loc(double x, double y, double z) {
        return new Location(WORLD, x, y, z);
    }

    public final static Location
            CENTER = loc(0.5D, 56.0D, 0.5D, 0F, 0F);
    private final static Location CENTER_FLYING = loc(0.5D, 57.0D, 0.5D, 0F, 0F);
    private final static Location PLAYERS_TELEPORTATION_POINT = loc(0.4D, 65.0D, -38.8D, -0.3F, 10.1F);

    public final BossSpell
            SPAWN_COPIES = new BossSpell(50, 50) {
        @Override
        public void process() {
            getPlayersInvolved().forEach(p -> p.damage(isHeroic() ? 8D : 6D + p.getMaxHealth() - p.getHealth()));
            sendMessagePrefixed(BOSS_NAME, "Поражение вам обеспечено.");
        }
    }, BLOCK_KILL = new BossSpell(45, 45) {

        private final List<KillableMaterial> MATERIALS = Lists.newArrayList(
                new KillableMaterial(Material.WOOD, "доски"),
                new KillableMaterial(Material.SANDSTONE, "песчаник"),
                new KillableMaterial(Material.GRASS, "траву"),
                new KillableMaterial(Material.BONE_BLOCK, "костяной блок"),
                new KillableMaterial(Material.LEAVES, "листья")
        );

        @Override
        public void process() {
            KillableMaterial selected = MATERIALS.get(AlgoUtil.r(MATERIALS.size()));
            getPlayersInvolved().forEach(p -> {
                GameApi.getTitleManager().sendTitle(p, Title.TitleType.TITLE, "&e&lВстань на %s", selected.name);
                GameApi.getTitleManager().sendTitle(p, Title.TitleType.SUBTITLE, "&c..или ты умрешь через 7 секунд");
            });
            Task.schedule(() -> getPlayersInvolved().stream().filter(p -> {
                if (getPhase() != Phase.RUNNING) {
                    return false;
                }
                Location loc = p.getLocation();
                if (loc.getY() < 5) {
                    return true;
                }
                return loc.getBlock().getRelative(BlockFace.DOWN).getType() != selected.material;
            }).forEach(p -> {
                p.getWorld().strikeLightningEffect(p.getLocation());
                p.damage(200);
            }), 140L);
            sendMessagePrefixed(BOSS_NAME, "И вот, игра подходит к концу..");
        }

        @RequiredArgsConstructor
        class KillableMaterial {

            private final Material material;
            private final String name;

        }

    }, THROW_AWAY = new BossSpell(30, 20) {
        @Override
        public void process() {
            Vector velocity = new Vector(0D, 2D, 0D);
            getPlayersInvolved().forEach(p -> p.setVelocity(velocity));
            Task.schedule(() -> getPlayersInvolved().forEach(p -> p.setVelocity(CENTER.clone().subtract(p.getLocation()).toVector().setY(0D))), 20L);
            sendMessagePrefixed(BOSS_NAME, "Моя мощь не-пре-о-до-лима!!");
        }
    }, FAST_ATTACKS = new BossSpell(18, 13) {

        private int taskId = 0;

        @Override
        public void process() {
            gladiator.getLogic()
                    .aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.NONE)
                    .setCurrentTarget(null);
            gladiator.teleportWithName(CENTER_FLYING);
            List<Player> targets = new ArrayList<>(getPlayersInvolved());
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
                if (series.incrementAndGet() == 6) {
                    target = targets.remove(0);
                    series.set(0);
                    gladiator.teleportWithName(target.getLocation());
                    target.damage(gladiator.getLogic().getDamage() + 3D);
                } else {
                    target = targets.get(0);
                }
                gladiator.getLogic().aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.MOST_DAMAGE);
                if (target.getWorld() == WORLD) {
                    gladiator.getLogic().setCurrentTarget(target);
                }
            }, 0L, 1L);
            sendMessagePrefixed(BOSS_NAME, "Конец настал.");
        }
    }, CHANGE_STRATEGY = new BossSpell(20, 15) {
        @Override
        public void process() {
            if (gladiator.getLogic().isFindingMostRelevantTargets()) {
                gladiator.getLogic().aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.CLOSEST);
                sendMessagePrefixed(BOSS_NAME, "Глупые смертные. Все закончится здесь. Это - ваша последняя битва.");
            } else {
                gladiator.getLogic().aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.MOST_RELEVANT);
                sendMessagePrefixed(BOSS_NAME, "Ваши сердца.. мне отвратительно их неустанное биение. Я заглушу их стук, как заглушил стук своего.");
            }
        }
    };

    private Gladiator gladiator;
    @Getter
    @Setter
    private int battlePhase;
    private long lastTpUse;

    public GladiatorScript() {
        super(1200, PLAYERS_TELEPORTATION_POINT);
        CENTER.getBlock().setType(Material.GOLD_BLOCK);
        Task.schedule(() -> {
            if (getPhase() != Phase.RUNNING)
                return;
            Collection<Player> players = getPlayersInvolved();
            if (players.isEmpty()) {
                switchPhase(Phase.WAITING);
            }
            if (battlePhase < 3 && players.size() < 3) {
                getPlayersInvolved().forEach(player -> player.damage(200));
            }
        }, 20L, 20L);
    }

    @Override
    public void startTheGame() {
        gladiator = new Gladiator(this, CENTER_FLYING);
        gladiator.spawnBoss();
        battlePhase = 1;
        lastTpUse = 0;
        sendMessagePrefixed(BOSS_NAME, "Вы блуждаете в потьмах. Здесь нет света, нет жалости. На этой арене простились с жизнью и куда более великие герои, чем вы.");
    }

    @Override
    public void endTheGame() {
        gladiator.despawnBoss();
        gladiator = null;
    }

    @Override
    public int getHeroicLevel() {
        return 70;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL && e.getEntity().getWorld() == CENTER.getWorld()) {
            e.setCancelled(true);
        }
    }

    public void castTpSpell() {
        if (System.currentTimeMillis() - lastTpUse <= 5_000) {
            return;
        }
        Player target = gladiator.getLogic().getCurrentTarget();
        if (target != null && target.getWorld() == WORLD && target.getLocation().distance(gladiator.getLocation()) > 5) {
            gladiator.teleportWithName(target.getLocation());
            getPlayersInvolved().forEach(player -> player.damage(4D));
        }
        lastTpUse = System.currentTimeMillis();
    }
}
