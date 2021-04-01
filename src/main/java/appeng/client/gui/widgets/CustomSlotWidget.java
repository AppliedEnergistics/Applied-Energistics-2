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

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public abstract class CustomSlotWidget extends AbstractGui implements ITooltip {
    private final int x;
    private final int y;
    private final int id;

    public CustomSlotWidget(final int id, final int x, final int y) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public boolean canClick(final PlayerEntity player) {
        return true;
    }

    public void slotClicked(final ItemStack clickStack, final int mouseButton) {
    }

    public abstract void drawContent(MatrixStack matrixStack, final Minecraft mc, final int mouseX, final int mouseY,
            final float partialTicks);

    public void drawBackground(MatrixStack matrices, int guileft, int guitop, int currentZIndex) {
    }

    @Override
    public ITextComponent getTooltipMessage() {
        return StringTextComponent.EMPTY;
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
        return false;
    }

    public boolean isSlotEnabled() {
        return true;
    }

}
