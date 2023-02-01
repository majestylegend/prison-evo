package net.stickmix.prisonevo.boss.blaze;

import com.google.common.collect.Lists;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.boss.BossScript;
import net.stickmix.prisonevo.boss.BossSpell;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BlazeKingScript extends BossScript {

    private final static World WORLD = Bukkit.getWorld("prison_boss2");
    public final static String BOSS_NAME = "&c&lПовелитель Огня";

    private static Location loc(double x, double y, double z, float yaw, float pitch) {
        Location loc = loc(x, y, z);
        loc.setYaw(yaw);
        loc.setPitch(pitch);
        return loc;
    }

    private static Location loc(double x, double y, double z) {
        return new Location(WORLD, x, y, z);
    }

    public final static Location SPAWN_LOCATION = loc(2.5D, 82D, 0.5D, -90F, 0F),
            CENTER = loc(2.5D, 80, 0.5D, -90F, 0F),
            PLAYERS_TELEPORTATION_POINT = loc(22.2D, 75.0D, 1.0D, -59.7F, 5.7F);

    private final static List<Location> CRYSTALS_POS = Lists.newArrayList(
            loc(19, 73, 33),
            loc(-30.4, 73, 25.7),
            loc(-19, 73, -32),
            loc(28, 73, -29)
    );

    private final static List<Location> BOSS_POS = Lists.newArrayList(
            loc(18D, 74.5D, 20.5D, 140F, -14.3F),
            loc(-25.7D, 75.5D, -1.2D, -77.4F, -8.6F),
            loc(2.4D, 76D, -28D, -14.5F, -1.5F)
    );

    private final static List<Location> TRANSPOSITION_POS = Lists.newArrayList(
            loc(-33.2D, 73D, -26.5D, -51.9F, 19.2F),
            loc(17.9, 73.0, -34.0, 48.1F, 4.9F),
            loc(29.5, 72.0, 20.8, 130.4F, 4.2F),
            loc(2.5D, 81.2D, 0.5D, 169.4F, 3.6F)
    );

    public final BossSpell
            TRANSPOSITION = new BossSpell(60, 40) {
        @Override
        public void process() {
            getPlayersInvolved().forEach(p -> p.teleport(TRANSPOSITION_POS.get(ThreadLocalRandom.current().nextInt(TRANSPOSITION_POS.size()))));
            sendMessagePrefixed(BOSS_NAME, "Добро пожаловать в мои владения! Здесь я решаю, кто и где находится!");
        }
    }, SET_ON_FIRE = new BossSpell(32, 25) {
        @Override
        public void process() {
            final int targets = 2;
            List<Player> list = new ArrayList<>(getPlayersInvolved());
            Set<Player> set = new HashSet<>();
            while (set.size() < Math.min(targets, list.size()))
                set.add(list.get(ThreadLocalRandom.current().nextInt(list.size())));
            set.forEach(p -> p.setFireTicks(60));
            sendMessagePrefixed(BOSS_NAME, "Кажется, здесь становится жарко!");
        }
    }, SET_ON_FIRE_GLOBAL = new BossSpell(30, 20) {
        @Override
        public void process() {
            List<Player> players = getPlayersInvolved();
            players.forEach(p -> {
                p.setFireTicks(80);
                p.damage(isHeroic() ? 7D : 4D);
            });
            sendMessagePrefixed(BOSS_NAME, "Вы все сгорите в тени моего пламени!!");
        }
    }, CHANGE_POSITION = new BossSpell(20, 15) {
        @Override
        public void process() {
            Location loc;
            do {
                loc = BOSS_POS.get(ThreadLocalRandom.current().nextInt(BOSS_POS.size()));
            } while (loc.distance(king.getLocation()) <= 3D);
            king.teleportWithName(loc);
            loc.getWorld().strikeLightningEffect(loc);
            sendMessagePrefixed(BOSS_NAME, "Ха! Не думал, что вы все еще здесь.");
        }
    }, BLINDNESS = new BossSpell(13, 13) {
        @Override
        public void process() {
            PotionEffect pe = new PotionEffect(PotionEffectType.BLINDNESS, 20 * 20, 5);
            getPlayersInvolved().forEach(p -> p.addPotionEffect(pe));
            sendMessagePrefixed(BOSS_NAME, "Вы не способны узреть моего истинного естества! Так сомкните же свои очи!");
        }
    }, CONFUSION = new BossSpell(24, 24) {
        @Override
        public void process() {
            PotionEffect pe = new PotionEffect(PotionEffectType.CONFUSION, 20 * 20, 8);
            getPlayersInvolved().forEach(p -> {
                p.addPotionEffect(pe);
                p.damage(isHeroic() ? 3D : 2D);
            });
            sendMessagePrefixed(BOSS_NAME, "Ваши попытки жалки, как и вы. На колени!");
        }
    };

    private BlazeKing king;
    private final Map<Location, ProtectingCrystal> crystals = new HashMap<>();

    public BlazeKingScript() {
        super(900, PLAYERS_TELEPORTATION_POINT);
        CENTER.getBlock().setType(Material.GOLD_BLOCK);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PrisonEvo.getInstance(), () -> {
            if (getPhase() != Phase.RUNNING) {
                return;
            }
            Collection<Player> players = getPlayersInvolved();
            if (players.isEmpty()) {
                switchPhase(Phase.WAITING);
                return;
            }
            if (king.getLogic().getPhase() < 3 && players.size() < 3) {
                getPlayersInvolved().forEach(player -> player.damage(200));
            }
        }, 20L, 20L);
    }

    @Override
    public void startTheGame() {
        king = new BlazeKing(this);
        king.spawnBoss();
        CRYSTALS_POS.forEach(this::addCrystal);
        sendMessagePrefixed(BOSS_NAME, "Что.. что это? Кто пробудил меня от векового сна!?");
    }

    private void addCrystal(Location loc) {
        loc.getWorld().strikeLightningEffect(loc);
        ProtectingCrystal crystal = new ProtectingCrystal(this, loc);
        crystal.spawnBoss();
        crystals.put(loc, crystal);
    }

    @Override
    public void endTheGame() {
        king.despawnBoss();
        king = null;
        new HashSet<>(crystals.values()).forEach(ProtectingCrystal::despawnBoss);
        crystals.clear();
    }

    @Override
    public int getHeroicLevel() {
        return 50;
    }

    public boolean anyCrystalsAlive() {
        return !crystals.isEmpty();
    }

    public void onCrystalKilled(Location location) {
        location.setYaw(0F);
        location.setPitch(0F);
        if (crystals.remove(location) != null) {
            if (!anyCrystalsAlive()) {
                sendMessagePrefixed(BOSS_NAME, "&4Огонь! Пламя и ярость! Они поглотят вас всех!!");
            } else {
                sendMessage("&e&lОдин из защитных кристаллов уничтожен!");
            }
        }
    }
}
