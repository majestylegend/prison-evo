package net.stickmix.prisonevo.athena;

import lombok.Getter;
import net.stickmix.game.api.athena.ObjectPool;
import net.stickmix.prisonevo.chest.DailyChest;
import net.stickmix.prisonevo.data.DeathMessage;
import net.stickmix.prisonevo.data.EnderChest;
import net.stickmix.prisonevo.data.GamePlayer;
import net.stickmix.prisonevo.data.Perks;
import net.stickmix.prisonevo.items.EvoItems;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;

public class AthenaManager {

    @Getter
    private final ObjectPool<GamePlayer> gamePlayerObjectPool;
    @Getter
    private final ObjectPool<EnderChest> enderChestObjectPool;
    @Getter
    private final ObjectPool<DailyChest> dailyChestObjectPool;
    @Getter
    private final ObjectPool<Perks> perksObjectPool;
    @Getter
    private final ObjectPool<EvoItems> evoItemsObjectPool;
    @Getter
    private final ObjectPool<DeathMessage> deathMessageObjectPool;

    public AthenaManager() {

        gamePlayerObjectPool = Storages.PLAYERS.newObjectPool();
        gamePlayerObjectPool.setDefaultObject(new GamePlayer(null, 0, 0, 0, 1, 0, new HashSet<>(), null));

        enderChestObjectPool = Storages.ENDERCHESTS.newObjectPool();
        enderChestObjectPool.setDefaultObject(new EnderChest(null, new ItemStack[27]));

        dailyChestObjectPool = Storages.DAILY.newObjectPool();
        dailyChestObjectPool.setDefaultObject(new DailyChest(null, 0, null));

        perksObjectPool = Storages.PERKS.newObjectPool();
        perksObjectPool.setDefaultObject(new Perks(null, new HashSet<>()));

        evoItemsObjectPool = Storages.EVOITEMS.newObjectPool();
        evoItemsObjectPool.setDefaultObject(new EvoItems(null, new HashSet<>()));

        deathMessageObjectPool = Storages.DEATH.newObjectPool();
        deathMessageObjectPool.setDefaultObject(new DeathMessage(null, null, 0, null, false));
    }

    public GamePlayer get(String name) {
        return gamePlayerObjectPool.get(name);
    }

    public GamePlayer get(Player player) {
        return gamePlayerObjectPool.get(player.getName());
    }
}
