package appeng.util;

import java.text.DecimalFormat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class NumberUtil {
    private static final String[] UNITS = { "", "K", "M", "G", "T", "P", "E", "Y", "Z", "R", "Q" };
    private static final DecimalFormat DF = new DecimalFormat("#.##");

    public static String formatNumber(double number) {
        if (number < 1000)
            return DF.format(number);
        int unit = Math.min((int) (Math.log10(number) / 3), UNITS.length - 1);
        return DF.format(number / Math.pow(1000, unit)) + UNITS[unit];
    }

    /**
     * Creates a Component displaying a percentage with coloring.
     * 
     * @param current The current amount.
     * @param max     The maximum amount.
     * @return Colored Component based on percentage.
     */
    public static Component createPercentageComponent(double current, double max) {
        if (max <= 0)
            return Component.literal("0%").withStyle(ChatFormatting.GREEN);

        double percentage = Math.max(0.0, Math.min(1.0, current / max));
        String percentageText = String.format("%.2f%%", percentage * 100);

        int red, green, blue = 0;
        if (percentage <= 0.33) {
            double localPercentage = percentage / 0.33;
            red = (int) (localPercentage * 180);
            green = 180;
        } else if (percentage <= 0.66) {
            double localPercentage = (percentage - 0.33) / 0.33;
            red = 180;
            green = (int) (180 - (localPercentage * 90));
        } else {
            double localPercentage = (percentage - 0.66) / 0.34;
            red = (int) (180 + (localPercentage * 75));
            green = (int) (90 - (localPercentage * 90));
        }

        int color = (red << 16) | (green << 8) | blue;
        return Component.literal(percentageText)
                .withStyle(Style.EMPTY.withColor(color));
    }
}
