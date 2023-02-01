package net.stickmix.prisonevo.data.bonus;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum Bonus {

    MODIFIER_5(60 * 5, Material.CHEST, "+5%% к общему модификатору", Lists.newArrayList()),
    MODIFIER_10(60 * 5, Material.CHEST, "+10%% к общему модификатору", Lists.newArrayList()),
    MODIFIER_15(60 * 3, Material.CHEST, "+15%% к общему модификатору", Lists.newArrayList()),
    ACCESS_TO_FOUR_UPPER_SHAFTS(60 * 5, Material.IRON_PICKAXE, "Доступ к шахтам на 4 уровня выше для всех", Lists.newArrayList()),
    ACCESS_TO_SIX_UPPER_SHAFTS(60 * 3, Material.DIAMOND_PICKAXE, "Доступ к шахтам на 6 уровней выше для всех", Lists.newArrayList());

    private final int durationInMinutes;
    private final Material material;
    private final String name;
    private final List<String> description;

    public long getActiveTimeInMillis() {
        return durationInMinutes * 60_000L;
    }

}