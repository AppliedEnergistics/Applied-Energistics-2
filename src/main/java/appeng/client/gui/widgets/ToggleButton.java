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

package appeng.client.gui.widgets;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import appeng.client.gui.Icon;

public class ToggleButton extends Button implements ITooltip {
    private final Icon icon;
    private final Icon iconDisabled;

    private final Component displayName;
    private final Component displayHint;

    private boolean isActive;

    public ToggleButton(final Icon on, final Icon off, final Component displayName,
            final Component displayHint, OnPress onPress) {
        super(0, 0, 16, 16, TextComponent.EMPTY, onPress);
        this.icon = on;
        this.iconDisabled = off;
        this.displayName = Objects.requireNonNull(displayName);
        this.displayHint = Objects.requireNonNull(displayHint);
    }

    public void setState(final boolean isOn) {
        this.isActive = isOn;
    }

    @Override
    public void renderButton(PoseStack poseStack, final int mouseX, final int mouseY, final float partial) {
        if (this.visible) {
            Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter().dest(x, y).blit(poseStack, getBlitOffset());
            getIcon().getBlitter().dest(x, y).blit(poseStack, getBlitOffset());
        }
    }

    private Icon getIcon() {
        return this.isActive ? this.icon : this.iconDisabled;
    }

    @Override
    public List<Component> getTooltipMessage() {
        return Arrays.asList(
                displayName,
                displayHint);
    }

    @Override
    public int getTooltipAreaX() {
        return this.x;
    }

    @Override
    public int getTooltipAreaY() {
        return this.y;
    }

    @Override
    public int getTooltipAreaWidth() {
        return 16;
    }

    @Override
    public int getTooltipAreaHeight() {
        return 16;
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return this.visible;
    }
}
