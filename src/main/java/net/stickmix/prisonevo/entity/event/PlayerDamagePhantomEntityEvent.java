package net.stickmix.prisonevo.entity.event;

import lombok.Getter;
import lombok.Setter;
import net.stickmix.prisonevo.entity.PhantomIntelligentEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerDamagePhantomEntityEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Setter
    @Getter
    private boolean cancelled = false;

    @Getter
    private final Player damager;

    @Getter
    private final PhantomIntelligentEntity victim;

    @Setter
    @Getter
    private double damage;

    public PlayerDamagePhantomEntityEvent(Player damager, PhantomIntelligentEntity victim, double damage) {
        this.damager = damager;
        this.victim = victim;
        this.damage = damage;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
