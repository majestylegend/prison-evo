package net.stickmix.prisonevo.utils;

import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.user.permission.UserPermission;
import net.stickmix.prisonevo.data.GamePlayer;
import org.bukkit.Bukkit;

import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class Modifiers {

    private static float MODIFIER = 0F;
    private static int TELEPORT_DELAY = 10;
    private static boolean SELLALL;
    private static float BOSS_MODIFIER = 1F;

    public static void initialize() {
        new GlobalBar();
    }

    public static void addGroup(UserPermission permissions) {
        if (!permissions.isVip() || permissions.isAdministrator()) {
            return;
        }
        setup(permissions);
        float modifier = getGroupModifier(permissions);
        if (modifier != 0F) {
            MODIFIER += modifier;
        } else {
            MODIFIER = 0F;
        }
        MODIFIER = Math.min(MODIFIER, 70F);
    }

    public static void removeGroup(UserPermission permissions) {
        if (!permissions.isVip() || permissions.isAdministrator()) {
            return;
        }
        Set<UserPermission> all = Bukkit.getOnlinePlayers().stream()
                .map(p -> GameApi.getUserManager().get(p.getName()).getPermission())
                .collect(Collectors.toSet());
        all.remove(permissions);
        all.removeIf(perms -> !perms.isVip() || perms.isAdministrator());
        SELLALL = false;
        TELEPORT_DELAY = 10;
        MODIFIER = 0F;
        BOSS_MODIFIER = 1F;
        for (UserPermission perms : all) {
            setup(perms);
            MODIFIER += getGroupModifier(perms);
        }
        MODIFIER = Math.min(MODIFIER, 70F);
    }

    private static float getGroupModifier(UserPermission permissions) {
        if (!permissions.isVip() || permissions.isAdministrator()) {
            return 0F;
        }
        if (permissions.isUnique()) {
            return 7.5F;
        }
        if (permissions.isSponsorPlus()) {
            return 5F;
        }
        if (permissions.isSponsor()) {
            return 3.5F;
        }
        if (permissions.isRichPlus()) {
            return 2.5F;
        }
        if (permissions.isRich()) {
            return 1.75F;
        }
        if (permissions.isPremiumPlus()) {
            return 1F;
        }
        if (permissions.isPremium()) {
            return .5F;
        }
        if (permissions.isVipPlus()) {
            return .25F;
        }
        if (permissions.isVip()) {
            return .1F;
        }
        return 0F;
    }

    public static long getBossMoneyModifier(GamePlayer player, long money) {
        float modifier = BOSS_MODIFIER;
        return (long) (money * modifier);
    }

    public static int getBossShardsModifier(GamePlayer player, int shards) {
        float modifier = BOSS_MODIFIER;
        return (int) (shards * modifier);
    }

    public static float getModifier() {
        return MODIFIER;
    }

    public static int getTeleportDelay() {
        return TELEPORT_DELAY;
    }

    public static boolean isSellall() {
        return SELLALL;
    }

    public static UnaryOperator<Long> getLongModifier() {
        return value -> (long) (value - (value / 100 * MODIFIER));
    }

    public static UnaryOperator<Integer> getIntModifier() {
        return value -> (int) (value - (value / 100 * MODIFIER));
    }

    private static void setup(UserPermission permissions) {
        SELLALL |= permissions.isPremium();
        int teleportDelay = permissions.isUnique()
                ? 1
                : permissions.isSponsor()
                ? 4
                : permissions.isRich()
                ? 6
                : permissions.isPremiumPlus()
                ? 8
                : permissions.isPremium()
                ? 9 : 10;
        TELEPORT_DELAY = Math.min(teleportDelay, TELEPORT_DELAY);
        float bossModifier = permissions.isUnique() ? 1.25F : permissions.isSponsor() ? 1.1F : 1F;
        BOSS_MODIFIER = Math.max(bossModifier, BOSS_MODIFIER);
    }
}
