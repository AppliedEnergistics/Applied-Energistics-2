package appeng.util;

import java.text.DecimalFormat;

public class NumberUtil {
    private static final String[] UNITS = { "", "K", "M", "G", "T", "P", "E", "Y", "Z", "R", "Q" };
    private static final DecimalFormat DF = new DecimalFormat("#.##");

    public static String formatNumber(double number) {
        if (number < 1000)
            return DF.format(number);
        int unit = Math.min((int) (Math.log10(number) / 3), UNITS.length - 1);
        return DF.format(number / Math.pow(1000, unit)) + UNITS[unit];
    }
}
