/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import com.google.common.base.Preconditions;

/**
 * Converter class to convert a large number into a SI system.
 */
public final class ReadableNumberConverter {
    /**
     * Defines the base for a division, non-si standard could be 1024 for kilobytes
     */
    private static final int DIVISION_BASE = 1000;

    /**
     * String representation of the sorted postfixes
     */
    private static final char[] ENCODED_POSTFIXES = "KMGTPE".toCharArray();

    private ReadableNumberConverter() {
    }

    /**
     * restricts a string representation of a number to a specific width
     *
     * @param number to be formatted number
     * @param width  width limitation of the resulting number
     * @return formatted number restricted by the width limitation
     */
    public static String format(final long number, final int width) {
        Preconditions.checkArgument(number >= 0, "Non-negative numbers cannot be formatted by this method");

        // handles low numbers more efficiently since no format is needed
        final String numberString = Long.toString(number);
        int numberSize = numberString.length();
        if (numberSize <= width) {
            return numberString;
        }

        long base = number;
        double last = base * 1000;
        int exponent = -1;
        String postFix = "";

        while (numberSize > width) {
            last = base;
            base /= DIVISION_BASE;

            exponent++;

            // adds +1 due to the postfix
            numberSize = Long.toString(base).length() + 1;
            postFix = String.valueOf(ENCODED_POSTFIXES[exponent]);
        }

        final String withPrecision = getFormat().format(last / DIVISION_BASE) + postFix;
        final String withoutPrecision = base + postFix;

        final String slimResult = withPrecision.length() <= width ? withPrecision : withoutPrecision;

        // post condition
        assert slimResult.length() <= width;

        return slimResult;
    }

    /**
     * restricts a string representation of a number to a specific width
     *
     * @param number to be formatted number
     * @param width  width limitation of the resulting number
     * @return formatted number restricted by the width limitation
     */
    public static String format(double number, int width) {
        Preconditions.checkArgument(number >= 0, "Non-negative numbers cannot be formatted by this method");

        // In cases where we have more integer digits than we have space, we can directly go into the integer-formatter.
        // We have to take the decimal point into account which we'd need if we would show the fractional.
        var integerDigits = (int) Math.max(0, Math.log10(number) + 1);
        if (integerDigits > width - 1) {
            return format((long) number, width);
        }

        // If the fractional is smaller than the amount of fractional space we have, we omit
        // the fractional and instead prefix "~"
        var fractionalDigits = width - integerDigits - 1;
        var minFractional = Math.pow(10, -fractionalDigits);
        var fractional = number - Math.floor(number);
        if (fractional + 1e-9 < minFractional && integerDigits - 1 <= width) {
            return "~" + format((long) number, width - 1);
        }

        // handles low numbers more efficiently since no format is needed
        var format = getFormat();

        format.setDecimalSeparatorAlwaysShown(false);
        format.setMaximumFractionDigits(fractionalDigits);
        return format.format(number);
    }

    private static DecimalFormat getFormat() {
        var symbols = DecimalFormatSymbols.getInstance();
        var format = new DecimalFormat(".#;0.#");
        format.setDecimalFormatSymbols(symbols);
        format.setRoundingMode(RoundingMode.DOWN);
        return format;
    }
}
