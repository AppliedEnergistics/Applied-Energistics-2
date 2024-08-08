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

package appeng.util.helpers;

import com.google.common.base.Preconditions;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import appeng.api.util.AEColor;

public class P2PHelper {

    public AEColor[] toColors(short frequency) {
        final AEColor[] colors = new AEColor[4];

        for (int i = 0; i < 4; i++) {
            int nibble = getFrequencyNibble(frequency, i);

            colors[i] = AEColor.values()[nibble];
        }

        return colors;
    }

    private static int getFrequencyNibble(short frequency, int i) {
        return frequency >> 4 * (3 - i) & 0xF;
    }

    public short fromColors(AEColor[] colors) {
        Preconditions.checkArgument(colors.length == 4);

        int t = 0;

        for (int i = 0; i < 4; i++) {
            int code = colors[3 - i].ordinal() << 4 * i;

            t |= code;
        }

        return (short) (t & 0xFFFF);
    }

    public String toHexDigit(AEColor color) {
        return String.format("%01X", color.ordinal());
    }

    public String toHexString(short frequency) {
        return String.format("%04X", frequency);
    }

    private static final String[] HEX_DIGITS = {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"
    };

    public MutableComponent toColoredHexString(short frequency) {
        var parent = Component.empty();

        for (var i = 0; i < 4; i++) {
            var nibble = getFrequencyNibble(frequency, i);
            var hex = Component.literal(HEX_DIGITS[nibble]);
            parent.append(hex.setStyle(hex.getStyle().withColor(AEColor.values()[nibble].mediumVariant)));
        }

        return parent;
    }

}
