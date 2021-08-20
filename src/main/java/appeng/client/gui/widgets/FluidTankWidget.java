/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.IIngredientSupplier;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.FluidBlitter;
import appeng.util.Platform;
import appeng.util.fluid.IAEFluidTank;

@Environment(EnvType.CLIENT)
public class FluidTankWidget extends AbstractWidget implements ITooltip, IIngredientSupplier {
    private final IAEFluidTank tank;
    private final int slot;

    public FluidTankWidget(IAEFluidTank tank, int slot) {
        super(0, 0, 0, 0, TextComponent.EMPTY);
        this.tank = tank;
        this.slot = slot;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            final IAEFluidStack fluidStack = this.tank.getFluidInSlot(this.slot);
            if (fluidStack != null && fluidStack.getStackSize() > 0) {
                Blitter blitter = FluidBlitter.create(fluidStack.getFluid());

                final int filledHeight = (int) (this.height
                        * ((float) fluidStack.getStackSize() / this.tank.getTankCapacity(this.slot)));

                // We assume the sprite has equal width/height, and to maintain that 1:1 aspect ratio,
                // We draw rectangles of size width by width to fill our entire height
                final int stepHeight = width;

                // We have to draw in multiples of the step height, but it's possible we need
                // to draw a "partial". This draws "bottom up"
                int iconHeightRemainder = filledHeight % stepHeight;
                for (int i = 0; i < filledHeight / stepHeight; i++) {
                    blitter.dest(x, y + height - iconHeightRemainder - (i + 1) * stepHeight, stepHeight, stepHeight)
                            .blit(poseStack, getBlitOffset());
                }
                // Draw the remainder last because it modifies the blitter
                if (iconHeightRemainder > 0) {
                    // Compute how much of the src sprite's height will be visible for this last piece
                    int srcHeightRemainder = (int) (blitter.getSrcHeight()
                            * (iconHeightRemainder / (float) stepHeight));
                    blitter.src(blitter.getSrcX(), blitter.getSrcY(), blitter.getSrcWidth(), srcHeightRemainder)
                            .dest(this.x, this.y + this.height - iconHeightRemainder, width, iconHeightRemainder)
                            .blit(poseStack, getBlitOffset());
                }
            }

        }
    }

    @Override
    public List<Component> getTooltipMessage() {
        final IAEFluidStack fluid = this.tank.getFluidInSlot(this.slot);
        if (fluid != null && fluid.getStackSize() > 0) {
            return Arrays.asList(
                    Platform.getFluidDisplayName(fluid),
                    new TextComponent(fluid.getStackSize() + "mB"));
        }
        return Collections.emptyList();
    }

    @Override
    public int getTooltipAreaX() {
        return this.x - 2;
    }

    @Override
    public int getTooltipAreaY() {
        return this.y - 2;
    }

    @Override
    public int getTooltipAreaWidth() {
        return this.width + 4;
    }

    @Override
    public int getTooltipAreaHeight() {
        return this.height + 4;
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return true;
    }

    @Nullable
    @Override
    public FluidVariant getFluidIngredient() {
        var stack = tank.getFluidInSlot(this.slot);
        return stack != null ? stack.getFluid() : null;
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
    }

}
