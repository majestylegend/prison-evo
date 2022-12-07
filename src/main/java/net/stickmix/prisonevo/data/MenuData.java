package net.stickmix.prisonevo.data;

import com.google.common.collect.Lists;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.item.GameItemStack;
import net.stickmix.game.api.menu.Menu;
import net.stickmix.game.api.menu.MenuButton;
import net.stickmix.prisonevo.MainClass;
import net.stickmix.prisonevo.player.GamePlayer;
import net.stickmix.prisonevo.util.NumberUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class MenuData {
    public MenuData(Player player) {
        GamePlayer gamePlayer = MainClass.getInstance().getAthenaManager().get(player.getName());
        Menu menu = GameApi.getMenuUtil().create("Меню", 5);
        menu.addItem(new MenuButton(getLevelItem(gamePlayer)) {
            @Override
            public void onClick(Player player, ClickType clickType, int slot) {
                if (LevelData.isMaxLevel(gamePlayer)) {
                    gamePlayer.sendMessage("&eВы уже достигли максимального уровня!");
                    player.closeInventory();
                    return;
                }

                long requiredMoney = LevelData.getNextLevelCost(gamePlayer);
                int blocksRequired = LevelData.getNextLevelBrokenBlocksRequired(gamePlayer);

                boolean canUp = gamePlayer.getBalance() >= requiredMoney && gamePlayer.getBlocks() >= blocksRequired;

                if (canUp) {
                    boolean predicate = LevelData.checkPredicate(player, gamePlayer.getLevel() + 1);
                    if (!predicate) {
                        LevelData.LevelInfo info = LevelData.getNextLevelInfo(gamePlayer);
                        if (info != null) {
                            Predicate<GamePlayer> alternative = info.getAlternative();
                            if (alternative != null) {
                                predicate = alternative.test(gamePlayer);
                            }
                        }
                    }
                    if (!predicate) {
                        gamePlayer.sendMessage("&cВ данный момент вы не можете перейти на следующий уровень.");
                        return;
                    }
                    gamePlayer.changeBalance(-requiredMoney);
                    gamePlayer.changeLevel(1);
                    gamePlayer.sendMessage("&aВы достигли &b%d &aуровня! Поздравляем!", gamePlayer.getLevel());
                    gamePlayer.getHandle().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    player.closeInventory();
                    return;
                }
                gamePlayer.sendMessage("&cВ данный момент вы не можете перейти на следующий уровень.");
            }
        }, 2, 3);
        menu.open(gamePlayer.getHandle());
    }

    private ItemStack getLevelItem(GamePlayer gamePlayer) {
        if (LevelData.isMaxLevel(gamePlayer)) {
            return new GameItemStack(Material.EXP_BOTTLE, "&c&lПовышение уровня более недоступно", Lists.newArrayList(
                    "&7Вы прокачали уровень до максимума.",
                    "&7Дождитесь следующего обновления."
            ));
        }
        int blocks = LevelData.getNextLevelBrokenBlocksRequired(gamePlayer);
        long money = LevelData.getNextLevelCost(gamePlayer);

        boolean blocksBroken = gamePlayer.getBlocks() >= blocks;
        boolean moneyNeed = gamePlayer.getBalance() >= money;

        List<String> lore = new ArrayList<>();
        lore.add(String.format("&7Разрушено блоков: %s%s&f/&a%d", blocksBroken ? "&a" : "&c", gamePlayer.getBlocks(), blocks));
        lore.add(String.format("&7Стоимость повышения уровня: %s%s&f/&a%s", moneyNeed ? "&a" : "&c", NumberUtil.formatMoneyHardly(gamePlayer.getBalance()), NumberUtil.formatMoney(money)));
        List<String> description = LevelData.getAdditionalPredicateDescription(gamePlayer.getLevel() + 1);
        if (description != null) {
            lore.add("");
            lore.addAll(description);
        }
        return new GameItemStack(Material.EXP_BOTTLE, "Повышение уровня до &b" + (gamePlayer.getLevel() + 1), lore);
    }
}
