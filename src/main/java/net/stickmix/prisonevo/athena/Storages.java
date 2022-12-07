package net.stickmix.prisonevo.athena;

import net.stickmix.prisonevo.player.GamePlayer;
import net.villenium.athena.client.IAthenaStorage;
import net.villenium.os.VilleniumOS;

public class Storages {

    public static IAthenaStorage<GamePlayer> PLAYERS = VilleniumOS.getInstance().getStorageManager().create("prisonevo_players", GamePlayer.class);
}
