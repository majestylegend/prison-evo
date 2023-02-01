package net.stickmix.prisonevo.boss;

import net.stickmix.prisonevo.boss.aqua.AquaLordScript;
import net.stickmix.prisonevo.boss.argus.ArgusScript;
import net.stickmix.prisonevo.boss.balnazzar.BalnazzarScript;
import net.stickmix.prisonevo.boss.blaze.BlazeKingScript;
import net.stickmix.prisonevo.boss.gladiator.GladiatorScript;
import net.stickmix.prisonevo.boss.keeper.ForestKeeperScript;

public class BossManager {

    private final static BossScript[] SCRIPTS = new BossScript[]{
            new ForestKeeperScript(),
            new BlazeKingScript(),
            new AquaLordScript(),
            new GladiatorScript(),
            new ArgusScript(),
            new BalnazzarScript()
    };

    public static int maxPossibleBossId() {
        return SCRIPTS.length;
    }

    public static BossScript getScript(int id) {
        return SCRIPTS[id - 1];
    }

}
