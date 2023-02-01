package net.stickmix.prisonevo.utils;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.ScoreBoardUtil;
import net.stickmix.prisonevo.data.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class MainScoreboard {

    private static final ScoreBoardUtil utils = GameApi.getScoreboardUtil();

    static {
        utils.updateTitle(Bukkit.getOnlinePlayers().stream().collect(Collectors.toList()), "&c&lPrisonEvo");
    }

    private static final Function<GamePlayer, List<String>> lines = player -> Lists.newArrayList(
            "&e&lПрогресс",
            "  &fУровень:&a " + (Level.isMaxLevel(player) ? "&n&a" : "") + player.getLevel(),
            "  &fДо следующего:&a " + Level.getFormattedPercents(player),
            "",
            "&e&lСтатистика",
            "  &fДеньги:&a " + (player.getBalance() >= 1_000_000_000_000L ? NumberUtil.formatMoneyHardly(player.getBalance()) : NumberUtil.formatMoney(player.getBalance())),
            "  &fШарды:&a " + player.getShards(),
            "  &fБлоки:&a " + player.getBlocksBroken(),
            "",
            "      &b&lstickmix.online"
    );

    public void setup(GamePlayer gamePlayer) {
        Player handle = gamePlayer.getHandle();
        utils.updateTitle(handle, "&c&lPrisonEvo");
        utils.send(handle, lines.apply(gamePlayer));
    }

    public void updateLevel(GamePlayer player) {
        utils.send(player.getHandle(), 9, "  &fУровень:&a " + (Level.isMaxLevel(player) ? "&n&a" : "") + player.getLevel());
    }

    public void updateProgress(GamePlayer player) {
        utils.send(player.getHandle(), 8, "  &fДо следующего:&a " + Level.getFormattedPercents(player));
    }

    public void updateMoney(GamePlayer player) {
        long balance = player.getBalance();
        String format = NumberUtil.formatMoney(balance);
        if (balance >= 1_000_000_000_000L) {
            format = NumberUtil.formatMoneyHardly(balance);
        }
        utils.send(player.getHandle(), 5, "  &fДеньги:&a " + format);
    }

    public void updateShards(GamePlayer player) {
        utils.send(player.getHandle(), 4, "  &fШарды:&a " + player.getShards());
    }

    public void updateBlocks(GamePlayer player) {
        utils.send(player.getHandle(), 3, "  &fБлоки:&a " + player.getBlocksBroken());
    }

    private String getTime() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Europe/Moscow")).format(dateTimeFormatter);
    }
}
