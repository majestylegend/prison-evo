package net.stickmix.prisonevo.player;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.Notification;
import net.stickmix.game.api.user.User;
import net.stickmix.game.api.util.ChatUtil;
import net.stickmix.prisonevo.MainClass;
import net.stickmix.prisonevo.data.LevelData;
import net.villenium.athena.client.annotation.Id;
import net.villenium.athena.client.annotation.Name;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@Getter
@Setter
public class GamePlayer {

    @Id
    private final String owner;
    @Name(name = "balance")
    private long balance;
    @Name(name = "shards")
    private int shards;
    @Name(name = "blocks")
    private int blocks;
    @Name(name = "level")
    private int level;

    public GamePlayer(String owner, int balance, int shards, int blocks, int level) {
        this.owner = owner;
        this.balance = balance;
        this.shards = shards;
        this.blocks = blocks;
        this.level = level;
    }

    public void changeLevel(int delta) {
        level += delta;
        if (LevelData.getMaxLevel() < level) level = LevelData.getMaxLevel();
        if (level <= 0) level = 1;
        Player p = getHandle();
        if (p != null) {
            p.setLevel(level);
            MainClass.getInstance().getScoreboardData().updateLevel(this);
            MainClass.getInstance().getScoreboardData().updateProgress(this);
            val exp = (float) (LevelData.getPercentsToNextLevel(this) / 100D);
            p.setExp(Math.min(0.99F, exp));
            GameApi.getNotificationManager().send(
                    p,
                    Material.DIAMOND_SWORD,
                    "&aВы достигли &b" + level + "&a уровня! Поздравляем!",
                    Notification.FrameType.GOAL
            );
        }
    }

    public void changeBalance(long delta) {
        this.balance += delta;
        Player p = getHandle();
        if (p != null) {
            MainClass.getInstance().getScoreboardData().updateMoney(this);
            MainClass.getInstance().getScoreboardData().updateProgress(this);
            val exp = (float) (LevelData.getPercentsToNextLevel(this) / 100D);
            p.setExp(Math.min(0.99F, exp));
        }
    }

    public void sendMessage(String message, Object... args) {
        Player handle = getHandle();
        if (handle == null) {
            return;
        }
        handle.sendMessage(ChatUtil.prefixed("PrisonEvo", message, args));
    }

    public User getUser() {
        return GameApi.getUserManager().get(owner);
    }

    public Player getHandle() {
        return Bukkit.getPlayerExact(owner);
    }

}
