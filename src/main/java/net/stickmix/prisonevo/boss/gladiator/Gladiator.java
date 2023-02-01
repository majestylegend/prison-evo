package net.stickmix.prisonevo.boss.gladiator;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.val;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.bar.BossBar;
import net.stickmix.game.api.phantom.entity.PhantomHologram;
import net.stickmix.prisonevo.boss.BossScript;
import net.stickmix.prisonevo.boss.SimpleBoss;
import net.stickmix.prisonevo.data.GamePlayer;
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

public class Gladiator extends SimpleBoss {

    private final GladiatorScript script;
    @Getter
    private final PhantomHologram displayName;
    @Getter
    private BossBar bar;

    public Gladiator(GladiatorScript script, Location location) {
        super(EntityType.HUSK, script.isHeroic() ? 23000 : 11000, location, Sets.newHashSet(
                new LootItem(100, ItemBuilder.fromMaterial(Material.POTION)
                        .amount(script.isHeroic() ? 10 : 4)
                        .withItemMeta(PotionMeta.class)
                        .setName("&fЗелье лечения")
                        .customModifying(meta -> meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL, false, true)))
                        .and().build()
                ),
                new LootItem(100, ItemBuilder.fromMaterial(Material.SPLASH_POTION)
                        .amount(script.isHeroic() ? 10 : 4)
                        .withItemMeta(PotionMeta.class)
                        .setName("&fВзрывное зелье лечения")
                        .customModifying(meta -> meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL, false, true)))
                        .and().build()
                )
        ));
        this.script = script;

        displayName = GameApi.getPhantomEntityFactory().createHologram(GladiatorScript.BOSS_NAME);
        displayName.setLocation(getLocation().clone().subtract(0, 0.1D, 0));

        setHand(new ItemStack(Material.IRON_SWORD));
        setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));

        setLogic(new GladiatorLogic(this, script));
        getLogic().aggressiveFlag(AggressiveLogicFlag.ATTACK_RANGE, 5)
                .aggressiveFlag(AggressiveLogicFlag.TARGETS_SEARCH_ALGORITHM, AggressiveLogicFlag.TargetSearchAlgorithm.MOST_RELEVANT)
                .aggressiveFlag(AggressiveLogicFlag.MOVEMENT_SPEED_MODIFIER, script.isHeroic() ? 2.2F : 2F)
                .aggressiveFlag(AggressiveLogicFlag.DAMAGE, script.isHeroic() ? 20D : 9D)
                .aggressiveFlag(AggressiveLogicFlag.AGGRESSION_RANGE, 160)
                .aggressiveFlag(AggressiveLogicFlag.ATTACK_SPEED, 700L)
                .aggressiveFlag(AggressiveLogicFlag.LAZY_TARGET_SEARCH, false);
    }

    @Override
    public boolean damage(double value) {
        double health = super.getHealth(), max = super.getMaxHealth();
        if (super.damage(value)) {
            onDeath();
            script.playSound(Sound.ENTITY_WITHER_DEATH);
            return true;
        }
        script.playSound(Sound.ENTITY_WITHER_HURT);
        double d1 = health / max, d2 = super.getHealth() / max;
        if (d1 > 0.7D && d2 <= 0.7D) {
            setLeggings(null);
            getLogic().aggressiveFlag(AggressiveLogicFlag.ATTACK_SPEED, 500L);
            this.script.setBattlePhase(2);
            this.script.sendMessagePrefixed(GladiatorScript.BOSS_NAME, "&4Великий гладиатор, коего вы искали здесь, давно погиб. Теперь он лишь призрак, слабый отзвук в моем сознании..");
        } else if (d1 > 0.45D && d2 <= 0.45D) {
            setBoots(null);
            getLogic().aggressiveFlag(AggressiveLogicFlag.DAMAGE, getLogic().getDamage() + 3D);
            this.script.setBattlePhase(3);
            this.script.sendMessagePrefixed(GladiatorScript.BOSS_NAME, "&4Жизнь кончается смертью, порядок сменяется хаосом. Смиритесь же с неизбежным, как смирился я!!");
            this.script.sendMessagePrefixed(GladiatorScript.BOSS_NAME, "&4Солнце уже закатилось над этим миром.");
        } else if (d1 > 0.25D && d2 <= 0.25D) {
            setChestplate(null);
            getLogic().aggressiveFlag(AggressiveLogicFlag.MOVEMENT_SPEED_MODIFIER, getLogic().getMovementSpeedModifier() + 0.5F);
            this.script.setBattlePhase(4);
            this.script.sendMessagePrefixed(GladiatorScript.BOSS_NAME, "&4Ахахахаха, покончить со мной? Никчемные герои, вы подумали, что можете отнять мою жизнь?");
            this.script.sendMessagePrefixed(GladiatorScript.BOSS_NAME, "&4Жизнь обманчива, хрупка, скоротечна. А смерти принадлежит вечность. Смерть - моя империя!!");
            this.script.sendMessagePrefixed(GladiatorScript.BOSS_NAME, "&4Посмотрите на меня, и вы увидите воплощение смерти!");
        }

        if (bar != null) {
            bar.setProgress(d2);
        }
        return false;
    }

    @Override
    public void spawnBoss() {
        spawn(true);
        displayName.spawn(true);
        bar = GameApi.getBarManager().createDefaultBar(GladiatorScript.BOSS_NAME, BarColor.YELLOW, BarStyle.SOLID);
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
        displayName.teleport(location.clone().subtract(0, 0.1D, 0));
    }

    @Override
    public GladiatorLogic getLogic() {
        return (GladiatorLogic) super.getLogic();
    }

    private void onDeath() {
        Location topLocation = GladiatorScript.CENTER.clone().add(0, 1, 0);
        spawnHologram(GladiatorScript.BOSS_NAME, script, topLocation);
        this.script.switchPhase(BossScript.Phase.RELOADING);
        dropLoot(getLocation());
        this.script.getPlayersInvolved().stream()
                .map(GamePlayer::wrap)
                .forEach(pi -> {
                    pi.getLocalData().put("gladiator_killed", true);
                    val delta = script.isHeroic() ? 35 : 20;
                    pi.sendMessage("&6+%d золота", delta);
                    int shards = script.isHeroic() ? 50 : 20;
                    long money = script.isHeroic() ? 300_000_000_000L : 35_000_000_000L;
                    money = Modifiers.getBossMoneyModifier(pi, money);
                    shards = Modifiers.getBossShardsModifier(pi, shards);
                    if (this.script.isHeroic()) {
                        pi.getLocalData().put("gladiator_heroic_killed", true);
                    }
                    pi.changeShards(shards);
                    pi.changeBalance(money);
                    pi.sendMessage("&a&lВы получили &b&l%s&a&l!", NumberUtil.formatMoney(money));
                });
        script.sendMessagePrefixed(GladiatorScript.BOSS_NAME, "&4Моя душа.. свободна...");
        broadcastDeath(GladiatorScript.BOSS_NAME, script);
    }

    @Override
    public Map<Player, Double> getDamageDealers() {
        return getLogic().getDamageDealers();
    }

    public static class GladiatorLogic extends EventBasedCircleAttackLogic {

        private final GladiatorScript script;

        public GladiatorLogic(Gladiator creature, GladiatorScript script) {
            super(creature, ignored -> script.isHeroic() ? script.getBattlePhase() >= 2 : script.getBattlePhase() >= 3);
            this.script = script;
        }

        @Override
        public void process() {
            Gladiator creature = (Gladiator) getCreature();
            if (!creature.isSpawned()) {
                return;
            }

            if (script.getBattlePhase() == 1)
                script.THROW_AWAY.cast(this.script);
            if (script.getBattlePhase() >= 2) {
                script.BLOCK_KILL.cast(this.script);
                script.CHANGE_STRATEGY.cast(this.script);
            }
            if (script.getBattlePhase() >= 3)
                script.FAST_ATTACKS.cast(this.script);
            script.SPAWN_COPIES.cast(this.script);
            if (script.isHeroic()) {
                script.castTpSpell();
            }
            super.process();
            creature.displayName.teleport(creature.getLocation().clone().subtract(0, 0.1D, 0));
        }
    }
}
