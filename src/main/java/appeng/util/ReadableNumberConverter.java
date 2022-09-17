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
import java.text.Format;


/**
 * Converter class to convert a large number into a SI system.
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public enum ReadableNumberConverter implements ISlimReadableNumberConverter, IWideReadableNumberConverter {
    INSTANCE;

    /**
     * Defines the base for a division, non-si standard could be 1024 for kilobytes
     */
    private static final int DIVISION_BASE = 1000;

    /**
     * String representation of the sorted postfixes
     */
    private static final char[] ENCODED_POSTFIXES = "KMGTPE".toCharArray();

    private final Format format;

    /**
     * Initializes the specific decimal format with special format for negative and positive numbers
     */
    ReadableNumberConverter() {
        final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        final DecimalFormat format = new DecimalFormat(".#;0.#");
        format.setDecimalFormatSymbols(symbols);
        format.setRoundingMode(RoundingMode.DOWN);

        this.format = format;
    }

    @Override
    public String toSlimReadableForm(final long number) {
        return this.toReadableFormRestrictedByWidth(number, 3);
    }

    /**
     * restricts a string representation of a number to a specific width
     *
     * @param number to be formatted number
     * @param width  width limitation of the resulting number
     * @return formatted number restricted by the width limitation
     */
    private String toReadableFormRestrictedByWidth(final long number, final int width) {
        assert number >= 0;

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

        final String withPrecision = this.format.format(last / DIVISION_BASE) + postFix;
        final String withoutPrecision = base + postFix;

        final String slimResult = (withPrecision.length() <= width) ? withPrecision : withoutPrecision;

        // post condition
        assert slimResult.length() <= width;

        return slimResult;
    }

    @Override
    public String toWideReadableForm(final long number) {
        return this.toReadableFormRestrictedByWidth(number, 4);
    }
}
