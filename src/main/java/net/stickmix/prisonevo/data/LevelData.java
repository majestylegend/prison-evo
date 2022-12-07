package net.stickmix.prisonevo.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;
import net.stickmix.prisonevo.MainClass;
import net.stickmix.prisonevo.player.GamePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Predicate;

public class LevelData {

    private final static LevelInfo[] LEVELS = new LevelInfo[]{
            new LevelInfo(0, 0), //1
            new LevelInfo(1000, 1000),
            new LevelInfo(4000, 2000),
            new LevelInfo(9000, 4000),
            new LevelInfo(20000, 8000), //5
            new LevelInfo(35000, 12000),
            new LevelInfo(50000, 16000),
            new LevelInfo(75000, 20000),
            new LevelInfo(100000, 24000),
            new LevelInfo(150000, 28000), //10
            new LevelInfo(200000, 32000),
            new LevelInfo(275000, 36000),
            new LevelInfo(350000, 40000),
            new LevelInfo(400000, 44000),
            new LevelInfo(500000, 50000), //15
            new LevelInfo(600000, 55000),
            new LevelInfo(800000, 60000),
            new LevelInfo(1000000, 66000),
            new LevelInfo(1250000, 72000),
            new LevelInfo(1500000, 80000), //20
            new LevelInfo(2000000, 84000),
            new LevelInfo(2500000, 88000),
            new LevelInfo(5000000, 92000),
            new LevelInfo(10000000, 96000),
            new LevelInfo(15000000, 100000), //25
            new LevelInfo(25000000, 104000),
            new LevelInfo(40000000, 108000),
            new LevelInfo(55000000, 112000),
            new LevelInfo(70000000, 116000),
            new LevelInfo(100000000, 120000), //30
            new LevelInfo(150000000, 124000),
            new LevelInfo(200000000, 128000),
            new LevelInfo(300000000, 132000),
            new LevelInfo(400000000, 136000),
            new LevelInfo(500000000, 140000), //35
            new LevelInfo(750000000, 144000),
            new LevelInfo(1500000000, 148000),
            new LevelInfo(2500000000L, 152000),
            new LevelInfo(3500000000L, 156000),
            new LevelInfo(5000000000L, 160000), //40
            new LevelInfo(6500000000L, 164000),
            new LevelInfo(8000000000L, 168000),
            new LevelInfo(10000000000L, 172000),
            new LevelInfo(20000000000L, 176000),
            new LevelInfo(30000000000L, 180000), //45
            new LevelInfo(40000000000L, 184000),
            new LevelInfo(50000000000L, 188000),
            new LevelInfo(60000000000L, 192000),
            new LevelInfo(70000000000L, 196000),
            new LevelInfo(85000000000L, 200000),  //50
            new LevelInfo(100000000000L, 208000),
            new LevelInfo(125000000000L, 216000),
            new LevelInfo(150000000000L, 224000),
            new LevelInfo(200000000000L, 232000),
            new LevelInfo(250000000000L, 240000), //55
            new LevelInfo(300000000000L, 248000),
            new LevelInfo(350000000000L, 256000),
            new LevelInfo(400000000000L, 264000),
            new LevelInfo(450000000000L, 272000),
            new LevelInfo(500000000000L, 280000), //60
            new LevelInfo(600000000000L, 292000),
            new LevelInfo(700000000000L, 304000),
            new LevelInfo(800000000000L, 316000),
            new LevelInfo(900000000000L, 328000),
            new LevelInfo(1_000_000_000_000L, 340000), //65
            new LevelInfo(2_500_000_000_000L, 352000),
            new LevelInfo(4_000_000_000_000L, 364000),
            new LevelInfo(5_500_000_000_000L, 376000),
            new LevelInfo(7_000_000_000_000L, 388000),
            new LevelInfo(8_500_000_000_000L, 400000), //70
            new LevelInfo(10_000_000_000_000L, 412000),
            new LevelInfo(12_000_000_000_000L, 424000),
            new LevelInfo(14_000_000_000_000L, 436000),
            new LevelInfo(16_000_000_000_000L, 448000),
            new LevelInfo(18_000_000_000_000L, 460000), // 75
            new LevelInfo(20_000_000_000_000L, 472000),
            new LevelInfo(22_000_000_000_000L, 484000),
            new LevelInfo(24_000_000_000_000L, 496000),
            new LevelInfo(26_000_000_000_000L, 508000),
            new LevelInfo(30_000_000_000_000L, 520000), // 80
            new LevelInfo(34_000_000_000_000L, 532000),
            new LevelInfo(38_000_000_000_000L, 544000),
            new LevelInfo(42_000_000_000_000L, 556000),
            new LevelInfo(46_000_000_000_000L, 568000),
            new LevelInfo(50_000_000_000_000L, 580000), // 85
            new LevelInfo(54_000_000_000_000L, 600000),
            new LevelInfo(58_000_000_000_000L, 620000),
            new LevelInfo(62_000_000_000_000L, 640000),
            new LevelInfo(66_000_000_000_000L, 660000),
            new LevelInfo(70_000_000_000_000L, 680000), // 90
            new LevelInfo(74_000_000_000_000L, 700000),
            new LevelInfo(78_000_000_000_000L, 720000),
            new LevelInfo(82_000_000_000_000L, 740000),
            new LevelInfo(86_000_000_000_000L, 760000),
            new LevelInfo(90_000_000_000_000L, 780000), // 95
            new LevelInfo(94_000_000_000_000L, 800000),
            new LevelInfo(98_000_000_000_000L, 830000),
            new LevelInfo(102_000_000_000_000L, 860000),
            new LevelInfo(106_000_000_000_000L, 890000),
            new LevelInfo(110_000_000_000_000L, 1_500_000), // 100
    };

