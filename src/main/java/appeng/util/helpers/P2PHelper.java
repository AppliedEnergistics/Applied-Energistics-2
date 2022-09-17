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


import appeng.api.util.AEColor;
import com.google.common.base.Preconditions;


public class P2PHelper {

    public AEColor[] toColors(short frequency) {
        final AEColor[] colors = new AEColor[4];

        for (int i = 0; i < 4; i++) {
            int nibble = (frequency >> 4 * (3 - i)) & 0xF;

            colors[i] = AEColor.values()[nibble];
        }

        return colors;
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

}
