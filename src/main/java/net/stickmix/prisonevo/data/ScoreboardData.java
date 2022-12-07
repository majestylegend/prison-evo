package net.stickmix.prisonevo.data;

import com.google.common.collect.Lists;
import net.stickmix.game.api.GameApi;
import net.stickmix.prisonevo.player.GamePlayer;
import net.stickmix.prisonevo.util.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ScoreboardData {

    private static Function<GamePlayer, List<String>> lines = player -> Lists.newArrayList(
            "",
            "&f&l&nУровень:&r " + "&a&l&n" + player.getLevel(),
            "",
            "&fШарды: &a" + player.getShards(),
            "&fДенег: &a" + (player.getBalance() >= 1_000_000_000_000L ? NumberUtil.formatMoneyHardly(player.getBalance()) : NumberUtil.formatMoney(player.getBalance())),
            "&fБлоков сломано: &a" + player.getBlocks(),
            "",
            "&fПрогресс: &a" + LevelData.getFormattedPercents(player),
            "",
            "   &bwww.stickmix.ru"
    );

    static {
        GameApi.getScoreboardUtil().updateTitle(Bukkit.getOnlinePlayers().stream().collect(Collectors.toList()), "&f&lPrison §c§l§nEvo");
    }

    public void setup(GamePlayer gamePlayer) {
        Player handle = gamePlayer.getHandle();
        GameApi.getScoreboardUtil().updateTitle(handle, "&f&lPrison §c§l§nEvo");
        GameApi.getScoreboardUtil().send(handle, lines.apply(gamePlayer));
    }

    public void updateLevel(GamePlayer player) {
        GameApi.getScoreboardUtil().send(player.getHandle(), 9, "&f&l&nУровень:&r " + "&a&l&n" + player.getLevel());
    }

    public void updateProgress(GamePlayer player) {
        GameApi.getScoreboardUtil().send(player.getHandle(), 3, "&fПрогресс: &a" + LevelData.getFormattedPercents(player));
    }

    public void updateMoney(GamePlayer player) {
        long balance = player.getBalance();
        String format = NumberUtil.formatMoney(balance);
        if (balance >= 1_000_000_000_000L) {
            format = NumberUtil.formatMoneyHardly(balance);
        }
        GameApi.getScoreboardUtil().send(player.getHandle(), 6, "&fДенег: &a" + format);
    }

    public void updateShards(GamePlayer player) {
        GameApi.getScoreboardUtil().send(player.getHandle(), 7, "&fШарды: &a" + player.getShards());
    }

    public void updateBlocks(GamePlayer player) {
        GameApi.getScoreboardUtil().send(player.getHandle(), 5, "&fБлоков сломано: &a" + player.getBlocks());
    }

    private String getTime() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Europe/Moscow")).format(dateTimeFormatter);
    }
}
