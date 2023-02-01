package net.stickmix.prisonevo.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberUtil {

    private final static DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);

    static {
        formatSymbols.setGroupingSeparator('.');
    }

    private final static DecimalFormat format = new DecimalFormat("###,###,###,###,###$", formatSymbols);
    private final static DecimalFormat format2 = new DecimalFormat("###,###,###,###,###", formatSymbols);

    public static String format(long amount) {
        return format2.format(amount);
    }

    public static String formatMoney(long amount) {
        return format.format(amount);
    }

    public static String formatMoneyHardly(long amount) {
        if (amount <= 1_000)
            return formatMoney(amount);
        if (amount <= 1_000_000)
            return format2.format(amount / 1_000) + " тыс $";
        if (amount <= 1_000_000_000)
            return format2.format(amount / 1_000_000) + " млн $";
        if (amount <= 1_000_000_000_000L)
            return format2.format(amount / 1_000_000_000) + " млрд $";
        return format2.format(amount / 1_000_000_000_000L) + " трлн $";
    }

}
