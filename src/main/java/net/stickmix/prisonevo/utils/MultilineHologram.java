package net.stickmix.prisonevo.utils;

import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.phantom.entity.PhantomEntity;
import net.stickmix.game.api.phantom.entity.PhantomHologram;
import org.bukkit.Location;

import java.util.LinkedList;
import java.util.List;

public class MultilineHologram {

    private final Location location;
    private final List<PhantomHologram> holograms = new LinkedList<>();

    public MultilineHologram(Location location) {
        this.location = location.clone().add(0, .23D, 0);
    }

    public MultilineHologram appendLine(String text, Object... args) {
        PhantomHologram hologram = GameApi.getPhantomEntityFactory().createHologram(String.format(text, args));
        hologram.setLocation(location.subtract(0, .23D, 0));
        hologram.spawn(true);
        holograms.add(hologram);
        return this;
    }

    public boolean isSpawned() {
        return !holograms.isEmpty();
    }

    public void invalidate() {
        holograms.forEach(PhantomEntity::invalidate);
        holograms.clear();
    }

    public List<PhantomHologram> getLines() {
        return holograms;
    }
}
