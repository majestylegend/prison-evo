package net.stickmix.prisonevo.boss.blaze;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.val;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.bar.BossBar;
import net.stickmix.game.api.phantom.entity.PhantomHologram;
import net.stickmix.prisonevo.boss.BossScript;
import net.stickmix.prisonevo.boss.SimpleBoss;
import net.stickmix.prisonevo.data.GamePlayer;
import net.stickmix.prisonevo.entity.PhantomIntelligentEntity;
import net.stickmix.prisonevo.entity.logic.LogicFlag;
import net.stickmix.prisonevo.entity.logic.aggressive.AggressiveLogicFlag;
import net.stickmix.prisonevo.entity.logic.aggressive.PhantomEntityAggressiveLogic;
import net.stickmix.prisonevo.entity.movement.MovementManager;
import net.stickmix.prisonevo.utils.ItemBuilder;
import net.stickmix.prisonevo.utils.Modifiers;
import net.stickmix.prisonevo.utils.NumberUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.Map;

public class BlazeKing extends SimpleBoss {

    private final BlazeKingScript script;
    @Getter
    private final PhantomHologram displayName;

    @Getter
    private BossBar bar;

    public BlazeKing(BlazeKingScript script) {
        super(EntityType.BLAZE, script.isHeroic() ? 6500 : 3000, BlazeKingScript.SPAWN_LOCATION, Sets.newHashSet(
                new LootItem(100, ItemBuilder.fromMaterial(Material.POTION)
                        .withItemMeta(PotionMeta.class)
                        .setName("&fЗелье повелителя огня")
                        .customModifying(meta -> {
                            meta.setBasePotionData(new PotionData(PotionType.FIRE_RESISTANCE, true, false));
                        })
                        .and().build()
                ),
                new LootItem(100, ItemBuilder.fromMaterial(Material.POTION)
                        .amount(script.isHeroic() ? 3 : 2)
                        .withItemMeta(PotionMeta.class)
                        .setName("&fЗелье лечения")
                        .customModifying(meta -> {
                            meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL, false, true));
                        })
                        .and().build()
                )
        ));
        this.script = script;

        displayName = GameApi.getPhantomEntityFactory().createHologram(BlazeKingScript.BOSS_NAME);
        displayName.setLocation(getLocation().clone().subtract(0, 0.5D, 0));

        setLogic(new BlazeKingLogic(this, script));

        getLogic().aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.CLOSEST)
                .aggressiveFlag(AggressiveLogicFlag.ATTACK_RANGE, 7)
                .aggressiveFlag(AggressiveLogicFlag.ATTACK_SPEED, 750L)
                .aggressiveFlag(AggressiveLogicFlag.DAMAGE, script.isHeroic() ? 10D : 4D)
                .aggressiveFlag(AggressiveLogicFlag.LAZY_TARGET_SEARCH, false)
                .logicFlag(LogicFlag.MOVEMENT_ALGORITHM, MovementManager.DONT_MOVE);
    }

    @Override
    public boolean damage(double value) {
        if (script.anyCrystalsAlive()) {
            return false;
        }
        double health = super.getHealth(), max = super.getMaxHealth();
        if (super.damage(value)) {
            onDeath();
            script.playSound(Sound.ENTITY_BLAZE_DEATH);
            return true;
        }
        script.playSound(Sound.ENTITY_BLAZE_HURT);

        if (health / max > 0.5D && super.getHealth() / max <= 0.5D) {
            getLogic().phase = 2;
            this.script.sendMessagePrefixed(BlazeKingScript.BOSS_NAME, "&4Я есть Неизбежность! Я есть Неотвратимость!! Я - ЯРОСТЬ, ВЫЖИГАЮЩАЯ ВСЕ НА СВОЕМ ПУТИ!!!");
        } else if (health / max > 0.25D && super.getHealth() / max <= 0.25D) {
            getLogic().phase = 3;
            getLogic().aggressiveFlag(AggressiveLogicFlag.DAMAGE, getLogic().getDamage() + (this.script.isHeroic() ? 6D : 3D));
            if (this.script.isHeroic()) {
                getLogic().aggressiveFlag(AggressiveLogicFlag.ATTACK_SPEED, getLogic().getAttackSpeed() * 2 / 3);
            }
            this.script.sendMessagePrefixed(BlazeKingScript.BOSS_NAME, "&4Да как вы, жалкие ничтожества, смеете нападать на меня!? Вы - лишь пыль, что я стряхну со своего плеча!");
        }
        if (bar != null) {
            bar.setProgress(super.getHealth() / max);
        }
        return false;
    }

    @Override
    public void spawnBoss() {
        spawn(true);
        displayName.spawn(true);
        bar = GameApi.getBarManager().createDefaultBar(BlazeKingScript.BOSS_NAME, BarColor.YELLOW, BarStyle.SOLID);
        bar.addSpigotPlayers(script.getPlayersInvolved());
    }

    @Override
    public void despawnBoss() {
        bar.removeAll();
        bar = null;
        displayName.invalidate();
        invalidate();
    }

    @Override
    public void teleportWithName(Location location) {
        teleport(location);
        displayName.teleport(location.clone().subtract(0, 0.5D, 0));
    }

    @Override
    public BlazeKingLogic getLogic() {
        return (BlazeKingLogic) super.getLogic();
    }

    private void onDeath() {
        Location topLocation = BlazeKingScript.CENTER.clone().add(0, 1, 0);
        spawnHologram(BlazeKingScript.BOSS_NAME, script, topLocation);
        this.script.switchPhase(BossScript.Phase.RELOADING);
        dropLoot(getLocation());
        this.script.getPlayersInvolved().stream()
                .map(GamePlayer::wrap)
                .forEach(pi -> {
                    pi.getLocalData().put("blaze_king_killed", true);
                    val delta = script.isHeroic() ? 30 : 10;
                    pi.sendMessage("&6+%d золота", delta);
                    int shards = script.isHeroic() ? 30 : 10;
                    long money = script.isHeroic() ? 30_000_000_000L : 1_000_000_000L;
                    money = Modifiers.getBossMoneyModifier(pi, money);
                    shards = Modifiers.getBossShardsModifier(pi, shards);
                    if (this.script.isHeroic()) {
                        pi.getLocalData().put("blaze_king_heroic_killed", true);
                    }
                    pi.changeShards(shards);
                    pi.changeBalance(money);
                    pi.sendMessage("&a&lВы получили &b&l%s&a&l!", NumberUtil.formatMoney(money));
                });
        broadcastDeath(BlazeKingScript.BOSS_NAME, script);
    }

    @Override
    public Map<Player, Double> getDamageDealers() {
        return getLogic().getDamageDealers();
    }

    public static class BlazeKingLogic extends PhantomEntityAggressiveLogic {

        private final BlazeKingScript script;

        @Getter
        private int phase = 1;

        public BlazeKingLogic(PhantomIntelligentEntity creature, BlazeKingScript script) {
            super(creature);
            this.script = script;
        }

        @Override
        public void process() {
            super.process();
            if (!script.anyCrystalsAlive()) {
                script.CHANGE_POSITION.cast(script);
            }
            if (script.anyCrystalsAlive() && !script.isHeroic()) {
                script.TRANSPOSITION.cast(script);
            }
            script.SET_ON_FIRE.cast(script);
            if (phase >= 2)
                script.SET_ON_FIRE_GLOBAL.cast(script);
            if (script.isHeroic() && phase >= 2 || phase >= 3) {
                script.BLINDNESS.cast(script);
                script.CONFUSION.cast(script);
            }
        }
    }
}
