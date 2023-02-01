package net.stickmix.prisonevo.entity.logic;

import net.stickmix.prisonevo.entity.PhantomIntelligentEntity;
import net.stickmix.prisonevo.entity.event.PhantomEntityDamagePlayerEvent;
import net.stickmix.prisonevo.entity.logic.aggressive.PhantomEntityAggressiveLogic;
import net.stickmix.prisonevo.utils.DamageHelper;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EventBasedCircleAttackLogic extends PhantomEntityAggressiveLogic {

    private final Predicate<Void> circleAttackPredicate;

    public EventBasedCircleAttackLogic(PhantomIntelligentEntity creature, Predicate<Void> circleAttackPredicate) {
        super(creature);
        this.circleAttackPredicate = circleAttackPredicate;
    }

    @Override
    public boolean attack(Player target) {
        if (!canAttack(target)) {
            return false;
        }
        if (circleAttackPredicate.test(null)) {
            Collection<Player> players = target.getWorld().getNearbyPlayers(target.getLocation(), 3)
                    .stream()
                    .filter(player -> player.getGameMode() != GameMode.CREATIVE)
                    .collect(Collectors.toList());
            Collection<PhantomEntityDamagePlayerEvent> events = players.stream().map(p -> new PhantomEntityDamagePlayerEvent(getCreature(), p, getDamage())).collect(Collectors.toSet());
            events.forEach(Bukkit.getPluginManager()::callEvent);
            if (events.stream().allMatch(Cancellable::isCancelled)) {
                return false;
            }
            getCreature().lookAt(target);
            getCreature().getAnimations().playAnimationHand();
            events.forEach(e -> DamageHelper.clearDamage(e.getVictim(), e.getFinalDamage()));
            super.setLastAttackTime(System.currentTimeMillis());
            return true;
        }
        return attack0(target);
    }

    public boolean attack0(Player target) {
        PhantomEntityDamagePlayerEvent event = new PhantomEntityDamagePlayerEvent(getCreature(), target, getDamage());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        getCreature().lookAt(target);
        getCreature().getAnimations().playAnimationHand();
        double finalDamage = event.getFinalDamage();
        DamageHelper.clearDamage(target, finalDamage);
        super.setLastAttackTime(System.currentTimeMillis());
        return true;
    }

}
