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

package appeng.client.theme;

import appeng.api.util.AEColor;
import appeng.client.gui.AEBaseScreen;

public enum ThemeColor {

    TEXT_TITLE("text.title", AEBaseScreen.COLOR_DARK_GRAY), TEXT_HEADING("text.heading", AEBaseScreen.COLOR_DARK_GRAY),
    TEXT_BODY("text.body", AEBaseScreen.COLOR_DARK_GRAY),

    TEXT_INPUT_NORMAL("text.input.normal", 0xFFA8A8A8), TEXT_INPUT_FOCUSED("text.input.focused", 0xFF606060),
    TEXT_INPUT_SELECTION("text.input.selection", 0xFF00FF00),

    FOREGROUND_CRAFTING_ACTIVE("foreground.crafting.active", AEColor.GREEN.blackVariant | 0x5A000000),
    FOREGROUND_CRAFTING_SCHEDULED("foreground.crafting.scheduled", AEColor.YELLOW.blackVariant | 0x5A000000),
    FOREGROUND_CRAFTING_MISSING("foreground.crafting.missing", 0x1AFF0000),

    FOREGROUND_SLOT_FLUID_EMPTY("foreground.slot.fluid.empty", AEColor.GRAY.blackVariant | 0xFF000000);

    private final ThemeConfig.ColorItem colorItem;
    private final int defaultColor;

    ThemeColor(String name, int defaultColor) {
        this.colorItem = ThemeConfig.INSTANCE.getColorItem(name, defaultColor);
        this.defaultColor = defaultColor;
    }

    public int argb() {
        if (this.colorItem != null) {
            return this.colorItem.getColor();
        }
        return this.defaultColor;
    }
}
