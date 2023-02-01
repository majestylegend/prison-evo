package net.stickmix.prisonevo.boss.keeper;

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

public class ForestKeeper extends SimpleBoss {

    private final ForestKeeperScript script;
    @Getter
    private final PhantomHologram displayName;

    private BossBar bar;

    public ForestKeeper(ForestKeeperScript script, Location location) {
        super(EntityType.IRON_GOLEM, script.isHeroic() ? 4500 : 1500, location, script.isHeroic() ? Sets.newHashSet(
                new LootItem(100, ItemBuilder.fromMaterial(Material.POTION)
                        .amount(3)
                        .withItemMeta(PotionMeta.class)
                        .setName("&fЗелье лечения")
                        .customModifying(meta -> meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL, false, true)))
                        .and().build()
                )
        ) : null);
        this.script = script;

        displayName = GameApi.getPhantomEntityFactory().createHologram(ForestKeeperScript.BOSS_NAME);
        displayName.setLocation(location.clone().add(0, 0.3D, 0));

        setLogic(new ForestKeeperLogic(this, script));

        getLogic()
                .logicFlag(LogicFlag.MOVEMENT_ALGORITHM, MovementManager.DONT_MOVE)
                .aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.CLOSEST)
                .aggressiveFlag(AggressiveLogicFlag.ATTACK_RANGE, 4)
                .aggressiveFlag(AggressiveLogicFlag.ATTACK_ONLY_PLAYERS, true)
                .aggressiveFlag(AggressiveLogicFlag.ATTACK_SPEED, 750L)
                .aggressiveFlag(AggressiveLogicFlag.DAMAGE, script.isHeroic() ? 5D : 2D)
                .aggressiveFlag(AggressiveLogicFlag.LAZY_TARGET_SEARCH, false);
    }

    @Override
    public boolean damage(double value) {
        double health = getHealth(), maxHealth = getMaxHealth();
        if (super.damage(value)) {
            onDeath();
            script.playSound(Sound.ENTITY_IRONGOLEM_DEATH);
            return true;
        }
        if (health / maxHealth > 0.6D && super.getHealth() / maxHealth <= 0.6D) {
            if (getLogic().phase != 2) {
                getLogic().aggressiveFlag(AggressiveLogicFlag.DAMAGE, script.isHeroic() ? 6D : 3D);
                getLogic().phase = 2;
                script.sendMessagePrefixed(ForestKeeperScript.BOSS_NAME, "&4Вы пришли в МОИ владения и пытаетесь одолеть МЕНЯ в МОЕМ же доме!!?");
                script.sendMessagePrefixed(ForestKeeperScript.BOSS_NAME, "&4Я разобью ваши кости на щепки и буду улыбаться, сжигая их!");
            }
        } else if (health / maxHealth > 0.4D && super.getHealth() / maxHealth <= 0.4D) {
            if (getLogic().phase != 3) {
                getLogic().phase = 3;
                getLogic().aggressiveFlag(AggressiveLogicFlag.ATTACK_SPEED, 300L);
                script.sendMessagePrefixed(ForestKeeperScript.BOSS_NAME, "&4В этом царстве есть место лишь для одного хранителя!");
            }
        }
        if (bar != null) {
            bar.setProgress(super.getHealth() / maxHealth);
        }
        script.playSound(Sound.ENTITY_IRONGOLEM_HURT);
        return false;
    }

    @Override
    public void spawnBoss() {
        spawn(true);
        displayName.spawn(true);
        bar = GameApi.getBarManager().createDefaultBar(ForestKeeperScript.BOSS_NAME, BarColor.GREEN, BarStyle.SOLID);
        bar.addSpigotPlayers(script.getPlayersInvolved());
    }

    @Override
    public void despawnBoss() {
        invalidate();
        bar.removeAll();
        bar = null;
        displayName.invalidate();
    }

    @Override
    public BossBar getBar() {
        return bar;
    }

    @Override
    public void teleportWithName(Location location) {
        teleport(location);
        displayName.teleport(location.clone().add(0, 0.3D, 0));
    }

    @Override
    public ForestKeeperLogic getLogic() {
        return (ForestKeeperLogic) super.getLogic();
    }

    @Override
    public Map<Player, Double> getDamageDealers() {
        return getLogic().getDamageDealers();
    }

    public void onDeath() {
        Location topLocation = ForestKeeperScript.CENTER.clone().add(0, 1, 0);
        spawnHologram(ForestKeeperScript.BOSS_NAME, script, topLocation);
        this.script.switchPhase(BossScript.Phase.RELOADING);
        this.script.getPlayersInvolved().stream()
                .map(GamePlayer::wrap)
                .forEach(pi -> {
                    pi.getLocalData().put("forest_keeper_killed", true);
                    val delta = script.isHeroic() ? 20 : 7;
                    pi.sendMessage("&6+%d золота", delta);
                    if (this.script.isHeroic()) {
                        pi.getLocalData().put("forest_keeper_heroic_killed", true);

                    }
                    int shards = script.isHeroic() ? 25 : 5;
                    long money = script.isHeroic() ? 5_000_000_000L : 5_000_000L;
                    money = Modifiers.getBossMoneyModifier(pi, money);
                    shards = Modifiers.getBossShardsModifier(pi, shards);
                    pi.changeShards(shards);
                    pi.changeBalance(money);
                    pi.sendMessage("&a&lВы получили &b&l%s&a&l!", NumberUtil.formatMoney(money));
                });
        broadcastDeath(ForestKeeperScript.BOSS_NAME, script);
    }

    public static class ForestKeeperLogic extends PhantomEntityAggressiveLogic {

        private final ForestKeeperScript script;

        @Getter
        private int phase = 1;

        public ForestKeeperLogic(PhantomIntelligentEntity creature, ForestKeeperScript script) {
            super(creature);
            this.script = script;
        }

        @Override
        public void process() {
            script.castTpSpell();
            if (phase > 1) {
                script.LIFT_UP.cast(script);
            }

            super.process();
        }
    }
}
