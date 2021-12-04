/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.client.gui.me.common;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import appeng.api.stacks.AEFluidKey;

/**
 * @author AlgorithmX2
 * @author thatsIch
 * @author yueh
 * @version rv6
 * @since rv6
 */
public class FluidStackSizeRenderer extends StackSizeRenderer {

    private static final String[] NUMBER_FORMATS = new String[] { "#.000", "#.00", "#.0", "#" };

    @Override
    public String getToBeRenderedStackSize(final long originalSize, boolean useLargeFonts) {
        // Handle any value below 100 (large font) or 1000 (small font) Buckets with a
        // custom formatter,
        // otherwise pass it to the normal number converter
        if (originalSize < AEFluidKey.AMOUNT_BUCKET * 100 && useLargeFonts) {
            return this.getSlimRenderedStacksize(originalSize);
        } else if (originalSize < AEFluidKey.AMOUNT_BUCKET * 1000 && !useLargeFonts) {
            return this.getWideRenderedStacksize(originalSize);
        }

        return super.getToBeRenderedStackSize(originalSize / AEFluidKey.AMOUNT_BUCKET, useLargeFonts);
    }

    private String getSlimRenderedStacksize(final long originalSize) {
        final int log = 1 + (int) Math.floor(Math.log10(originalSize)) / 2;

        return this.getRenderedFluidStackSize(originalSize, log);
    }

    private String getWideRenderedStacksize(final long originalSize) {
        final int log = (int) Math.floor(Math.log10(originalSize)) / 2;

        return this.getRenderedFluidStackSize(originalSize, log);
    }

    private String getRenderedFluidStackSize(final long originalSize, final int log) {
        final int index = Math.max(0, Math.min(3, log));

        final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        final DecimalFormat format = new DecimalFormat(NUMBER_FORMATS[index]);
        format.setDecimalFormatSymbols(symbols);
        format.setRoundingMode(RoundingMode.DOWN);

        return format.format(originalSize / (double) AEFluidKey.AMOUNT_BUCKET);
    }

}
