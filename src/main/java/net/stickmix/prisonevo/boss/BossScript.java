package net.stickmix.prisonevo.boss;

import com.google.common.collect.Lists;
import lombok.Data;
import net.stickmix.game.api.util.ChatUtil;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.data.GamePlayer;
import net.stickmix.prisonevo.utils.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

@Data
public abstract class BossScript implements Listener {

    private static List<BinaryOperator<String>> DEATHS_PHRASES = Lists.newArrayList(
            (boss, player) -> String.format("%s &eпал от рук %s&e!", player, boss),
            (boss, player) -> String.format("%s &eбыл изничтожен боссом %s&e!", player, boss)
    );

    private final int reloadingTime;
    private final Location teleportationPoint;
    private Phase phase = Phase.WAITING;
    private boolean heroic = false;

    protected BossScript(int reloadingTime, Location teleportationPoint) {
        this.reloadingTime = reloadingTime;
        this.teleportationPoint = teleportationPoint;
        Bukkit.getPluginManager().registerEvents(this, PrisonEvo.getInstance());

    }

    public abstract void startTheGame();

    public abstract void endTheGame();

    public abstract int getHeroicLevel();

    public boolean isAvailable() {
        return this.phase != Phase.RUNNING;
    }

    public void switchPhase(Phase phase) {
        if (phase == Phase.RUNNING)
            startTheGame();
        else if (phase == Phase.RELOADING || this.phase != Phase.RELOADING)
            endTheGame();
        if ((this.phase = phase) == Phase.RELOADING) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(PrisonEvo.getInstance(), () -> switchPhase(Phase.WAITING), 20L * reloadingTime);
        }
    }

    public List<Player> getPlayersInvolved() {
        return teleportationPoint.getWorld()
                .getPlayers()
                .stream()
                .collect(Collectors.toList());
    }

    public void sendMessagePrefixed(String name, String msg, Object... args) {
        sendMessage("%s&7: &c%s", name, String.format(msg, args));
    }

    public void sendMessage(String msg, Object... args) {
        String fmsg = ChatUtil.colorize(msg, args);
        getPlayersInvolved().forEach(p -> p.sendMessage(fmsg));
    }

    public void playSound(Sound sound) {
        getPlayersInvolved().forEach(player -> player.playSound(player.getLocation(), sound, 1F, 1F));
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getFrom().getWorld() == this.teleportationPoint.getWorld()) {
            return;
        }
        if (e.getTo().getWorld() == this.teleportationPoint.getWorld() && getPhase() == Phase.RUNNING) {
            GamePlayer player = PrisonEvo.getInstance().getAthenaManager().get(e.getPlayer());
            if (player.asUser().getPermission().isModerator()) {
                return;
            }
            player.sendMessage("&cСражение с боссом уже началось. Вы не можете телепортироваться на поле боя.");
            TeleportManager.cancelTeleportation(e.getPlayer());
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        List<String> worlds = Lists.newArrayList(
                "prison_boss1",
                "prison_boss2",
                "prison_boss3",
                "prison_boss5",
                "prison_boss6"
        );
        if (worlds.contains(p.getWorld().getName())) {
            p.teleport(PrisonEvo.SPAWN);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        GamePlayer gamePlayer = PrisonEvo.getInstance().getAthenaManager().get(e.getPlayer());
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK &&
                e.getClickedBlock().getWorld() == this.teleportationPoint.getWorld() &&
                e.getClickedBlock().getType() == Material.GOLD_BLOCK) {
            if (getPhase() == Phase.WAITING) {
                ItemStack item = e.getItem();
                boolean heroic = item != null && item.getType() == Material.STICK;
                if (heroic) {
                    if (getPlayersInvolved().stream().map(GamePlayer::wrap).anyMatch(p -> p.getLevel() < getHeroicLevel())) {
                        gamePlayer.sendMessage("&cУровень как минимум одного из игроков в этой зоне недостаточен для сражения с боссом в героическом режиме.");
                        gamePlayer.sendMessage("&cСражение с боссами в героическом режиме доступно с &b%d-го &cуровня.", getHeroicLevel());
                        return;
                    }
                    this.heroic = true;
                } else {
                    this.heroic = false;
                }
                String msg = ChatUtil.colorize("%s &eпризвал %s&e!", gamePlayer.asUser().getFullDisplayName(), this.heroic ? "&6&l&nгероическую версию босса" : "босса");
                getPlayersInvolved().forEach(p -> p.sendMessage(msg));
                switchPhase(Phase.RUNNING);
            } else if (getPhase() == Phase.RELOADING)
                gamePlayer.sendMessage("&cЭтого босса совсем недавно убили. Его вновь можно будет призвать через некоторое время, но не сейчас.");
        }
    }

    public enum Phase {
        WAITING, RUNNING, RELOADING
    }

}
