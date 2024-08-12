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

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Tooltip;
import appeng.core.localization.GuiText;

/**
 * A 3x3 toolbox panel attached to the player inventory.
 */
public class ToolboxPanel implements ICompositeWidget {

    private final Component toolbeltName;

    // Relative to the origin of the current screen (not window)
    private Rect2i bounds = new Rect2i(0, 0, 0, 0);

    public ToolboxPanel(Component toolbeltName) {
        this.toolbeltName = toolbeltName;
    }

    @Override
    public void setPosition(Point position) {
        this.bounds = new Rect2i(position.getX(), position.getY(), bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public void setSize(int width, int height) {
        this.bounds = new Rect2i(bounds.getX(), bounds.getY(), width, height);
    }

    @Override
    public Rect2i getBounds() {
        return bounds;
    }

    @Override
    public void addBackgroundPanels(PanelBlitter panels, Rect2i screenBounds) {
        panels.addBounds(bounds);
    }

    @Nullable
    @Override
    public Tooltip getTooltip(int mouseX, int mouseY) {
        return new Tooltip(
                this.toolbeltName,
                GuiText.UpgradeToolbelt.text().plainCopy().withStyle(ChatFormatting.GRAY));
    }

}
