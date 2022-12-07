package net.stickmix.prisonevo.player;

import lombok.Getter;
import lombok.Setter;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.user.User;
import net.stickmix.game.api.util.ChatUtil;
import net.villenium.athena.client.annotation.Id;
import net.villenium.athena.client.annotation.Name;
import org.bukkit.Bukkit;
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
