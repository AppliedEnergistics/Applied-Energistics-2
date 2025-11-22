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

package appeng.client.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import appeng.client.gui.style.ScreenStyle;

/**
 * Variant of AETextField that only accepts digits as characters. Also includes shorthands for getting and setting the
 * value as int. Value should always be parseable as int, hence no exception handling in get/set.
 */
public class IntegerTextField extends AETextField {

    public IntegerTextField(ScreenStyle style, Font fontRenderer, int x, int y, int width, int height) {
        super(style, fontRenderer, x, y, width, height);
        this.setPlaceholder(Component.literal("0"));
        this.setFilter(text -> {
            try {
                if (!text.isEmpty()) {
                    Integer.parseInt(text);
                }
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
    }

    public int getIntValue() {
        if (this.getValue().isEmpty()) {
            return 0;
        }
        return Integer.parseInt(this.getValue());
    }

    public void setIntValue(int value) {
        setValue(Integer.toString(value));
    }
}
