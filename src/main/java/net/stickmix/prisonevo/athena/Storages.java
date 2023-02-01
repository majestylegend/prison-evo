package net.stickmix.prisonevo.athena;

import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.athena.AthenaStorage;
import net.stickmix.prisonevo.chest.DailyChest;
import net.stickmix.prisonevo.data.DeathMessage;
import net.stickmix.prisonevo.data.EnderChest;
import net.stickmix.prisonevo.data.GamePlayer;
import net.stickmix.prisonevo.data.Perks;
import net.stickmix.prisonevo.items.EvoItems;

public class Storages {

    public static AthenaStorage<GamePlayer> PLAYERS = GameApi.getStorageManager().create("prisonevo_players", GamePlayer.class);
    public static AthenaStorage<EnderChest> ENDERCHESTS = GameApi.getStorageManager().create("prisonevo_enderchests", EnderChest.class);
    public static AthenaStorage<DailyChest> DAILY = GameApi.getStorageManager().create("prisonevo_daily", DailyChest.class);
    public static AthenaStorage<Perks> PERKS = GameApi.getStorageManager().create("prisonevo_perks", Perks.class);
    public static AthenaStorage<EvoItems> EVOITEMS = GameApi.getStorageManager().create("prisonevo_items", EvoItems.class);
    public static AthenaStorage<DeathMessage> DEATH = GameApi.getStorageManager().create("prisonevo_death", DeathMessage.class);
}
