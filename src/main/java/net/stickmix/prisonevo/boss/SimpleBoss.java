package net.stickmix.prisonevo.boss;

import lombok.Data;
import net.md_5.bungee.api.chat.TextComponent;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.bar.BossBar;
import net.stickmix.game.api.user.User;
import net.stickmix.game.api.util.ChatUtil;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.entity.PhantomIntelligentEntity;
import net.stickmix.prisonevo.utils.MultilineHologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public abstract class SimpleBoss extends PhantomIntelligentEntity implements Listener {

    private final Set<LootItem> loot = new HashSet<>();

    public SimpleBoss(EntityType type, double health, Location location, Set<LootItem> loot) {
        super(type, health, location);
        if (loot != null) this.loot.addAll(loot);
        Bukkit.getPluginManager().registerEvents(this, PrisonEvo.getInstance());
    }

    public abstract void spawnBoss();

    public abstract void despawnBoss();

    public abstract void teleportWithName(Location location);

    public abstract BossBar getBar();

    public abstract Map<Player, Double> getDamageDealers();

    protected void broadcastDeath(String name, BossScript script) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            TextComponent component = ChatUtil.makeTextComponent(ChatUtil.prefixed("&c&lPrisonEvo", "%s%s &a&lповержен! &8(наведите, чтобы посмотреть атакующих)", name, script.isHeroic() ? " &6&l&n(героический)&r" : ""));
            List<String> collect = getDamageDealers().entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .map(entry -> {
                        User dmsPlayer = GameApi.getUserManager().get(entry.getKey().getName());
                        return ChatUtil.colorize(
                                "%s &7- &b%.1f &7дмг",
                                dmsPlayer.getFullDisplayName(),
                                entry.getValue()
                        );
                    })
                    .collect(Collectors.toList());
            component.setHoverEvent(ChatUtil.makeHoverEvent(collect));
            player.sendMessage(component);
        });
    }

    protected void spawnHologram(String name, BossScript script, Location location) {
        int[] ticks = {script.getReloadingTime()};
        MultilineHologram hologram = new MultilineHologram(location)
                .appendLine(name + " &cмертв")
                .appendLine("&e" + ticks[0]-- * 1000);
        new BukkitRunnable() {


            @Override
            public void run() {
                if (ticks[0] <= 0) {
                    hologram.invalidate();
                    cancel();
                    return;
                }
                if (script.getPhase() == BossScript.Phase.WAITING) {
                    hologram.invalidate();
                    cancel();
                    return;
                }
                Bukkit.getOnlinePlayers().forEach(player -> hologram.getLines().forEach(hologram -> {
                    if (!hologram.isVisibleFor(player) && player.getWorld() == hologram.getLocation().getWorld()) {
                        hologram.show(player);
                    }
                }));
                String message = "" + ticks[0]-- * 1000;
                hologram.getLines().get(1).setText("&e" + message);
            }
        }.runTaskTimer(PrisonEvo.getInstance(), 20, 20L);
    }

    public void dropLoot(Location location) {
        World world = location.getWorld();
        loot.forEach(li -> {
            if (ThreadLocalRandom.current().nextInt(100) < li.probability) {
                ItemStack stack = li.item.clone();
                stack.setAmount(1);
                for (int i = 0; i < li.item.getAmount(); i++) {
                    world.dropItem(location, stack);
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Location to = event.getTo(), from = event.getFrom();
        if (to.getWorld() == getLocation().getWorld()) {
            BossBar bar = getBar();
            if (bar != null) {
                bar.addPlayer(event.getPlayer());
            }
            return;
        }
        if (from.getWorld() == getLocation().getWorld()) {
            BossBar bar = getBar();
            if (bar != null) {
                bar.removePlayer(event.getPlayer());
            }
        }
    }

    @Data
    public static class LootItem {

        private final int probability;
        private final ItemStack item;

        public LootItem(int probability, ItemStack item) {
            this.probability = probability;
            this.item = item;
        }

    }
}
