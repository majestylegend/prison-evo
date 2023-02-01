package net.stickmix.prisonevo.utils;

import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.bar.BossBar;
import net.stickmix.prisonevo.PrisonEvo;
import net.villenium.os.util.RListener;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class GlobalBar extends RListener {

    private final BossBar bar;

    public GlobalBar() {
        String[] text = {
                "&cОбщий модификатор от групп игроков: &b%.2f%%",
                "&eОбщий модификатор от групп игроков: &b%.2f%%"
        };
        double modifier = Modifiers.getModifier();
        bar = GameApi.getBarManager().createDefaultBar(String.format(text[1], modifier), BarColor.RED, BarStyle.SOLID);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PrisonEvo.getInstance(), new Runnable() {

            int tick = 0;

            @Override
            public void run() {
                if (tick == text.length) {
                    tick = 0;
                }
                double modifier = Modifiers.getModifier();
                String title = String.format(text[tick++], modifier);
                bar.setTitle(title);
            }
        }, 20L, 60L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        bar.addPlayer(event.getPlayer());
    }

}
