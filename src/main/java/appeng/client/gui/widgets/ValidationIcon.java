/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;

import appeng.client.gui.Icon;

/**
 * Displays a small icon that shows validation errors for some input control.
 */
public class ValidationIcon extends IconButton {

    private final List<Component> tooltip = new ArrayList<>();

    public ValidationIcon() {
        super(btn -> {
        });
        setDisableBackground(true);
        setDisableClickSound(true);
        setHalfSize(true);
    }

    public void setValid(boolean valid) {
        setVisibility(!valid);
        if (valid) {
            this.tooltip.clear();
        }
    }

    @Override
    public List<Component> getTooltipMessage() {
        return tooltip;
    }

    public void setTooltip(List<Component> lines) {
        this.tooltip.clear();
        this.tooltip.addAll(lines);
    }

    @Override
    protected Icon getIcon() {
        return Icon.INVALID;
    }

    @Override
    public boolean changeFocus(boolean flag) {
        return false; // Cannot focus this element
    }

}
