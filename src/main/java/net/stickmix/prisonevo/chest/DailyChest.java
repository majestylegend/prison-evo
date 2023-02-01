package net.stickmix.prisonevo.chest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.val;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.athena.annotation.Id;
import net.stickmix.game.api.phantom.entity.PhantomHologram;
import net.stickmix.prisonevo.data.GamePlayer;
import net.stickmix.prisonevo.utils.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class DailyChest {

    private static final long COOLDOWN = TimeUnit.DAYS.toMillis(1);
    private static final Location HOLO_LOCATION = new Location(Bukkit.getWorld("world_spawn"), 17002, 135, 2966);
    private static final Map<Integer, Awards> AWARDS = new HashMap<>();

    static {
        AWARDS.put(1, new Awards(
                new Award(1, 7),
                new Award(5_000, 30_000)
        ));
        AWARDS.put(10, new Awards(
                new Award(7, 15),
                new Award(100_000, 500_000)
        ));
        AWARDS.put(20, new Awards(
                new Award(10, 25),
                new Award(15_000_000, 75_000_000)
        ));
        AWARDS.put(30, new Awards(
                new Award(20, 35),
                new Award(500_000_000, 1_000_000_000)
        ));
        AWARDS.put(40, new Awards(
                new Award(30, 45),
                new Award(10_000_000_000L, 30_000_000_000L)
        ));
        AWARDS.put(50, new Awards(
                new Award(40, 55),
                new Award(40_000_000_000L, 70_000_000_000L)
        ));
        AWARDS.put(60, new Awards(
                new Award(50, 65),
                new Award(70_000_000_000L, 100_000_000_000L)
        ));
        AWARDS.put(70, new Awards(
                new Award(60, 75),
                new Award(100_000_000_000L, 200_000_000_000L)
        ));
        AWARDS.put(80, new Awards(
                new Award(70, 85),
                new Award(200_000_000_000L, 500_000_000_000L)
        ));
        AWARDS.put(90, new Awards(
                new Award(80, 95),
                new Award(1500_000_000_000L, 4_000_000_000_000L)
        ));
        AWARDS.put(100, new Awards(
                new Award(90, 105),
                new Award(2_000_000_000_000L, 7_500_000_000_000L)
        ));
    }

    @Id
    private final String owner;
    @Getter
    private long time;
    @Getter
    private PhantomHologram hologram;

    public boolean isAvailable() {
        return System.currentTimeMillis() > time;
    }

    public long getRemainingTime() {
        return time - System.currentTimeMillis();
    }

    public void takeAward() {
        val gamePlayer = GamePlayer.wrap(owner);

        AWARDS.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
                .filter(entry -> gamePlayer.getLevel() >= entry.getKey())
                .findFirst()
                .ifPresent(entry -> {
                    val awards = entry.getValue();
                    val random = ThreadLocalRandom.current();
                    val current = random.nextInt(1, 3);
                    switch (current) {
                        case 1: {
                            val shardAward = awards.getShardAward();
                            val value = random.nextLong(shardAward.min, shardAward.max);
                            gamePlayer.changeShards((int) value);
                            break;
                        }
                        case 2: {
                            val moneyAward = awards.getMoneyAward();
                            val value = random.nextLong(moneyAward.min, moneyAward.max);
                            gamePlayer.changeBalance(value);
                            gamePlayer.sendMessage("&aВы получили " + NumberUtil.formatMoney(value) + " из ежедневного сундука.");
                            break;
                        }
                    }
                });
        time = System.currentTimeMillis() + COOLDOWN;
        updateHologram();
    }

    public void updateHologram() {
        val player = Bukkit.getPlayerExact(owner);
        if (player == null || !player.isOnline()) {
            return;
        }
        String message = isAvailable() ? "§aДоступна награда" : "§cНаграда получена";
        if (hologram == null) {
            hologram = GameApi.getPhantomEntityFactory().createHologram(message);
            hologram.setLocation(HOLO_LOCATION);
            hologram.spawn(false);
            hologram.show(player);
            return;
        }
        if (!hologram.isVisibleFor(player)) {
            hologram.show(player);
        }
        hologram.setText(message);
    }

    @Data
    private static class Awards {
        private final Award shardAward;
        private final Award moneyAward;
    }

    @Data
    private static class Award {
        private final long min;
        private final long max;
    }

}
