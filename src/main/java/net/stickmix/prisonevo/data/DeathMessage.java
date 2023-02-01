package net.stickmix.prisonevo.data;

import lombok.AllArgsConstructor;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.athena.annotation.Id;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.utils.MultilineHologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@AllArgsConstructor
public class DeathMessage {
    @Id
    private final String playerName;
    private String message;
    private long lastUpdate;
    private MultilineHologram holograms;

    boolean hasFeature = false;


    public boolean hasFeature() {
        return hasFeature || GameApi.getUserManager().get(playerName).getPermission().isUnique();
    }

    public boolean hasCooldown() {
        if (GameApi.getUserManager().get(playerName).getPermission().isAdministrator()) {
            return false;
        }
        if (!hasFeature()) {
            return true;
        }
        return System.currentTimeMillis() - lastUpdate < 15 * 60_000;
    }

    public void buy() {
        if (hasFeature()) {
            return;
        }
        hasFeature = true;
        GamePlayer player = GamePlayer.wrap(playerName);
        if (player != null) {
            player.sendMessage("&aВы приобрели возможность оставлять предсмертное сообщение!");
        }
        PrisonEvo.getInstance().getAthenaManager().getDeathMessageObjectPool().save(this.playerName);
    }

    public void changeMessage(String message) {
        if (hasCooldown()) {
            return;
        }
        this.message = message;

        if (holograms != null) {
            holograms.getLines().get(1).setText(message);
        }
        this.message = message;
        this.lastUpdate = System.currentTimeMillis();

        PrisonEvo.getInstance().getAthenaManager().getDeathMessageObjectPool().save(this.playerName);
    }

    public void spawnHolograms(Location location) {
        if (!hasFeature()) {
            return;
        }
        if (message == null || message.isEmpty()) {
            return;
        }
        deleteHolograms();
        Location position = location.clone();
        holograms = new MultilineHologram(position);
        holograms.appendLine("%s &cпогиб", GameApi.getUserManager().get(playerName).getFullDisplayName());
        holograms.appendLine(message);

        Bukkit.getScheduler().runTaskLater(PrisonEvo.getInstance(), this::deleteHolograms, 20L * 60L * 2);
    }

    public void clear() {
        this.message = null;
        deleteHolograms();
    }

    public void deleteHolograms() {
        if (holograms != null) {
            holograms.invalidate();
            holograms = null;
        }
    }
}
