package net.stickmix.prisonevo;

import com.google.common.collect.Lists;
import lombok.val;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.Title;
import net.stickmix.game.api.item.GameItemStack;
import net.stickmix.game.api.menu.Menu;
import net.stickmix.game.api.menu.MenuButton;
import net.stickmix.game.api.menu.MenuEmptyButton;
import net.stickmix.game.api.util.ChatUtil;
import net.stickmix.prisonevo.boss.BossManager;
import net.stickmix.prisonevo.boss.BossScript;
import net.stickmix.prisonevo.boss.aqua.AquaLordScript;
import net.stickmix.prisonevo.boss.argus.ArgusScript;
import net.stickmix.prisonevo.boss.balnazzar.BalnazzarScript;
import net.stickmix.prisonevo.boss.blaze.BlazeKingScript;
import net.stickmix.prisonevo.boss.gladiator.GladiatorScript;
import net.stickmix.prisonevo.boss.keeper.ForestKeeperScript;
import net.stickmix.prisonevo.commands.SellAllCommand;
import net.stickmix.prisonevo.data.GamePlayer;
import net.stickmix.prisonevo.data.Perks;
import net.stickmix.prisonevo.items.EvoItem;
import net.stickmix.prisonevo.items.EvoItems;
import net.stickmix.prisonevo.items.EvoItemsManager;
import net.stickmix.prisonevo.utils.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MenuItemHandler {

    public MenuItemHandler(GamePlayer gamePlayer) {
        Menu menu = GameApi.getMenuUtil().create("PrisonEvo | Меню", 5);
        menu.addItem(new MenuButton(getLevelItem(gamePlayer)) {
            @Override
            public void onClick(Player player, ClickType clickType, int slot) {
                if (Level.isMaxLevel(gamePlayer)) {
                    gamePlayer.sendMessage("&eВы уже достигли максимального уровня!");
                    player.closeInventory();
                    return;
                }

                long requiredMoney = Level.getNextLevelCost(gamePlayer);
                int blocksRequired = Level.getNextLevelBrokenBlocksRequired(gamePlayer);

                boolean canUp = gamePlayer.getBalance() >= requiredMoney && gamePlayer.getBlocksBroken() >= blocksRequired;

                if (canUp) {
                    boolean predicate = Level.checkPredicate(player, gamePlayer.getLevel() + 1);
                    if (!predicate) {
                        Level.LevelInfo info = Level.getNextLevelInfo(gamePlayer);
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
                    player.closeInventory();
                    return;
                }
                gamePlayer.sendMessage("&cВ данный момент вы не можете перейти на следующий уровень.");
            }
        }, 2, 3);
        menu.addItem(new MenuButton(Material.STONE_PICKAXE, "Телепортация в шахту", Lists.newArrayList(
                "&7Чтобы попасть в шахту уровня",
                "&7меньше, воспользуйся командой",
                "&b/shaft <уровень шахты>"
        )) {
            @Override
            public void onClick(Player player, ClickType clickType, int slot) {
                player.performCommand("shaft");
            }
        }, 2, 5);
        menu.addItem(new MenuButton(Material.GOLD_INGOT, "Магазин предметов", Lists.newArrayList(
                "&7Нажми, чтобы открыть меню магазина."
        )) {
            @Override
            public void onClick(Player player, ClickType clickType, int slot) {
                Menu shop = GameApi.getMenuUtil().create("PrisonEvo | Магазин предметов", 5);
                EvoItems items = gamePlayer.getEvoItems();
                List<Integer> itemIds = new ArrayList<>(items.getItemsToBeDisplayed());
                itemIds.sort(Integer::compare);
                int pos = 1;
                for (int id : itemIds) {
                    EvoItem item = EvoItemsManager.getItem(id);
                    val builder = ItemBuilder.fromItem(item.getItem())
                            .withItemMeta()
                            .addBlankLore();

                    long price = Modifiers.getLongModifier().apply(item.getPrice());
                    if (price > gamePlayer.getBalance()) {
                        builder.addLore(String.format("&cЦена: %s", NumberUtil.formatMoney(price)));
                    } else {
                        builder.addLore(String.format("&aКупить за %s", NumberUtil.formatMoney(price)));
                    }
                    ItemStack build = builder.and().build();
                    shop.addItem(new MenuButton(build) {
                        @Override
                        public void onClick(Player player, ClickType clickType, int slot) {
                            if (price > gamePlayer.getBalance()) {
                                gamePlayer.sendMessage("&cУ вас недостаточно денег для покупки данного предмета.");
                                return;
                            }
                            int ps = item.getPrevious();
                            if (ps != 0)
                                player.getInventory().remove(EvoItemsManager.getItem(ps).getItem().getType());
                            ItemStack i = item.getItem();
                            if (UtilItem.isHelmet(i)) {
                                player.getInventory().setHelmet(i);
                            } else if (UtilItem.isChestplate(i)) {
                                player.getInventory().setChestplate(i);
                            } else if (UtilItem.isLeggings(i)) {
                                player.getInventory().setLeggings(i);
                            } else if (UtilItem.isBoots(i)) {
                                player.getInventory().setBoots(i);
                            } else if (!player.getInventory().addItem(i).isEmpty()) {
                                gamePlayer.sendMessage("&cУ вас недостаточно места в инвентаре.");
                                return;
                            }
                            gamePlayer.changeBalance(-price);
                            items.onItemBought(item);
                            if (item.getId() == 150) {
                                Bukkit.broadcastMessage(ChatUtil.prefixed(
                                        "&c&lPrisonEvo", "%s &c&lзавладел &6&lИспепелителем&c&l!",
                                        gamePlayer.asUser().getFullDisplayName()
                                ));
                                Title util = GameApi.getTitleManager();
                                util.sendTitle(player, Title.TitleType.TITLE, BalnazzarScript.BOSS_NAME);
                                util.sendTitle(player, Title.TitleType.SUBTITLE, "&cЯ чувствую твою силу, насекомое..");
                                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 9));
                                player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 1F, 1F);
                            }
                            player.closeInventory();
                            gamePlayer.sendMessage("&aВы купили новый предмет: %s&a :)", item.getItem().getItemMeta().getDisplayName());
                        }
                    }, 2, pos++);
                }
                int shardsLine = 4;
                int shardsIndex = 1;
                backpacks:
                {
                    int nextLevel = gamePlayer.getBackpackLevel() + 1;
                    if (!Backpack.isValid(nextLevel))
                        break backpacks;
                    int size = Backpack.getSize(nextLevel), price = Backpack.getPrice(nextLevel);
                    String priceString = gamePlayer.getShards() >= price ? "&aКупить за " + price + " шардов" : "&cЦена: " + price + " шардов";
                    String chestName = ChatUtil.colorize("&5&lРюкзак (Ур. %d)", nextLevel);
                    shop.addItem(new MenuButton(Material.ENDER_CHEST, chestName, Lists.newArrayList(
                            "&7Позволяет переносить до " + size + " ресурсов.",
                            "&7Очищается при перезаходе на сервер.",
                            "&7Вы также не можете самостоятельно",
                            "&7перекладывать предметы в/из рюкзака.",
                            "",
                            priceString
                    )) {
                        @Override
                        public void onClick(Player player, ClickType clickType, int slot) {
                            if (gamePlayer.getShards() >= price) {
                                gamePlayer.upgradeBackpack();
                                gamePlayer.sendMessage("&aВы купили новый предмет: %s&a :)", chestName);
                                gamePlayer.changeShards(-price);
                                player.closeInventory();
                            } else {
                                gamePlayer.sendMessage("&cУ вас недостаточно шардов для покупки данного предмета.");
                            }
                        }

                    }, shardsLine, shardsIndex++);
                }
                shardsIndex = addPerk(shop, shardsLine, shardsIndex, gamePlayer, Perks.Perk.MONEY_I, Perks.Perk.MONEY_IV);
                shardsIndex = addPerk(shop, shardsLine, shardsIndex, gamePlayer, Perks.Perk.DOUBLE_LOOT_I, Perks.Perk.DOUBLE_LOOT_II);
                shardsIndex = addPerk(shop, shardsLine, shardsIndex, gamePlayer, Perks.Perk.MORE_SHARDS_I, Perks.Perk.MORE_SHARDS_IV);
                shardsIndex = addPerk(shop, shardsLine, shardsIndex, gamePlayer, Perks.Perk.BOSS_DAMAGE_I, Perks.Perk.BOSS_DAMAGE_VI);
                shardsIndex = addPerk(shop, shardsLine, shardsIndex, gamePlayer, Perks.Perk.HEROIC_BOSSES, Perks.Perk.HEROIC_BOSSES);
                addPerk(shop, shardsLine, shardsIndex, gamePlayer, Perks.Perk.BOSS_CRIT_I, Perks.Perk.BOSS_CRIT_V);
                shop.open(player);
            }
        }, 2, 7);
        menu.addItem(new MenuButton(Material.ENDER_CHEST, "Открыть рюкзак", Lists.newArrayList(
                "&7Рюкзак позволяет переносить больше",
                "&7ресурсов, что, в свою очередь,",
                "&7позволяет меньше отвлекаться на продажу",
                "&7добытых ресурсов."
        )) {
            @Override
            public void onClick(Player player, ClickType clickType, int slot) {
                if (gamePlayer.getBackpackLevel() == 0) {
                    gamePlayer.sendMessage("&cВы еще не купили себе рюкзак.");
                    return;
                }
                player.openInventory(gamePlayer.getBackpack());
            }
        }, 4, 4);
        menu.addItem(new MenuButton(Material.SIGN, "Продать предметы", Lists.newArrayList(
                "&7Нажми, чтобы продать все",
                "&7добытые ресурсы. Для всех доступна",
                "&7команда &b/sellall&7, если на сервере",
                "&7присутствует игрок группы &e&lELITE&7."
        )) {
            @Override
            public void onClick(Player player, ClickType clickType, int slot) {
                SellAllCommand.sell(player);
                player.closeInventory();
            }
        }, 4, 6);
        menu.addItem(new MenuButton(Material.BOOK_AND_QUILL, "Помощь", Lists.newArrayList(
                "&7Здесь можно найти ответы",
                "&7на частозадаваемые вопросы",
                "&7по режиму, донату и прочему."
        )) {
            @Override
            public void onClick(Player player, ClickType clickType, int slot) {
                getFAQMenu().open(player);
            }
        }, 5, 9);
        menu.addItem(new MenuButton(Material.DIAMOND_HOE, "Боссы", Lists.newArrayList(
                "&7Менюшка с информацией по",
                "&7и возможностью переместиться",
                "&7к боссам этого сервера."
        )) {
            @Override
            public void onClick(Player player, ClickType clickType, int slot) {
                if (gamePlayer.getLevel() < 20 && !gamePlayer.asUser().getPermission().isAdministrator()) {
                    gamePlayer.sendMessage("&cПервый босс доступен лишь с &e20-го &cуровня. Тебе пока рановато :(");
                    return;
                }
                Menu bosses = GameApi.getMenuUtil().create("PrisonEvo | Боссы", 1);
                bosses.addItem(new BossItem(1, Material.WOOD_SWORD, ForestKeeperScript.BOSS_NAME, 20, Lists.newArrayList(
                        "&7Убийство этого босса является",
                        "&7необходимым условием для",
                        "&7достижения 30-го уровня."
                )));
                bosses.addItem(new BossItem(2, Material.BLAZE_ROD, BlazeKingScript.BOSS_NAME, 30, Lists.newArrayList(
                        "&7Убийство этого босса является",
                        "&7необходимым условием для",
                        "&7достижения 40-го уровня."
                )));
                bosses.addItem(new BossItem(3, Material.WATER_BUCKET, AquaLordScript.BOSS_NAME, 40, Lists.newArrayList(
                        "&7Убийство этого босса является",
                        "&7необходимым условием для",
                        "&7достижения 50-го уровня."
                )));
                bosses.addItem(new BossItem(4, Material.DIAMOND_SWORD, GladiatorScript.BOSS_NAME, 50, Lists.newArrayList(
                        "&7Убийство этого босса является",
                        "&7необходимым условием для",
                        "&7достижения 60-го уровня."
                )));
                bosses.addItem(new BossItem(5, Material.IRON_FENCE, ArgusScript.BOSS_NAME, 65, Lists.newArrayList(
                        "&7Убийство этого босса является",
                        "&7необходимым условием для",
                        "&7достижения 75-го уровня."
                )));
                List<String> balnazzarLore = Lists.newArrayList(
                        "&7Убийство этого босса является",
                        "&7необходимым условием для",
                        "&7достижения 85-го уровня."
                );
                if (!gamePlayer.getEvoItems().hasItem(150)) {
                    balnazzarLore.add("");
                    balnazzarLore.add("&cВы будете бесполезны при убийстве этого босса.");
                }
                bosses.addItem(new BossItem(6, Material.GOLD_SWORD, BalnazzarScript.BOSS_NAME, 80, balnazzarLore));
                bosses.open(player);
            }
        }, 3, 2);
        menu.addItem(new MenuButton(Material.DIAMOND_SWORD, "Телепортация на PvP арену", Lists.newArrayList(
                "&7Нажми, чтобы перенестись на",
                "&7арену, где игроки могут друг",
                "&7с другом сражаться."
        )) {

            @Override
            public void onClick(Player player, ClickType clickType, int slot) {
                player.closeInventory();
                Collection<Location> players = Bukkit.getWorld("prison_arena").getPlayers().stream()
                        .map(Player::getLocation)
                        .collect(Collectors.toSet());
                Location target = players.isEmpty()
                        ? pvpArena.get(ThreadLocalRandom.current().nextInt(pvpArena.size()))
                        : pvpArena.stream().max(Comparator.comparingDouble(l1 -> players.stream().mapToDouble(l2 -> l2.distance(l1)).min().getAsDouble())).get();
                TeleportManager.teleport(player, target);
            }
        }, 3, 8);

        menu.open(gamePlayer.getHandle());
    }

    private ItemStack getLevelItem(GamePlayer gamePlayer) {
        if (Level.isMaxLevel(gamePlayer)) {
            return new GameItemStack(Material.EXP_BOTTLE, "&c&lПовышение уровня более недоступно", Lists.newArrayList(
                    "&7Вы прокачали уровень до максимума.",
                    "&7Дождитесь следующего обновления."
            ));
        }
        int blocks = Level.getNextLevelBrokenBlocksRequired(gamePlayer);
        long money = Level.getNextLevelCost(gamePlayer);

        boolean blocksBroken = gamePlayer.getBlocksBroken() >= blocks;
        boolean moneyNeed = gamePlayer.getBalance() >= money;

        List<String> lore = new ArrayList<>();
        lore.add(String.format("&7Разрушено блоков: %s%s&f/&a%d", blocksBroken ? "&a" : "&c", gamePlayer.getBlocksBroken(), blocks));
        lore.add(String.format("&7Стоимость повышения уровня: %s%s&f/&a%s", moneyNeed ? "&a" : "&c", NumberUtil.formatMoneyHardly(gamePlayer.getBalance()), NumberUtil.formatMoney(money)));
        List<String> description = Level.getAdditionalPredicateDescription(gamePlayer.getLevel() + 1);
        if (description != null) {
            lore.add("");
            lore.addAll(description);
        }
        return new GameItemStack(Material.EXP_BOTTLE, "Повышение уровня до &b" + (gamePlayer.getLevel() + 1), lore);
    }

    private Menu getFAQMenu() {
        Menu faq = GameApi.getMenuUtil().create("PrisonEvo | Помощь", 5);
        Menu faq2 = GameApi.getMenuUtil().create("PrisonEvo | FAQ", 4);

        faq.addItem(new MenuButton(Material.BOOK, "FAQ по режиму", Lists.newArrayList(
                "&7В этом разделе можно найти",
                "&7всю необходимую по режиму информацию."
        )) {
            @Override
            public void onClick(Player player, ClickType clickType, int slot) {
                faq2.open(player);
            }

        }, 2, 3);
        faq.addItem(new MenuEmptyButton(Material.REDSTONE_COMPARATOR, "Изменение настроек игры!", Lists.newArrayList(
                "&7Чтобы исключить возможность сильных",
                "&7подлагиваний на этом сервере, пожалуйста,",
                "&7зайдите в настройки графики клиента и",
                "&7включите использование VBO (справа),",
                "&7отключите мягкое освещение (по крайней",
                "&7мере до тех пор, пока не выберетесь из",
                "&7шахт с листвой)."
        )), 2, 5);
        faq.addItem(new MenuButton(Material.ENCHANTED_BOOK, "Что по донату?", Lists.newArrayList(
                "&7Описание донат-возможностей."
        )) {
            @Override
            public void onClick(Player player, ClickType clickType, int slot) {
                getDonateMenu().open(player);
            }
        }, 2, 7);
        faq.addItem(new MenuEmptyButton(Material.NETHER_STAR, "&cРейты этого сервера", Lists.newArrayList(
                "&7Рейты (модификаторы) этого сервера PrisonEvo:",
                "&7Цена уровней: &ax1",
                "&7Кол-во блоков для уровней: &ax1",
                "&7Цена предметов в магазине: &ax1",
                "&7Цена продажи ресурсов: &ax1",
                "&7Редкость шардов: &ax1"
        )), 4, 5);
//        faq.addItem(new MenuEmptyButton(Material.DIAMOND_PICKAXE, "&cПеренос прогресса", Lists.newArrayList(
//                "&7Всю информацию можно посмотреть по команде",
//                "&b/transfer&7. Команда не переносит с первого раза,",
//                "&7там будет предупреждение!"
//        )), 4, 6);

        faq2.addItem(new MenuEmptyButton(Material.SKULL_ITEM, "Суть режима", Lists.newArrayList(
                "&7Основная цель режима - добывать",
                "&7ресурсы путем разрушения блоков,",
                "&7продавать эти ресурсы за $, а затем -",
                "&7покупать инструменты и доступы к новым",
                "&7местностям (шахтам), в которых встречаются",
                "&7значительно более дорогие ресурсы. Также",
                "&7можно (и нужно) покупать броню и оружие,",
                "&7т.к. есть PvP-зоны."
        )), 2, 2);
        faq2.addItem(new MenuEmptyButton(Material.IRON_PICKAXE, "Как устроены шахты?", Lists.newArrayList(
                "&7Новые шахты встречаются каждые 2 уровня,",
                "&7то есть с каждого нечетного уровня",
                "&7(с 1, 3, 5 и т.д.).",
                "&7При этом стиль шахты (обстановка)",
                "&7обычно меняется раз в 4 уровня",
                "&7(то есть с 1, 5, 9 и т.д. уровней).",
                "",
                "&7Телепортироваться на свою шахту можно",
                "&7с помощью команды &b/shaft&7. На шахту",
                "&7других уровней - &b/shaft <уровень>&7."
        )), 3, 3);
        faq2.addItem(new MenuEmptyButton(Material.IRON_SWORD, "Подробнее о PvP", Lists.newArrayList(
                "&7Все шахты по 20 уровень включительно",
                "&7находятся под защитой, и на них вы не",
                "&7можете умереть от рук других игроков.",
                "&7Тем не менее, на местностях выше 20го",
                "&7уровня, у боссов и на PvP-арене вас могут",
                "&7убить, вследствие чего вы потеряете все",
                "&7предметы, кроме &a&lМеню &7и тех, на которых",
                "&7написано, что их потерять нельзя.",
                "&7Также при смерти в PvP вы потеряете 1% от",
                "&7минимума из суммы, которая у вас есть на руках",
                "&7и суммы, необходимой для получения следующего уровня."
        )), 2, 4);
        faq2.addItem(new MenuEmptyButton(Material.GRILLED_PORK, "Некоторая общая информация", Lists.newArrayList(
                "&7Игроки не подвержены жажде или голоду,",
                "&7вы также не можете выбрасывать предметы,",
                "&7помеченные невыпадаемыми, и &a&lМеню&7.",
                "&7Для телепортации на спавн может быть",
                "&7использована команда &b/spawn&7."
        )), 3, 5);
        faq2.addItem(new MenuEmptyButton(Material.EMERALD, "Экономика ($ и шарды)", Lists.newArrayList(
                "&7На сервере есть 2 валюты: это $ и шарды.",
                "&7Первые можно получать за продажу ресурсов,",
                "&7шарды же встречаются случайно при ломании",
                "&7блоков. И то, и другое можно потратить в",
                "&7магазине (золотой слиток в &a&lМеню&7):",
                "&7за $ можно покупать и улучшать снаряжение,",
                "&7за шарды - &5&lРюкзак&7 и перки.",
                "&7Также $ необходимы для повышения уровня и",
                "&7их можно передавать с помощью команды &b/pay&7."
        )), 2, 6);
        faq2.addItem(new MenuEmptyButton(Material.DIAMOND_SWORD, "Как же Боссы?", Lists.newArrayList(
                "&7Начиная с &e20-го &7уровня, у вас открывается",
                "&7доступ к локациям боссов. У каждого из них",
                "&7уникальная механика боя, что делает схватку с ними",
                "&7действительно необычной.",
                "&7С боссов не падает много особо полезных вещей,",
                "&7максимум - немного шардов (и, возможно, $$).",
                "&7Почему же они нужны, спросите вы? Потому что они",
                "&7являются необходимым условием для получения",
                "&7определенных уровней. Так, например, нельзя получить",
                "&e35-й &7уровень, не одолев самого первого босса",
                "&7непосредственно перед получением уровня.",
                "&7Каждый босс имеет свою героическую версию:",
                "&7значительно более сильнее, чем обычная, - и за которую",
                "&7положены куда более ценные награды."
        )), 3, 7);
        faq2.addItem(new MenuEmptyButton(Material.BARRIER, "Баги, ошибки?!", Lists.newArrayList(
                "&7Стоит отметить, что режим находится",
                "&7на стадии &c&lBETA&7-тестирования, а значит,",
                "&7на нем могут всплывать некоторые приятные",
                "&7(и не очень) ошибки, о которых, мы надеемся,",
                "&7вы нам сразу же сообщите."
        )), 2, 8);
        return faq;
    }

    private Menu getDonateMenu() {
        Menu donate = GameApi.getMenuUtil().create("PrisonEvo | Донат", 5);
//        donate.addItem(new MenuEmptyButton(Material.EMERALD, "Где это все найти?", Lists.newArrayList(
//                "&7Помимо указанного здесь, можно",
//                "&7на PrisonEvo можно купить еще много",
//                "&7чего интересного, и тем самым помочь в",
//                "&7развитии проекту. Полный список можно",
//                "&7найти там же, где и купить все",
//                "&7перечисленные предметы:",
//                "&7на &bhttps://qubas.net&7.",
//                "",
//                "&fЕсли при покупке предмета вы находились",
//                "&fв игре, перезайдите, и он у вас появится."
//        )), 5, 5);
        donate.addItem(new MenuEmptyButton(Material.INK_SACK, 10, "Бонусы групп: &a&lVIP&a, &a&lVIP+", Lists.newArrayList(
                "&8- &fЦена уровней снижена на &b0.1/0.25%&f для всех",
                "&8- &fКол-во блоков для уровней снижено на &b0.1/0.25%&f для всех"
        )), 2, 4);
        donate.addItem(new MenuEmptyButton(Material.INK_SACK, 2, "Бонусы групп: &2&lPREMIUM&a, &2&lPREMIUM+", Lists.newArrayList(
                "&8- &fЦена уровней снижена на &b0.5/1%&f для всех",
                "&8- &fКол-во блоков для уровней снижено на &b0.5/1%&f для всех",
                "&8- &fВремя телепортации снижено до девяти/восьми секунд для всех"
        )), 2, 6);
        donate.addItem(new MenuEmptyButton(Material.INK_SACK, 11, "Бонусы групп: &e&lELITE&a, &e&lELITE+", Lists.newArrayList(
                "&8- &fЦена уровней снижена на &b1.75/2.5%&f для всех",
                "&8- &fКол-во блоков для уровней снижено на &b1.75/2.5%&f для всех",
                "&8- &fВремя телепортации снижено до шести секунд для всех",
                "&8- &fКоманда &b/sellall&f доступна для всех"
        )), 4, 4);
        donate.addItem(new MenuEmptyButton(Material.INK_SACK, 14, "Преимущества групп: &6&lSPONSOR&a, &6&lSPONSOR+", Lists.newArrayList(
                "&8- &fЦена уровней снижена на &b3.5/5%&f для всех",
                "&8- &fКол-во блоков для уровней снижено на &b3.5/5%&f для всех",
                "&8- &fВремя телепортации снижено до четырех секунд для всех",
                "&8- &fКоманда &b/sellall&f доступна для всех",
                "&8- &b+10%&f к дропу денег и шардов с боссов для всех"
        )), 4, 6);
        donate.addItem(new MenuEmptyButton(Material.INK_SACK, 6, "Преимущества групп: &b&lLUXURY&a, &b&lLUXURY+&a, &b&lLUXURY++", Lists.newArrayList(
                "&8- &fЦена уровней снижена на &b7.5/12.5/20%&f для всех",
                "&8- &fКол-во блоков для уровней снижено на &b7.5/12.5/20%&f для всех",
                "&8- &fВремя телепортации снижено до одной секунды для всех",
                "&8- &fКоманда &b/sellall&f доступна для всех",
                "&8- &b+25%&f к дропу денег и шардов с боссов для всех",
                "&8- &fДоступ к предсмертному сообщению (/dm)"
        )), 3, 5);
        return donate;
    }

    private static int addPerk(Menu inv, int line, int slot, GamePlayer player, Perks.Perk start, Perks.Perk end) {
        Perks perks = player.getPerks();
        if (perks.hasPerk(end)) {
            return slot;
        }
        int zero = start.ordinal();
        Perks.Perk next = end;
        for (int ordinal = end.ordinal() - 1; ordinal >= zero; --ordinal) {
            Perks.Perk perk = Perks.Perk.values()[ordinal];
            if (perks.hasPerk(perk)) {
                inv.addItem(new PerkItem(player, next, perk), line, slot++);
                return slot;
            }
            next = perk;
        }
        inv.addItem(new PerkItem(player, start), line, slot++);
        return slot;
    }

    private static class PerkItem extends MenuButton {

        private static ItemStack getIcon(GamePlayer player, Perks.Perk perk) {
            val builder = ItemBuilder.fromItem(perk.getIcon())
                    .withItemMeta()
                    .addBlankLore();
            if (player.getShards() >= perk.getCost()) {
                builder.addLore(String.format("&aКупить за %d шардов", perk.getCost()));
            } else {
                builder.addLore(String.format("&cЦена: %d шардов", perk.getCost()));
            }
            return builder.and().build();
        }

        private final Perks.Perk old;
        private final Perks.Perk perk;

        public PerkItem(GamePlayer player, Perks.Perk perk, Perks.Perk old) {
            super(getIcon(player, perk));
            this.perk = perk;
            this.old = old;
        }

        public PerkItem(GamePlayer player, Perks.Perk perk) {
            this(player, perk, null);
        }

        @Override
        public void onClick(Player player, ClickType clickType, int slot) {
            GamePlayer info = PrisonEvo.getInstance().getAthenaManager().get(player);
            Perks perks = info.getPerks();
            if (perks.hasPerk(this.perk))
                return;
            if (info.getShards() >= this.perk.getCost()) {
                info.changeShards(-this.perk.getCost());
                if (this.old != null)
                    perks.replacePerk(this.old, this.perk);
                else
                    perks.addPerk(this.perk);
                info.sendMessage("&aВы приобрели новый перк: %s&a :)", this.perk.getIcon().getItemMeta().getDisplayName());
                player.closeInventory();
            } else {
                info.sendMessage("&cУ вас недостаточно шардов для покупки данного предмета.");
            }
        }
    }

    private static class BossItem extends MenuButton {

        private static List<String> constructDescription(int level, List<String> description) {
            List<String> out = new ArrayList<>();
            out.add("&7Босс доступен с &e" + level + "&7 уровня.");
            out.add("");
            out.addAll(description);
            out.add("");
            out.add("&7Нажми для телепортации к боссу.");
            return out;
        }

        private final int bossId;
        private final int level;

        public BossItem(int bossId, Material icon, String name, int level, List<String> description) {
            super(icon, name, constructDescription(level, description));
            this.bossId = bossId;
            this.level = level;
        }

        @Override
        public void onClick(Player player, ClickType clickType, int slot) {
            BossScript script = BossManager.getScript(this.bossId);
            GamePlayer gamePlayer = PrisonEvo.getInstance().getAthenaManager().get(player);
            if (gamePlayer.getLevel() < this.level && !gamePlayer.asUser().getPermission().isAdministrator()) {
                gamePlayer.sendMessage("&cВаш уровень недостаточен для телепортации к этому боссу.");
                return;
            }
            if (!script.isAvailable() && !gamePlayer.asUser().getPermission().isAdministrator()) {
                gamePlayer.sendMessage("&cВ данный момент кто-то уже начал сражение с этим боссом. Телепортация невозможна.");
                return;
            }
            TeleportManager.teleport(player, script.getTeleportationPoint());
        }
    }

    private static final World WORLD = Bukkit.getWorld("prison_arena");

    private static final List<Location> pvpArena = Lists.newArrayList(
            new Location(WORLD, 0.5, 81, 27.5, -111F, -1.4F),
            new Location(WORLD, 17.5, 81, 17.5, -144, -1.6F),
            new Location(WORLD, 27.5, 81, 0.5),
            new Location(WORLD, 17.5, 81, -16.5, 124, 0),
            new Location(WORLD, 0.5, 81, -26.5, 65.6F, -2.5F),
            new Location(WORLD, -16.5, 81, -16.5, 36.2F, 0.1F),
            new Location(WORLD, -26.5, 81, 0.5, -18.6F, -0.5F),
            new Location(WORLD, -16.5, 81, 17.5, -52.7F, -3.7F),
            new Location(WORLD, -12.5, 66, 13.5, -139.4F, 0.9F),
            new Location(WORLD, 13.5, 66, 13.5, 133.3F, 0),
            new Location(WORLD, 13.5, 66, -12.5),
            new Location(WORLD, -12.5, 66, -12.5, -47.4F, 0)
    );

}
