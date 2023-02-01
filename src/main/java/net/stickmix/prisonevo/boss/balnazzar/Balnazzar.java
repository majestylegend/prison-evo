package net.stickmix.prisonevo.boss.balnazzar;

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
import net.villenium.os.util.Task;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Balnazzar extends SimpleBoss {

    private final BalnazzarScript script;
    @Getter
    private final PhantomHologram displayName;
    @Getter
    private BossBar bar;

    public Balnazzar(BalnazzarScript script, Location location) {
        super(EntityType.PIG_ZOMBIE, script.isHeroic() ? 32000 : 25000, location, Sets.newHashSet(
                new LootItem(100, ItemBuilder.fromMaterial(Material.POTION)
                        .amount(script.isHeroic() ? 15 : 10)
                        .withItemMeta(PotionMeta.class)
                        .setName("&fЗелье лечения")
                        .customModifying(meta -> meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL, false, true)))
                        .and().build()
                ),
                new LootItem(100, ItemBuilder.fromMaterial(Material.SPLASH_POTION)
                        .amount(script.isHeroic() ? 15 : 10)
                        .withItemMeta(PotionMeta.class)
                        .setName("&fВзрывное зелье лечения")
                        .customModifying(meta -> meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL, false, true)))
                        .and().build()
                ),
                new LootItem(100, ItemBuilder.fromMaterial(Material.SPLASH_POTION)
                        .amount(script.isHeroic() ? 7 : 5)
                        .withItemMeta(PotionMeta.class)
                        .setName("&fВзрывное зелье регенерации")
                        .customModifying(meta -> meta.setBasePotionData(new PotionData(PotionType.REGEN, false, true)))
                        .and().build()
                )
        ));
        this.script = script;

        displayName = GameApi.getPhantomEntityFactory().createHologram(BalnazzarScript.BOSS_NAME);
        displayName.setLocation(location.clone().subtract(0, 0.1D, 0));

        setHand(new ItemStack(Material.IRON_SWORD));
        setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        setBoots(new ItemStack(Material.DIAMOND_BOOTS));

        setLogic(new BalnazzarLogic(this, script));

        getLogic().aggressiveFlag(AggressiveLogicFlag.ATTACK_RANGE, 4)
                .aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.CLOSEST)
                .aggressiveFlag(AggressiveLogicFlag.MOVEMENT_SPEED_MODIFIER, script.isHeroic() ? 2.5F : 2.4F)
                .aggressiveFlag(AggressiveLogicFlag.DAMAGE, script.isHeroic() ? 35D : 30D)
                .aggressiveFlag(AggressiveLogicFlag.AGGRESSION_RANGE, 50)
                .aggressiveFlag(AggressiveLogicFlag.ATTACK_SPEED, script.isHeroic() ? 700L : 800L)
                .aggressiveFlag(AggressiveLogicFlag.LAZY_TARGET_SEARCH, false);
    }

    @Override
    public void spawnBoss() {
        spawn(true);
        displayName.spawn(true);
        bar = GameApi.getBarManager().createDefaultBar(BalnazzarScript.BOSS_NAME, BarColor.BLUE, BarStyle.SOLID);
        bar.addSpigotPlayers(script.getPlayersInvolved());
        script.sendMessagePrefixed(BalnazzarScript.BOSS_NAME, "Это будет хорошим испытанием!");
    }

    @Override
    public boolean damage(double value) {
        double health = super.getHealth(), max = super.getMaxHealth();
        int random = ThreadLocalRandom.current().nextInt(100);
        if (random < 20) {
            value /= 4D;
        }
        if (super.damage(value)) {
            onDeath();
            script.playSound(Sound.ENTITY_ENDERDRAGON_AMBIENT);
            return true;
        }
        script.playSound(Sound.ENTITY_ENDERDRAGON_HURT);
        double d1 = health / max, d2 = super.getHealth() / max;
        if (d1 > 0.8D && d2 <= 0.8D) {
            setHelmet(null);
            this.script.setBattlePhase(2);
            this.script.sendMessagePrefixed(BalnazzarScript.BOSS_NAME, "&4Говорят, лишь чистые сердцем могут владеть этим клинком. Примите же свою судьбу!");
            getLogic().aggressiveFlag(AggressiveLogicFlag.ATTACK_RANGE, 6)
                    .aggressiveFlag(AggressiveLogicFlag.ATTACK_SPEED, 400L);
        } else if (d1 > 0.6D && d2 <= 0.6D) {
            setLeggings(null);
            this.script.setBattlePhase(3);
            this.script.sendMessagePrefixed(BalnazzarScript.BOSS_NAME, "Имя мне - Бальназзар. А вы - всего лишь пешки в игре за гранью вашего понимая.");
            Task.schedule(() -> this.script.sendMessagePrefixed(BalnazzarScript.BOSS_NAME, "&4Сейчас я подчиню себе ваши тела и вырву из них души!"), 60L);
            getLogic().aggressiveFlag(AggressiveLogicFlag.DAMAGE, getLogic().getDamage() + 10D);
        } else if (d1 > 0.4D && d2 <= 0.4D) {
            setBoots(null);
            this.script.setBattlePhase(4);
            this.script.sendMessagePrefixed(BalnazzarScript.BOSS_NAME, "&4Склонитесь перед новым хозяином!");
        } else if (d1 > 0.2D && d2 <= 0.2D) {
            setChestplate(null);
            this.script.setBattlePhase(5);
            this.script.sendMessagePrefixed(BalnazzarScript.BOSS_NAME, "Вы еще здесь, потому что я так хочу. Вы еще не трупы, потому что такова моя воля. Вы будуте служить мне!");
        }

        if (bar != null) {
            bar.setProgress(d2);
        }
        return false;
    }

    private void onDeath() {
        Location topLocation = BalnazzarScript.CENTER_FLYING.clone();
        spawnHologram(BalnazzarScript.BOSS_NAME, script, topLocation);
        this.script.switchPhase(BossScript.Phase.RELOADING);
        dropLoot(getLocation());
        this.script.getPlayersInvolved().stream()
                .map(GamePlayer::wrap)
                .forEach(pi -> {
                    pi.getLocalData().put("balnazzar_killed", true);
                    val delta = script.isHeroic() ? 100 : 50;
                    pi.sendMessage("&6+%d золота", delta);
                    int shards = script.isHeroic() ? 150 : 75;
                    long money = script.isHeroic() ? 2_000_000_000_000L : 950_000_000_000L;
                    money = Modifiers.getBossMoneyModifier(pi, money);
                    shards = Modifiers.getBossShardsModifier(pi, shards);
                    if (this.script.isHeroic()) {
                        pi.getLocalData().put("balnazzar_heroic_killed", true);
                    }
                    pi.changeShards(shards);
                    pi.changeBalance(money);
                    pi.sendMessage("&a&lВы получили &b&l%s&a&l!", NumberUtil.formatMoney(money));
                });
        script.sendMessagePrefixed(BalnazzarScript.BOSS_NAME, "&4Это еще не конец, смертные..");
        broadcastDeath(BalnazzarScript.BOSS_NAME, script);
    }

    @Override
    public void despawnBoss() {
        invalidate();
        displayName.invalidate();
        bar.removeAll();
        bar = null;
    }

    @Override
    public void teleportWithName(Location location) {
        teleport(location);
        teleport(location.clone().subtract(0, .1D, 0));
    }

    @Override
    public Map<Player, Double> getDamageDealers() {
        return getLogic().getDamageDealers();
    }

    @Override
    public BossBar getBar() {
        return bar;
    }

    @Override
    public BalnazzarLogic getLogic() {
        return (BalnazzarLogic) super.getLogic();
    }

    public static class BalnazzarLogic extends EventBasedCircleAttackLogic {

        private final BalnazzarScript script;

        public BalnazzarLogic(PhantomIntelligentEntity creature, BalnazzarScript script) {
            super(creature, ignored -> true);
            this.script = script;
        }

        @Override
        public void process() {
            Balnazzar creature = (Balnazzar) getCreature();
            if (!creature.isSpawned()) {
                return;
            }
            script.SWAP.cast(script);
            script.SLEEP.cast(script);
            if (script.getBattlePhase() >= 2) {
                script.BLOOD_CURSE.cast(script);
            }
            if (script.getBattlePhase() >= 3) {
                script.SPAWN_SLAVES.cast(script);
            }
            if (script.getBattlePhase() >= 4) {
                script.VORTEX.cast(script);
            }

            script.castTpSpell();
            super.process();
            creature.getDisplayName().teleport(creature.getLocation().clone().subtract(0, .1D, 0));
        }
    }
}
