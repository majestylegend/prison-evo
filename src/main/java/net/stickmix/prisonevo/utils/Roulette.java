package net.stickmix.prisonevo.utils;

import com.google.common.collect.Lists;
import lombok.val;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.item.GameItemStack;
import net.stickmix.game.api.menu.MenuEmptyButton;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.data.GamePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Roulette {

    public Roulette(Player player) {
        List<RoulettePrize> prizes = new ArrayList<>();
        val random = ThreadLocalRandom.current();
        while (prizes.size() < 60) {
            val prize = RoulettePrize.VALUES[random.nextInt(RoulettePrize.VALUES.length)];
            for (int i = 0; i < prize.amount; i++) {
                prizes.add(prize);
            }
        }
        for (int i = 0; i < 2; i++) {
            Collections.shuffle(prizes);
        }

        List<Integer> slots = Lists.newArrayList(9, 10, 11, 12, 13, 14, 15, 16, 17);
        val menu = GameApi.getMenuUtil().create("Рулетка", 3);
        IntStream.of(8, 26).forEach(slot -> {
            menu.addItem(new MenuEmptyButton(new ItemStack(Material.STAINED_CLAY, 1, (short) 4)), slot);
        });
        for (int i = 0; i < 26; i++) {
            if (slots.contains(i) || i == 8) {
                continue;
            }
            menu.addItem(new MenuEmptyButton(new ItemStack(Material.STAINED_CLAY, 1, (short) 15)), i);
        }

        new BukkitRunnable() {

            int maxTicks = 1;
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                if (prizes.size() == 1) {
                    val gamePlayer = PrisonEvo.getInstance().getAthenaManager().get(player);
                    val prize = prizes.get(0);
                    prize.action.accept(gamePlayer);
                    gamePlayer.sendMessage("&aВы выиграли %s&a из рулетки!", prize.name);
                    player.closeInventory();
                    cancel();
                    return;
                }
                if (prizes.size() < 15) {
                    maxTicks = 2;
                }
                if (prizes.size() < 5) {
                    maxTicks = 3;
                }
                if (++ticks >= maxTicks) {
                    ticks = 0;
                    val lotteryPrizes = prizes.stream().limit(9).collect(Collectors.toList());
                    prizes.remove(0);
                    for (int i = 0; i < lotteryPrizes.size(); i++) {
                        menu.updateItemIcon(slots.get(i), null);
                        val prize = lotteryPrizes.get(i);
                        menu.updateItemIcon(slots.get(i), prize.icon);
                    }
                    menu.open(player);
                }
            }
        }.runTaskTimer(PrisonEvo.getInstance(), 0, 5L);

    }

    public enum RoulettePrize {

        SHARDS_I(Material.EMERALD, "&a10 шардов", 7, player -> {
            player.changeShards(10);
        }),
        SHARDS_II(Material.EMERALD, "&e25 шардов", 4, player -> {
            player.changeShards(25);
        }),
        SHARDS_III(Material.EMERALD, "&650 шардов", 3, player -> {
            player.changeShards(50);
        }),
        SHARDS_IV(Material.EMERALD, "&c100 шардов", 2, player -> {
            player.changeShards(100);
        }),
        SHARDS_V(Material.EMERALD, "&4&l250 шардов", 1, player -> {
            player.changeShards(250);
        }),
        FULL_INVENTORY_I(Material.LEAVES_2, "&aПолный инвентарь листвы", 7, player -> {
            for (int i = 0; i < 50; i++) {
                player.getHandle().getInventory().addItem(new ItemStack(Material.LEAVES_2, 64));
            }
        }),
        FULL_INVENTORY_II(Material.REDSTONE_BLOCK, "&eПолный инвентарь редстоуна", 4, player -> {
            for (int i = 0; i < 50; i++) {
                player.getHandle().getInventory().addItem(new ItemStack(Material.REDSTONE_BLOCK, 64));
            }
        }),
        FULL_INVENTORY_III(Material.ENDER_STONE, "&cПолный инвентарь эндерняка", 3, player -> {
            for (int i = 0; i < 50; i++) {
                player.getHandle().getInventory().addItem(new ItemStack(Material.ENDER_STONE, 64));
            }
        }),
        FULL_INVENTORY_IV(Material.SOUL_SAND, "&4&lПолный инвентарь песка душ", 2, player -> {
            for (int i = 0; i < 50; i++) {
                player.getHandle().getInventory().addItem(new ItemStack(Material.SOUL_SAND, 64));
            }
        }),
        MONEY_I(Material.REDSTONE, "&a250.000$", 7, player -> {
            player.changeBalance(250000);
        }),
        MONEY_II(Material.REDSTONE, "&e25.000.000$", 5, player -> {
            player.changeBalance(25_000_000);
        }),
        MONEY_III(Material.REDSTONE, "&6500.000.000$", 4, player -> {
            player.changeBalance(500_000_000);
        }),
        MONEY_IV(Material.REDSTONE, "&c1.000.000.000$", 2, player -> {
            player.changeBalance(1_000_000_000L);
        }),
        MONEY_V(Material.REDSTONE, "&4&l500.000.000.000$", 1, player -> {
            player.changeBalance(500_000_000_000L);
        });
//        BOOST_DOUBLE_ITEMS_25(Material.CHEST, "&eБустер \"+25%% к вероятности удвоенного лута\"", 2, player -> {
//            player.getBonuses().add(Bonus.DOUBLE_ITEMS_25);
//        }),
//        BOOST_DOUBLE_ITEMS_50(Material.ENDER_CHEST, "&cБустер \"+50%% к вероятности удвоенного лута\"", 1, player -> {
//            player.getBonuses().add(Bonus.DOUBLE_ITEMS_50);
//        }),
//        BOOST_DOUBLE_BLOCKS_25(Material.STONE_PICKAXE, "&eБустер \"+25%% к вероятности выпадения дополнительного блока\"", 2, player -> {
//            player.getBonuses().add(Bonus.DOUBLE_BLOCK_25);
//        }),
//        BOOST_DOUBLE_BLOCKS_50(Material.DIAMOND_PICKAXE, "&cБустер \"+50%% к вероятности выпадения дополнительного блока\"", 1, player -> {
//            player.getBonuses().add(Bonus.DOUBLE_BLOCK_50);
//        }),
//        BOOST_DOUBLE_SHARD_10(Material.GOLD_INGOT, "&eБустер \"+10%% к вероятности выпадения шарда\"", 2, player -> {
//            player.getBonuses().add(Bonus.DOUBLE_SHARD_10);
//        }),
//        BOOST_DOUBLE_SHARD_20(Material.DIAMOND, "&cБустер \"+20%% к вероятности выпадения шарда\"", 1, player -> {
//            player.getBonuses().add(Bonus.DOUBLE_SHARD_20);
//        }),
//        ACCESS_TO_FOUR_UPPER_SHAFTS(Material.DIAMOND_BLOCK, "&eБустер \"Доступ к шахтам на 4 уровня выше\"", 1, player -> {
//            player.getBonuses().add(Bonus.ACCESS_TO_FOUR_UPPER_SHAFTS);
//        }),
//        ACCESS_TO_SIX_UPPER_SHAFTS(Material.EMERALD_BLOCK, "&cБустер \"Доступ к шахтам на 6 уровней выше\"", 1, player -> {
//            player.getBonuses().add(Bonus.ACCESS_TO_SIX_UPPER_SHAFTS);
//        });

        public static RoulettePrize[] VALUES = values();

        private final ItemStack icon;
        private final String name;
        private final int amount;
        private final Consumer<GamePlayer> action;

        RoulettePrize(Material material, String name, int amount, Consumer<GamePlayer> action) {
            this.name = name;
            this.icon = new GameItemStack(material, name);
            this.amount = amount;
            this.action = action;
        }
    }
}
