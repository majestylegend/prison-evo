package net.stickmix.prisonevo.utils;

import net.stickmix.prisonevo.data.bonus.Bonus;

import java.util.EnumMap;
import java.util.Map;

public class DonationManager {

    private final Map<Bonus, Long> bonuses = new EnumMap<>(Bonus.class);

    public DonationManager() {
        /*DMS.getDatabase().sync().table(TABLE)
                .create()
                .column("bonus", DmsDatabaseCreateQuery.ColumnType.VARCHAR_128, true)
                .column("remaining_time", DmsDatabaseCreateQuery.ColumnType.BIG_INT, false)
                .executeUnchecked();

        List<Row> rows = DMS.getDatabase().sync().table(TABLE)
                .select()
                .allFields()
                .executeUnchecked()
                .getRows();
        rows.forEach(row -> {
            Bonus bonus;
            try {
                bonus = Bonus.valueOf(row.getString("bonus"));
            } catch (IllegalArgumentException ignored) {
                return;
            }
            long remainingTime = row.getLong("remaining_time");
            if (remainingTime <= 0) {
                return;
            }
            long endTime = System.currentTimeMillis() + remainingTime;
            bonuses.put(bonus, endTime);
        });*/
    }

    public void activateBonus(Bonus bonus) {
        long endTime = bonuses.getOrDefault(bonus, 0L);
        if (System.currentTimeMillis() >= endTime) {
            endTime = System.currentTimeMillis();
        }
        endTime += bonus.getActiveTimeInMillis();
        bonuses.put(bonus, endTime);
        long remainingTime = endTime - System.currentTimeMillis();
        if (remainingTime > 0) {
            /*DMS.getDatabase().async().table(TABLE)
                    .insert()
                    .field("bonus", bonus.name())
                    .field("remaining_time", remainingTime)
                    .onDuplicateKeyUpdate("remaining_time")
                    .executeUnchecked();*/
        }
    }

    public boolean isActive(Bonus bonus) {
        return bonuses.getOrDefault(bonus, 0L) > System.currentTimeMillis();
    }

    public void saveAll() {
        bonuses.forEach((bonus, endTime) -> {
            long current = System.currentTimeMillis();
            if (endTime < current) {
                return;
            }
            long remainingTime = endTime - current;
            if (remainingTime <= 0) {
                return;
            }
            /*DMS.getDatabase().async().table(TABLE)
                    .insert()
                    .field("bonus", bonus.name())
                    .field("remaining_time", remainingTime)
                    .onDuplicateKeyUpdate("remaining_time")
                    .executeUnchecked();*/
        });
    }
}
