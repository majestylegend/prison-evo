package net.stickmix.prisonevo.boss.argus;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Argus extends SimpleBoss {

    private final ArgusScript script;
    @Getter
    private final PhantomHologram displayName;
    @Getter
    private BossBar bar;

    public Argus(ArgusScript script, Location location) {
        super(EntityType.WITHER_SKELETON, script.isHeroic() ? 21000 : 15000, location, Sets.newHashSet(
                new LootItem(100, ItemBuilder.fromMaterial(Material.POTION)
                        .amount(script.isHeroic() ? 11 : 6)
                        .withItemMeta(PotionMeta.class)
                        .setName("&fЗелье лечения")
                        .customModifying(meta -> meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL, false, true)))
                        .and().build()
                ),
                new LootItem(100, ItemBuilder.fromMaterial(Material.SPLASH_POTION)
                        .amount(script.isHeroic() ? 11 : 6)
                        .withItemMeta(PotionMeta.class)
                        .setName("&fВзрывное зелье лечения")
                        .customModifying(meta -> meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL, false, true)))
                        .and().build()
                ),
                new LootItem(30, ItemBuilder.fromMaterial(Material.TOTEM)
                        .amount(script.isHeroic() ? 2 : 1)
                        .withItemMeta()
                        .setName("&fТотем бессмертия")
                        .addLore("&7Спасет вас от нежелательной смерти..")
                        .and().build()
                ),
                new LootItem(80, ItemBuilder.fromMaterial(Material.SPLASH_POTION)
                        .amount(script.isHeroic() ? 3 : 2)
                        .withItemMeta(PotionMeta.class)
                        .setName("&fВзрывное зелье регенерации")
                        .customModifying(meta -> meta.setBasePotionData(new PotionData(PotionType.REGEN, false, true)))
                        .and().build()
                )
        ));
        this.script = script;

        displayName = GameApi.getPhantomEntityFactory().createHologram(ArgusScript.BOSS_NAME);
        displayName.setLocation(location.clone().add(0, 0.3D, 0));

        setHand(new ItemStack(Material.IRON_SWORD));
        setLogic(new ArgusLogic(this, script));

        getLogic().aggressiveFlag(AggressiveLogicFlag.ATTACK_RANGE, 5)
                .aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.MOST_RELEVANT)
                .aggressiveFlag(AggressiveLogicFlag.MOVEMENT_SPEED_MODIFIER, script.isHeroic() ? 2.5F : 2.4F)
                .aggressiveFlag(AggressiveLogicFlag.DAMAGE, script.isHeroic() ? 18D : 13D)
                .aggressiveFlag(AggressiveLogicFlag.AGGRESSION_RANGE, 50)
                .aggressiveFlag(AggressiveLogicFlag.ATTACK_SPEED, 1000L)
                .aggressiveFlag(AggressiveLogicFlag.LAZY_TARGET_SEARCH, false);
    }

    @Override
    public boolean damage(double value) {
        double health = super.getHealth(), max = super.getMaxHealth();
        int random = ThreadLocalRandom.current().nextInt(100);
        if (random < 10) {
            return false;
        }
        if (super.damage(value)) {
            onDeath();
            script.playSound(Sound.ENTITY_WITHER_SKELETON_DEATH);
            return true;
        }
        script.playSound(Sound.ENTITY_WITHER_SKELETON_HURT);
        double d1 = health / max, d2 = super.getHealth() / max;
        if (d1 > 0.7D && d2 <= 0.7D) {
            this.script.setBattlePhase(2);
            this.script.sendMessagePrefixed(ArgusScript.BOSS_NAME, "&4Зачем? Зачем нужно было прирывать мой покой? Чего вы хотели добиться..?");
            getLogic().aggressiveFlag(AggressiveLogicFlag.ATTACK_RANGE, 5)
                    .aggressiveFlag(AggressiveLogicFlag.ATTACK_SPEED, 600L);
        } else if (d1 > 0.45D && d2 <= 0.45D) {
            this.script.setBattlePhase(3);
            this.script.sendMessagePrefixed(ArgusScript.BOSS_NAME, "&4Мои мучения подходят к концу. Вас же дальше ждет только смерть!");
            this.script.sendMessagePrefixed(ArgusScript.BOSS_NAME, "&4Тени сгущаются..");
            ArgusScript.WORLD.setTime(16000);
            getLogic().aggressiveFlag(AggressiveLogicFlag.DAMAGE, getLogic().getDamage() + 3D);
        } else if (d1 > 0.20D && d2 <= 0.20D) {
            getLogic().aggressiveFlag(AggressiveLogicFlag.MOVEMENT_SPEED_MODIFIER, script.isHeroic() ? 2.7F : 2.6F);
            this.script.setBattlePhase(4);
            this.script.sendMessagePrefixed(ArgusScript.BOSS_NAME, "&4Что движет вами..? Не знаю..");
            this.script.sendMessagePrefixed(ArgusScript.BOSS_NAME, "&4Ваши старания напрасны, усилия бесполезны!");
            this.script.sendMessagePrefixed(ArgusScript.BOSS_NAME, "&4Все это бессмысленно, потому то, что грядет, уничтожит все, что вам дорого!");
        }

        if (bar != null) {
            bar.setProgress(d2);
        }
        return false;
    }

    private void onDeath() {
        Location topLocation = ArgusScript.CENTER.clone().add(0, 1, 0);
        spawnHologram(ArgusScript.BOSS_NAME, script, topLocation);
        this.script.switchPhase(BossScript.Phase.RELOADING);
        dropLoot(getLocation());
        this.script.getPlayersInvolved().stream()
                .map(GamePlayer::wrap)
                .forEach(pi -> {
                    pi.getLocalData().put("argus_killed", true);
                    val delta = script.isHeroic() ? 60 : 30;
                    pi.sendMessage("&6+%d золота", delta);
                    int shards = script.isHeroic() ? 80 : 50;
                    long money = script.isHeroic() ? 600_000_000_000L : 250_000_000_000L;
                    money = Modifiers.getBossMoneyModifier(pi, money);
                    shards = Modifiers.getBossShardsModifier(pi, shards);
                    if (this.script.isHeroic()) {
                        pi.getLocalData().put("argus_heroic_killed", true);
                    }
                    pi.changeShards(shards);
                    pi.changeBalance(money);
                    pi.sendMessage("&a&lВы получили &b&l%s&a&l!", NumberUtil.formatMoney(money));
                });
        script.sendMessagePrefixed(ArgusScript.BOSS_NAME, "&4Вам не устоять перед ним..");
        broadcastDeath(ArgusScript.BOSS_NAME, script);
    }

    @Override
    public void spawnBoss() {
        spawn(true);
        displayName.spawn(true);
        bar = GameApi.getBarManager().createDefaultBar(ArgusScript.BOSS_NAME, BarColor.BLUE, BarStyle.SOLID);
        bar.addSpigotPlayers(script.getPlayersInvolved());
        if (script.getPlayersInvolved().stream().map(GamePlayer::wrap).anyMatch(p -> p.getEvoItems().hasItem(150))) {
            script.sendMessagePrefixed(ArgusScript.BOSS_NAME, "Испепелитель, я знал, что ты придешь за мной..");
        }
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
        displayName.teleport(location.clone().add(0, 0.3D, 0));
    }

    @Override
    public Map<Player, Double> getDamageDealers() {
        return getLogic().getDamageDealers();
    }

    @Override
    public ArgusLogic getLogic() {
        return (ArgusLogic) super.getLogic();
    }

    public static class ArgusLogic extends EventBasedCircleAttackLogic {

        private final ArgusScript script;

        public ArgusLogic(PhantomIntelligentEntity creature, ArgusScript script) {
            super(creature, ignored -> script.getBattlePhase() >= 2);
            this.script = script;
        }

        @Override
        public void process() {
            Argus creature = (Argus) getCreature();
            if (!creature.isSpawned()) {
                return;
            }
            if (script.getBattlePhase() >= 1)
                script.THROW_AWAY.cast(script);
            script.PRISON.cast(script);
            if (script.getBattlePhase() >= 2) {
                script.DARKNESS.cast(script);
                script.KNOCK_SWORD.cast(script);
                script.SOCIAL.cast(script);
            }
            if (script.getBattlePhase() >= 3) {
                script.KILL.cast(script);
                script.FAST_ATTACKS.cast(script);
            }
            script.castTpSpell();
            super.process();
            creature.getDisplayName().teleport(creature.getLocation().clone().add(0, 0.3D, 0));
        }
    }
}
