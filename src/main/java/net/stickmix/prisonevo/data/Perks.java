package net.stickmix.prisonevo.data;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.stickmix.game.api.item.GameItemStack;
import net.stickmix.prisonevo.PrisonEvo;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class Perks {

    private final String owner;
    @Getter
    private final Set<Perk> perks;


    public boolean hasPerk(Perk perk) {
        return this.perks.contains(perk);
    }

    public void addPerk(Perk perk) {
        this.perks.add(perk);
        Player p = Bukkit.getPlayerExact(owner);
        if (p != null)
            perk.onBuy(p);

        PrisonEvo.getInstance().getAthenaManager().getPerksObjectPool().save(p.getName());
    }

    public boolean removePerk(Perk perk) {
        if (!hasPerk(perk)) {
            return false;
        }
        this.perks.remove(perk);

        PrisonEvo.getInstance().getAthenaManager().getPerksObjectPool().save(owner);
        return true;
    }

    public void replacePerk(Perk old, Perk newOne) {
        if (removePerk(old)) {
            addPerk(newOne);
        }
    }

    @Getter
    public enum Perk {
        MONEY_I(250, "&a&l+10% к цене ресурсов", Lists.newArrayList(
                "&7&oС этим перком вы будете",
                "&7&oполучать на 10% больше ",
                "&7&o$ за продажу добытых ресурсов."
        ), Material.GOLD_NUGGET),
        MONEY_II(500, "&a&l+20% к цене ресурсов", Lists.newArrayList(
                "&7&oВы уже получаете на 10% больше",
                "&7&o$ за продажу добытых ресурсов.",
                "&7&oС этим перком вы будете получать",
                "&7&oеще на 10% больше."
        ), Material.GOLD_INGOT),
        MONEY_III(750, "&a&l+30% к цене ресурсов", Lists.newArrayList(
                "&7&oВы уже получаете на 20% больше",
                "&7&o$ за продажу добытых ресурсов.",
                "&7&oС этим перком вы будете получать",
                "&7&oеще на 10% больше."
        ), Material.DIAMOND),
        MONEY_IV(1000, "&a&l+50% к цене ресурсов", Lists.newArrayList(
                "&7&oВы уже получаете на 30% больше",
                "&7&o$ за продажу добытых ресурсов.",
                "&7&oС этим перком вы будете получать",
                "&7&oеще на 20% больше."
        ), Material.EMERALD),
        HEROIC_BOSSES(500, "&6&lДоступ к героическим боссам", Lists.newArrayList(
                "&7&oПосле покупки этого перка вы",
                "&7&oполучите трезубец, кликнув",
                "&7&oкоторым по блоку призыва босса,",
                "&7&oвы сможете начать бой с оным в",
                "&7&oгероическом, то бишь усиленном режиме."
        ), Material.STICK) {
            @Override
            public void onBuy(Player p) {
                GamePlayer.wrap(p).getEvoItems().grantItem(137);
            }

        },
        DOUBLE_LOOT_I(200, "&a&l+10% к вероятности двойного лута", Lists.newArrayList(
                "&7&oС этим перком вы повышаете",
                "&7&oвероятность выпадения удвоенного",
                "&7&oколичества ресурсов из каждого",
                "&7&oсломанного вами блока на 10%."
        ), Material.IRON_PICKAXE),
        DOUBLE_LOOT_II(400, "&a&l+25% к вероятности двойного лута", Lists.newArrayList(
                "&7&oВероятность выпадения удвоенного",
                "&7&oколичества ресурсов из каждого сломанного",
                "&7&oвами блока уже увеличено на 10%.",
                "&7&oС этим же перком оно будет увеличено еще",
                "&7&oна 15%, до 25%."
        ), Material.DIAMOND_PICKAXE),
        MORE_SHARDS_I(100, "&a&l+10% к выпадению шардов", Lists.newArrayList(
                "&7&oС этим перком вы повышаете",
                "&7&oвероятность выпадения шарда из",
                "&7&oкаждого сломанного вами блока на 10%."
        ), Material.STONE_HOE),
        MORE_SHARDS_II(200, "&a&l+20% к выпадению шардов", Lists.newArrayList(
                "&7&oВероятность выпадения шарда",
                "&7&oиз каждого сломанного вами блока",
                "&7&oуже увеличено на 10%.",
                "&7&oС этим же перком оно будет увеличено еще",
                "&7&oна 10%, до 20%."
        ), Material.IRON_HOE),
        MORE_SHARDS_III(300, "&a&l+30% к выпадению шардов", Lists.newArrayList(
                "&7&oВероятность выпадения шарда",
                "&7&oиз каждого сломанного вами блока",
                "&7&oуже увеличено на 20%.",
                "&7&oС этим же перком оно будет увеличено еще",
                "&7&oна 10%, до 30%."
        ), Material.GOLD_HOE),
        MORE_SHARDS_IV(600, "&a&l+50% к выпадению шардов", Lists.newArrayList(
                "&7&oВероятность выпадения шарда",
                "&7&oиз каждого сломанного вами блока",
                "&7&oуже увеличено на 30%.",
                "&7&oС этим же перком оно будет увеличено еще",
                "&7&oна 20%, до 50%."
        ), Material.DIAMOND_HOE),
        BOSS_DAMAGE_I(50, "&c&l+1 к урону против боссов", Lists.newArrayList(
                "&7&oУвеличивает урон, наносимый боссам",
                "&7&oи их прислужникам, на 1 единицу."
        ), Material.WOOD_AXE),
        BOSS_DAMAGE_II(100, "&c&l+2 к урону против боссов", Lists.newArrayList(
                "&7&oВы уже имеете увеличенный урон против",
                "&7&oбоссов и их прислужников на 1.",
                "&7&oС этим перком он будет увеличен до 2."
        ), Material.WOOD_SWORD),
        BOSS_DAMAGE_III(200, "&c&l+3 к урону против боссов", Lists.newArrayList(
                "&7&oВы уже имеете увеличенный урон против",
                "&7&oбоссов и их прислужников на 2.",
                "&7&oС этим перком он будет увеличен до 3."
        ), Material.STONE_SWORD),
        BOSS_DAMAGE_IV(400, "&c&l+4 к урону против боссов", Lists.newArrayList(
                "&7&oВы уже имеете увеличенный урон против",
                "&7&oбоссов и их прислужников на 3.",
                "&7&oС этим перком он будет увеличен до 4."
        ), Material.IRON_SWORD),
        BOSS_DAMAGE_V(600, "&c&l+5 к урону против боссов", Lists.newArrayList(
                "&7&oВы уже имеете увеличенный урон против",
                "&7&oбоссов и их прислужников на 4.",
                "&7&oС этим перком он будет увеличен до 5."
        ), Material.GOLD_SWORD),
        BOSS_DAMAGE_VI(800, "&c&l+6 к урону против боссов", Lists.newArrayList(
                "&7&oВы уже имеете увеличенный урон против",
                "&7&oбоссов и их прислужников на 5.",
                "&7&oС этим перком он будет увеличен до 6."
        ), Material.DIAMOND_SWORD),
        BOSS_CRIT_I(350, "&c&l+2% к криту по боссам", Lists.newArrayList(
                "&7&oДает шанс при ударе нанести 120%",
                "&7&oпо боссу или его прислужнику."
        ), Material.BLAZE_ROD),
        BOSS_CRIT_II(600, "&c&l+3% к криту по боссам", Lists.newArrayList(
                "&7&oДает шанс при ударе нанести 140%",
                "&7&oпо боссу или его прислужнику."
        ), Material.BLAZE_ROD),
        BOSS_CRIT_III(800, "&c&l+4% к криту по боссам", Lists.newArrayList(
                "&7&oДает шанс при ударе нанести 160%",
                "&7&oпо боссу или его прислужнику."
        ), Material.BLAZE_ROD),
        BOSS_CRIT_IV(1000, "&c&l+5% к криту по боссам", Lists.newArrayList(
                "&7&oДает шанс при ударе нанести 180%",
                "&7&oпо боссу или его прислужнику."
        ), Material.BLAZE_ROD),
        BOSS_CRIT_V(1400, "&c&l+6% к криту по боссам", Lists.newArrayList(
                "&7&oДает шанс при ударе нанести 200%",
                "&7&oпо боссу или его прислужнику."
        ), Material.BLAZE_ROD);

        public static Perk[] VALUES = values();

        private final ItemStack icon;
        private final int cost;

        Perk(int cost, String name, List<String> description, Material icon) {
            this.cost = cost;
            if (!description.isEmpty())
                description.add("");
            this.icon = new GameItemStack(icon, name, description);
        }

        public void onBuy(Player p) {
        }

    }

}
