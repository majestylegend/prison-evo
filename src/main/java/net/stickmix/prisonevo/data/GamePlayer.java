package net.stickmix.prisonevo.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.Notification;
import net.stickmix.game.api.athena.annotation.Id;
import net.stickmix.game.api.user.User;
import net.stickmix.game.api.util.ChatUtil;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.chest.DailyChest;
import net.stickmix.prisonevo.items.EvoItems;
import net.stickmix.prisonevo.utils.Backpack;
import net.stickmix.prisonevo.utils.ItemUtils;
import net.stickmix.prisonevo.utils.Level;
import net.stickmix.prisonevo.utils.MainScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;


@Getter
@Setter
@AllArgsConstructor
public class GamePlayer {

    @Id
    private final String owner;
    private long balance;
    private int shards;
    private int blocksBroken;
    private int level;
    private int backpackLevel;
    private String items;
    private Inventory backpack;

    private final Map<String, Object> localData = new HashMap<>();

    public void upgradeBackpack() {
        this.backpackLevel++;
        Inventory newBackpack = Backpack.createBackpack(this);
        if (this.backpack != null && newBackpack != null) {
            for (int i = 0; i < backpack.getSize(); ++i) {
                newBackpack.setItem(i, backpack.getItem(i));
            }
            this.backpack.clear();
        }
        this.backpack = newBackpack;
    }

    public void changeBalance(long delta) {
        this.balance += delta;
        Player p = getHandle();
        if (p != null) {
            MainScoreboard.updateMoney(this);
            MainScoreboard.updateProgress(this);
            val exp = (float) (Level.getPercentsToNextLevel(this) / 100D);
            p.setExp(Math.min(0.99F, exp));
        }
    }

    public void playShardObtainEffect(int amount) {
        Player p = getHandle();
        if (p == null) {
            return;
        }
        sendMessage(amount == 1 ? "&a&lВы нашли волшебный шард!" : "&a&lВы получили волшебные шарды (" + amount + ")!");
        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
    }

    public void changeShards(int delta) {
        this.shards += delta;
        Player p = getHandle();
        if (p != null)
            MainScoreboard.updateShards(this);
        if (delta > 0) {
            playShardObtainEffect(delta);
        }
    }

    public void changeLevel(int delta) {
        level += delta;
        if (Level.getMaxLevel() < level) level = Level.getMaxLevel();
        if (level <= 0) level = 1;
        Player p = getHandle();
        if (p != null) {
            p.setLevel(level);
            MainScoreboard.updateLevel(this);
            MainScoreboard.updateProgress(this);
            val exp = (float) (Level.getPercentsToNextLevel(this) / 100D);
            p.setExp(Math.min(0.99F, exp));
            GameApi.getNotificationManager().send(
                    p,
                    Material.DIAMOND_SWORD,
                    "&aВы достигли &b" + level + "&a уровня! Поздравляем!",
                    Notification.FrameType.GOAL
            );
        }
    }

    public void changeBlocks(int delta) {
        blocksBroken += delta;
        Player p = getHandle();
        if (p != null && System.currentTimeMillis() - GameData.lastBlocksUpdate.get(this) >= 10_000) {
            MainScoreboard.updateBlocks(this);
            MainScoreboard.updateProgress(this);
            val exp = (float) (Level.getPercentsToNextLevel(this) / 100D);
            p.setExp(Math.min(0.99F, exp));
            GameData.lastBlocksUpdate.remove(this);
            GameData.lastBlocksUpdate.put(this, System.currentTimeMillis());
            saveItems();
        }
    }

    public EnderChest getEnderChest() {
        return PrisonEvo.getInstance().getAthenaManager().getEnderChestObjectPool().get(this.owner);
    }

    public DailyChest getDailyChest() {
        return PrisonEvo.getInstance().getAthenaManager().getDailyChestObjectPool().get(this.owner);
    }

    public Perks getPerks() {
        return PrisonEvo.getInstance().getAthenaManager().getPerksObjectPool().get(this.owner);
    }

    public EvoItems getEvoItems() {
        return PrisonEvo.getInstance().getAthenaManager().getEvoItemsObjectPool().get(this.owner);
    }

    public DeathMessage getDeathMessage() {
        return PrisonEvo.getInstance().getAthenaManager().getDeathMessageObjectPool().get(this.owner);
    }

    public void saveItems() {
        this.items = ItemUtils.arrayToString(getHandle().getInventory().getStorageContents());
    }

    public void fulfillInventory() {
        Player handle = getHandle();
        if (handle == null) {
            return;
        }
        PlayerInventory inventory = handle.getInventory();
        ItemStack[] contents = ItemUtils.stringToArray(this.items, new ItemStack[36]);
        inventory.setContents(contents);
    }

    public void sendMessage(String message, Object... args) {
        Player handle = getHandle();
        if (handle == null) {
            return;
        }
        handle.sendMessage(ChatUtil.prefixed("&c&lPrisonEvo", message, args));
    }

    public void addResource(ItemStack is) {
        Player p = getHandle();
        if (p == null) {
            return;
        }
        Map<Integer, ItemStack> over = p.getInventory().addItem(is);
        if (over.isEmpty())
            return;
        if (this.backpack == null) {
            sendMessage("&cУ вас недостаточно места в инвентаре!");
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F);
            return;
        }
        for (ItemStack value : over.values())
            if (!this.backpack.addItem(value).isEmpty()) {
                sendMessage("&cУ вас недостаточно места в инвентаре и рюкзаке!");
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F);
                return;
            }
    }

    public User asUser() {
        return GameApi.getUserManager().get(owner);
    }

    public Player getHandle() {
        return Bukkit.getPlayerExact(owner);
    }

    public static GamePlayer wrap(Player player) {
        return PrisonEvo.getInstance().getAthenaManager().get(player);
    }

    public static GamePlayer wrap(String player) {
        return PrisonEvo.getInstance().getAthenaManager().get(player);
    }
}
