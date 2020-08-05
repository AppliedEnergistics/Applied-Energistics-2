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

package appeng.fluids.client.gui.widgets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AEColor;
import appeng.client.gui.widgets.ITooltip;
import appeng.fluids.util.IAEFluidTank;

@Environment(EnvType.CLIENT)
public class FluidTankWidget extends AbstractButtonWidget implements ITooltip {
    private final IAEFluidTank tank;
    private final int slot;

    public FluidTankWidget(IAEFluidTank tank, int slot, int x, int y, int w, int h) {
        super(x, y, w, h, LiteralText.EMPTY);
        this.tank = tank;
        this.slot = slot;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height,
                    AEColor.GRAY.blackVariant | 0xFF000000);

            final IAEFluidStack fluidStack = this.tank.getFluidInSlot(this.slot);
            if (fluidStack != null && fluidStack.getStackSize() > 0) {

                FluidVolume volume = fluidStack.getFluidStack();
                FluidAmount maxAmount = this.tank.getMaxAmount_F(this.slot);
                double fillRatio = volume.amount().div(maxAmount).asInexactDouble();

                final int scaledHeight = (int) (this.height * fillRatio);

                // Render the tank's content unstretched in patches of 16x16 squares,
                // with a partial square for the remainder
                int iconHeightRemainder = scaledHeight % 16;
                int top = this.y + this.height - iconHeightRemainder;
                if (iconHeightRemainder > 0) {
                    int x1 = this.x;
                    int y1 = top;
                    int x2 = x1 + 16;
                    int y2 = y1 + iconHeightRemainder;
                    volume.renderGuiRect(x1, y2, x2, y2);
                }
                for (int i = 0; i < scaledHeight / 16; i++) {
                    int x1 = this.x;
                    int y1 = top - (i + 1) * 16;
                    int x2 = x1 + 16;
                    int y2 = y1 + 16;
                    volume.renderGuiRect(x1, y2, x2, y2);
                }
            }

        }
    }

    @Override
    public Text getTooltipMessage() {
        final IAEFluidStack fluid = this.tank.getFluidInSlot(this.slot);
        if (fluid != null && fluid.getStackSize() > 0) {
            Text desc = fluid.getFluidStack().getName();
            String amountToText = fluid.getStackSize() + "mB";

            return desc.copy().append("\n").append(amountToText);
        }
        return LiteralText.EMPTY;
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

}
