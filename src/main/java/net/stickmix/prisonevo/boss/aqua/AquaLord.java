package net.stickmix.prisonevo.boss.aqua;

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
import net.stickmix.prisonevo.entity.logic.EventBasedCircleAttackLogic;
import net.stickmix.prisonevo.entity.logic.aggressive.AggressiveLogicFlag;
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

public class AquaLord extends SimpleBoss {

    private final AquaLordScript script;
    @Getter
    private final PhantomHologram displayName;
    @Getter
    private BossBar bar;

    public AquaLord(AquaLordScript script, Location location) {
        super(EntityType.ENDERMAN, script.isHeroic() ? 15000 : 7000, location, Sets.newHashSet(
                new LootItem(100, ItemBuilder.fromMaterial(Material.POTION)
                        .amount(script.isHeroic() ? 6 : 4)
                        .withItemMeta(PotionMeta.class)
                        .setName("&fЗелье лечения")
                        .customModifying(meta -> meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL, false, true)))
                        .and().build()
                ),
                new LootItem(100, ItemBuilder.fromMaterial(Material.SPLASH_POTION)
                        .amount(script.isHeroic() ? 3 : 2)
                        .withItemMeta(PotionMeta.class)
                        .setName("&fВзрывное зелье лечения")
                        .customModifying(meta -> meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL, false, true)))
                        .and().build()
                )
        ));
        this.script = script;

        this.displayName = GameApi.getPhantomEntityFactory().createHologram(AquaLordScript.BOSS_NAME);
        this.displayName.setLocation(location.clone().add(0, 1.3D, 0));

        setLogic(new AquaLordLogic(this, script));
        getLogic().aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.NONE)
                .aggressiveFlag(AggressiveLogicFlag.MOVEMENT_SPEED_MODIFIER, getLogic().getMovementSpeedModifier() * 1.5F)
                .aggressiveFlag(AggressiveLogicFlag.DAMAGE, script.isHeroic() ? 14D : 4D)
                .aggressiveFlag(AggressiveLogicFlag.AGGRESSION_RANGE, 32)
                .aggressiveFlag(AggressiveLogicFlag.ATTACK_SPEED, getLogic().getAttackSpeed() << 1)
                .aggressiveFlag(AggressiveLogicFlag.LAZY_TARGET_SEARCH, false);

    }

    @Override
    public boolean damage(double value) {
        if (script.anySlavesAlive()) {
            return false;
        }
        double health = super.getHealth(), max = super.getMaxHealth();
        if (super.damage(value)) {
            onDeath();
            script.playSound(Sound.ENTITY_ENDERDRAGON_DEATH);
            return true;
        }
        script.playSound(Sound.ENTITY_ENDERMEN_HURT);
        double d1 = health / max, d2 = super.getHealth() / max;
        if (d1 > 0.8D && d2 <= 0.8D) {
            this.script.setBattlePhase(2);
            this.script.sendMessagePrefixed(AquaLordScript.BOSS_NAME, "&4То, что вы здесь устроили, попросту смехотворно! Вам не одолеть меня.");
        } else if (d1 > 0.66D && d2 <= 0.66D) {
            this.script.setBattlePhase(3);
            this.script.sendMessagePrefixed(AquaLordScript.BOSS_NAME, "&4Вы выступаете против Шайна, Подводного Владыки! Все ваши попытки ничтожны.");
            this.script.sendMessagePrefixed(AquaLordScript.BOSS_NAME, "&4Что бы вы ни делали, все это будет не более чем жалкой каплей в бесконечном океане..");
        } else if (d1 > 0.33D && d2 <= 0.33D) {
            this.script.setBattlePhase(4);
            getLogic().aggressiveFlag(AggressiveLogicFlag.MOVEMENT_SPEED_MODIFIER, getLogic().getMovementSpeedModifier() + .3F);
            this.script.sendMessagePrefixed(AquaLordScript.BOSS_NAME, "&4Вам некуда бежать. Вокруг лишь тьма, и водные глубины, и они, то есть Я в их лице, приберут вас к себе!");
        }

        if (bar != null) {
            bar.setProgress(d2);
        }
        return false;
    }

    @Override
    public void spawnBoss() {
        spawn(true);
        this.displayName.spawn(true);
        bar = GameApi.getBarManager().createDefaultBar(AquaLordScript.BOSS_NAME, BarColor.BLUE, BarStyle.SOLID);
        bar.addSpigotPlayers(script.getPlayersInvolved());
    }

    @Override
    public void despawnBoss() {
        bar.removeAll();
        bar = null;
        invalidate();
        displayName.invalidate();
    }

    @Override
    public void teleportWithName(Location location) {
        teleport(location);
        displayName.teleport(location.clone().add(0, 1.2D, 0));
    }

    @Override
    public Map<Player, Double> getDamageDealers() {
        return getLogic().getDamageDealers();
    }

    @Override
    public AquaLordLogic getLogic() {
        return (AquaLordLogic) super.getLogic();
    }

    private void onDeath() {
        Location topLocation = AquaLordScript.CENTER.clone().add(0, 1, 0);
        spawnHologram(AquaLordScript.BOSS_NAME, script, topLocation);
        this.script.switchPhase(BossScript.Phase.RELOADING);
        dropLoot(getLocation());
        this.script.getPlayersInvolved().stream()
                .map(GamePlayer::wrap)
                .forEach(pi -> {
                    pi.getLocalData().put("aqua_lord_killed", true);
                    val delta = script.isHeroic() ? 25 : 10;
                    pi.sendMessage("&6+%d золота", delta);
                    int shards = script.isHeroic() ? 30 : 15;
                    long money = script.isHeroic() ? 87_500_000_000L : 5_000_000_000L;
                    money = Modifiers.getBossMoneyModifier(pi, money);
                    shards = Modifiers.getBossShardsModifier(pi, shards);
                    if (this.script.isHeroic()) {
                        pi.getLocalData().put("aqua_lord_heroic_killed", true);
                    }
                    pi.changeShards(shards);
                    pi.changeBalance(money);
                    pi.sendMessage("&a&lВы получили &b&l%s&a&l!", NumberUtil.formatMoney(money));
                });
        broadcastDeath(AquaLordScript.BOSS_NAME, script);
    }

    public static class AquaLordLogic extends EventBasedCircleAttackLogic {

        private final AquaLordScript script;

        @Getter
        private final int battlePhase = 1;

        public AquaLordLogic(PhantomIntelligentEntity creature, AquaLordScript script) {
            super(creature, ignored -> script.getBattlePhase() >= 4);
            this.script = script;
        }

        @Override
        public void process() {
            if (script.isHeroic()) {
                script.POISON.cast(script);
                if (battlePhase >= 3)
                    script.CONFUSION.cast(script);
                if (script.anySlavesAlive()) {
                    if (getBattlePhase() >= 2)
                        script.KICK_UP.cast(script);
                    return;
                }
            } else {
                if (script.anySlavesAlive()) {
                    script.POISON.cast(script);
                    if (getBattlePhase() >= 3)
                        script.KICK_UP.cast(script);
                    if (getBattlePhase() >= 4)
                        script.CONFUSION.cast(script);
                    return;
                }
            }
            if (script.getKing().getLocation().getBlockY() <= 70D) {
                script.SUMMON.cast(script);
            }
            script.SPAWN_SLAVES.cast(script);

            super.process();
            script.getKing().getDisplayName().teleport(getCreature().getLocation().clone().add(0, 1.2D, 0));
        }
    }
}