    public static String getColorizedLevel(int level) {
        if (level <= 30) {
            return "&e" + level;
        }
        if (level <= 60) {
            return "&a" + level;
        }
        if (level <= 95) {
            return "&c" + level;
        }
        return "&b&l" + level;
    }

    public static LevelInfo getNextLevelInfo(GamePlayer gamePlayer) {
        int current = gamePlayer.getLevel();
        if (current >= LEVELS.length)
            return null;
        return LEVELS[current];
    }

    public static long getNextLevelCost(GamePlayer gamePlayer) {
        int current = gamePlayer.getLevel();
        if (current >= LEVELS.length)
            return 0;
        return ModifiersData.getLongModifier().apply(LEVELS[current].getCost());
    }

    public static int getNextLevelBrokenBlocksRequired(GamePlayer gamePlayer) {
        int current = gamePlayer.getLevel();
        if (current >= LEVELS.length)
            return 0;
        return ModifiersData.getIntModifier().apply(LEVELS[current].getBlockBrokenRequired());
    }

    public static boolean checkPredicate(Player p, int level) {
        if (level > LEVELS.length || LEVELS[level - 1].getAdditional() == null)
            return true;
        return LEVELS[level - 1].getAdditional().test(MainClass.getInstance().getAthenaManager().get(p.getName()));
    }

    public static List<String> getAdditionalPredicateDescription(int level) {
        if (level > LEVELS.length)
            return null;
        return LEVELS[level - 1].getAdditionalDescription();
    }

    public static boolean isMaxLevel(GamePlayer player) {
        return player.getLevel() >= getMaxLevel();
    }

    public static int getMaxLevel() {
        return LEVELS.length;
    }

    public static double getPercentsToNextLevel(GamePlayer player) {
        if (isMaxLevel(player)) {
            return 0D;
        }
        long nextLevelCost = getNextLevelCost(player);
        if (nextLevelCost == 0) {
            return 0D;
        }
        int blocksRequired = getNextLevelBrokenBlocksRequired(player);
        if (blocksRequired == 0) {
            return 0D;
        }

        val moneyPercents = player.getBalance() * 50D / nextLevelCost;
        val blocksPercents = player.getBlocks() * 50D / blocksRequired;
        return Math.min(50D, moneyPercents) + Math.min(50D, blocksPercents);
    }

    public static String getFormattedPercents(GamePlayer player) {
        double percents = getPercentsToNextLevel(player);
        if (isMaxLevel(player)) {
            return "---";
        }
        return String.format("%.2f%%", percents);
    }

    @Data
    @AllArgsConstructor
    public static class LevelInfo {

        private final long cost;
        private final int blockBrokenRequired;
        private final Predicate<GamePlayer> additional;
        private final Predicate<GamePlayer> alternative;
        private final List<String> additionalDescription;

        public LevelInfo(long cost, int blockBrokenRequired) {
            this(cost, blockBrokenRequired, null, null, null);
        }

        public LevelInfo(long cost, int blockBrokenRequired, Predicate<GamePlayer> additional, List<String> additionalDescription) {
            this(cost, blockBrokenRequired, additional, null, additionalDescription);
        }
    }

}
