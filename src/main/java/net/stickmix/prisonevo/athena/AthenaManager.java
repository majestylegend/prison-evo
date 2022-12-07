package net.stickmix.prisonevo.athena;

import lombok.Getter;
import net.stickmix.prisonevo.player.GamePlayer;
import net.villenium.athena.client.ObjectPool;

public class AthenaManager {

    @Getter
    private final ObjectPool<GamePlayer> gamePlayerObjectPool;


    public AthenaManager() {
        gamePlayerObjectPool = Storages.PLAYERS.newObjectPool();
        gamePlayerObjectPool.setDefaultObject(new GamePlayer(null, 0, 0, 0, 1));
    }

    public GamePlayer get(String name) {
        return gamePlayerObjectPool.get(name);
    }
}
