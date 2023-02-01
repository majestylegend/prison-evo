package net.stickmix.prisonevo.listeners;

import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.data.GamePlayer;
import net.stickmix.prisonevo.data.Perks;
import net.stickmix.prisonevo.mob.Silverfish;
import net.stickmix.prisonevo.plot.Plot;
import net.stickmix.prisonevo.plot.PlotManager;
import net.stickmix.prisonevo.plot.Resource;
import net.villenium.os.util.AlgoUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class PlotListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        GamePlayer gamePlayer = PrisonEvo.getInstance().getAthenaManager().get(p);
        Block broken = e.getBlock();
        e.setCancelled(true);
        if (PlotManager.isAllowBreakInPlot(gamePlayer, broken.getLocation())) {
            Plot plot = PlotManager.getPlotByLoc(broken.getLocation());
            if (plot == null) {
                return;
            }
            plot.applyEffects(p);
            Resource resource = Resource.getByBlockMaterial(broken.getType());
            broken.setType(Material.AIR);
            if (resource == null) {
                return;
            }

            gamePlayer.changeBlocks(1);

            int doubleProbability = 0, shardProbability = 600;
            Perks perks = gamePlayer.getPerks();
            if (perks.hasPerk(Perks.Perk.DOUBLE_LOOT_II)) {
                doubleProbability += 25;
            } else if (perks.hasPerk(Perks.Perk.DOUBLE_LOOT_I)) {
                doubleProbability += 10;
            }

            if (perks.hasPerk(Perks.Perk.MORE_SHARDS_IV)) {
                shardProbability *= 0.5F;
            } else if (perks.hasPerk(Perks.Perk.MORE_SHARDS_III)) {
                shardProbability *= 0.7F;
            } else if (perks.hasPerk(Perks.Perk.MORE_SHARDS_II)) {
                shardProbability *= 0.8F;
            } else if (perks.hasPerk(Perks.Perk.MORE_SHARDS_I)) {
                shardProbability *= 0.9F;
            }

            gamePlayer.addResource(new ItemStack(resource.getItem(), AlgoUtil.r(100) < doubleProbability ? 2 : 1));

            if (AlgoUtil.r(shardProbability) == 7) {
                gamePlayer.changeShards(1);
            }

            if (plot.getLevel() >= 75) {
                if (AlgoUtil.r(500) == 10) {
                    new Silverfish(broken.getLocation()).spawn(true);
                }
            }
        }
    }

}
