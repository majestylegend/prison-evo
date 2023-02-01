package net.stickmix.prisonevo.boss.aqua;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.boss.BossScript;
import net.stickmix.prisonevo.boss.BossSpell;
import net.stickmix.prisonevo.entity.logic.LogicFlag;
import net.stickmix.prisonevo.entity.logic.aggressive.AggressiveLogicFlag;
import net.stickmix.prisonevo.entity.movement.MovementManager;
import net.stickmix.prisonevo.utils.DamageHelper;
import net.stickmix.prisonevo.utils.SimplePotionEffect;
import net.villenium.os.util.Task;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class AquaLordScript extends BossScript {

    private final static World WORLD = Bukkit.getWorld("prison_boss3");
    public final static String BOSS_NAME = "&9&lШайн, Подводный Владыка";

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
            SPAWN_LOCATION = loc(25.5D, 69.0D, 56.5D, 0F, 0F),
            CENTER_FLYING = loc(25.5D, 83.0D, 56.5D, 0F, 0F),
            CENTER = loc(25.5, 67, 56.5),
            PLAYERS_TELEPORTATION_POINT = loc(25.5D, 69.0D, 84.5D, -176F, 0F);

    private final static List<Location> MOB_SPAWN_POS = Lists.newArrayList(
            loc(25.5D, 69.0D, 77.5D, -180F, 0F),
            loc(4.5D, 69.0D, 56.5D, -90F, 0F),
            loc(25.5D, 69.0D, 35.5D, 0F, 0F),
            loc(46.5D, 69.0D, 56.5D, 90F, 0F)
    );
    public final BossSpell POISON = new BossSpell(30, 22) {
        @Override
        public void process() {
            List<Player> players = new ArrayList<>(getPlayersInvolved());
            if (players.isEmpty())
                return;
            Player randy = players.get(ThreadLocalRandom.current().nextInt(players.size()));
            randy.addPotionEffect(new SimplePotionEffect(PotionEffectType.POISON, 2, 15));
            sendMessagePrefixed(BOSS_NAME, "Ты прокаженный, %s&c.", PrisonEvo.getInstance().getAthenaManager().get(randy).asUser().getFullDisplayName());
        }
    }, KICK_UP = new BossSpell(40, 30) {
        @Override
        public void process() {
            getPlayersInvolved().forEach(p -> p.setVelocity(new Vector(0D, 40D, 0D)));
            sendMessagePrefixed(BOSS_NAME, "Вы - последние из вашего рода. Ненадолго..");
        }
    }, CONFUSION = new BossSpell(45, 30) {
        @Override
        public void process() {
            PotionEffect pe = new SimplePotionEffect(PotionEffectType.CONFUSION, 1, 60);
            getPlayersInvolved().forEach(p -> {
                p.removePotionEffect(PotionEffectType.CONFUSION);
                p.addPotionEffect(pe);
                DamageHelper.clearDamage(p, 4D);
            });
            sendMessagePrefixed(BOSS_NAME, "Ваше время подошло к концу, смертные.");
        }
    }, SPAWN_SLAVES = new BossSpell(80, 60) {
        @Override
        public void process() {
            spawnSlaves();
        }
    }, SUMMON = new BossSpell(35, 23) {
        @Override
        public void process() {
            getPlayersInvolved().forEach(p -> p.setVelocity(king.getLocation().clone().subtract(p.getLocation()).toVector()));
            sendMessagePrefixed(BOSS_NAME, "Ко мне! Идите все ко мне, мои ненаглядные..");
        }
    };

    @Getter
    private AquaLord king;
    private final Set<AquaMob> slaves = new HashSet<>();

    @Getter
    @Setter
    private int battlePhase;
    private int slavesCall;

    public AquaLordScript() {
        super(1200, PLAYERS_TELEPORTATION_POINT);
        loc(25.5, 67, 56.5).getBlock().setType(Material.GOLD_BLOCK);
        Task.schedule(() -> {
            if (getPhase() != Phase.RUNNING) {
                return;
            }
            Collection<Player> players = getPlayersInvolved();
            if (players.isEmpty()) {
                switchPhase(Phase.WAITING);
            }
        }, 20L, 20L);
    }

    @Override
    public void startTheGame() {
        this.slavesCall = 0;
        this.battlePhase = 1;
        king = new AquaLord(this, CENTER_FLYING);
        king.spawnBoss();
        sendMessagePrefixed(BOSS_NAME, "Каковы ваши мотивы? Зачем вы заявились сюда? Ведь вы не добьетесь ничего, кроме собственного уничтожения.");
        spawnSlaves();
        SPAWN_SLAVES.setLastUsage(System.currentTimeMillis());
    }

    private void addSlave(Location loc) {
        playSound(Sound.ENTITY_GHAST_SCREAM);
        AquaMob slave = new AquaMob(this, loc, 1D + (this.slavesCall++ * 0.25D));
        slave.spawnBoss();
        slaves.add(slave);
    }

    @Override
    public void endTheGame() {
        new HashSet<>(slaves).forEach(AquaMob::despawnBoss);
        slaves.clear();
        king.despawnBoss();
        king = null;
    }

    @Override
    public int getHeroicLevel() {
        return 60;
    }

    public void spawnSlaves() {
        MOB_SPAWN_POS.forEach(this::addSlave);
        king.getLogic().aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.NONE)
                .logicFlag(LogicFlag.MOVEMENT_ALGORITHM, MovementManager.DONT_MOVE);
        king.teleportWithName(CENTER_FLYING);
        sendMessagePrefixed(BOSS_NAME, "Слуги мои, восстаньте! Восстаньте и идите, что есть вашей мощи!");
    }

    public boolean anySlavesAlive() {
        return !slaves.isEmpty();
    }

    public void onSlaveKilled(AquaMob slave) {
        slaves.remove(slave);
        if (!anySlavesAlive()) {
            sendMessagePrefixed(BOSS_NAME, "Вы лишь приближаете неотвратимость.");
            king.getLogic().aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.CLOSEST)
                    .logicFlag(LogicFlag.MOVEMENT_ALGORITHM, MovementManager.GO_THROUGH_EVERYTHING);
            if (king.getLogic().getBattlePhase() >= 2) {
                List<Player> players = new ArrayList<>(getPlayersInvolved());
                if (players.isEmpty()) {
                    return;
                }
                Player target = players.get(ThreadLocalRandom.current().nextInt(players.size()));
                king.getLogic().setCurrentTarget(target);
                king.teleportWithName(target.getLocation());
            }
        }
    }

    private final List<String> SELFKILLED_PHRASES = Lists.newArrayList(
            "Смерть приходит..",
            "Я освобожу этот мир от зла. И начну с вас.",
            "Смерть освободит вас от боли.",
            "Другого выхода нет.",
            "Ничто не вечно.. Кроме меня, разумеется.",
            "Мои слуги жертвуют собой ради высшего дела. Последуйте их примеру.",
            "Конец неизбежен.",
            "Ваша судьба стала обречена еще в тот момент, как вы вошли в эту залу."
    );

    public void onSlaveSelfkilled(AquaMob slave) {
        sendMessagePrefixed(BOSS_NAME, SELFKILLED_PHRASES.get(ThreadLocalRandom.current().nextInt(SELFKILLED_PHRASES.size())));
        getPlayersInvolved().forEach(p -> DamageHelper.clearDamage(p, 8D));
        onSlaveKilled(slave);
    }
}
