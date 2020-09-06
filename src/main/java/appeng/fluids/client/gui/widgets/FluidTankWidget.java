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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.widgets.ITooltip;
import appeng.client.theme.ThemeColor;
import appeng.fluids.util.IAEFluidTank;

@OnlyIn(Dist.CLIENT)
public class FluidTankWidget extends Widget implements ITooltip {
    private final IAEFluidTank tank;
    private final int slot;

    public FluidTankWidget(IAEFluidTank tank, int slot, int x, int y, int w, int h) {
        super(x, y, w, h, StringTextComponent.EMPTY);
        this.tank = tank;
        this.slot = slot;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            RenderSystem.disableBlend();

            fill(matrixStack, this.x, this.y, this.x + this.width, this.y + this.height,
                    ThemeColor.FOREGROUND_SLOT_FLUID_EMPTY.argb());

            final IAEFluidStack fluidStack = this.tank.getFluidInSlot(this.slot);
            if (fluidStack != null && fluidStack.getStackSize() > 0) {
                Fluid fluid = fluidStack.getFluid();
                FluidAttributes attributes = fluid.getAttributes();

                float red = (attributes.getColor() >> 16 & 255) / 255.0F;
                float green = (attributes.getColor() >> 8 & 255) / 255.0F;
                float blue = (attributes.getColor() & 255) / 255.0F;
                RenderSystem.color3f(red, green, blue);

                Minecraft mc = Minecraft.getInstance();
                mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                final TextureAtlasSprite sprite = mc.getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE)
                        .apply(attributes.getStillTexture(fluidStack.getFluidStack()));

                final int scaledHeight = (int) (this.height
                        * ((float) fluidStack.getStackSize() / this.tank.getTankCapacity(this.slot)));

                int iconHeightRemainder = scaledHeight % 16;
                if (iconHeightRemainder > 0) {
                    blit(matrixStack, this.x, this.y + this.height - iconHeightRemainder, getBlitOffset(), 16,
                            iconHeightRemainder, sprite);
                }
                for (int i = 0; i < scaledHeight / 16; i++) {
                    blit(matrixStack, this.x, this.y + this.height - iconHeightRemainder - (i + 1) * 16,
                            getBlitOffset(), 16, 16, sprite);
                }
            }

        }
    }

    @Override
    public ITextComponent getTooltipMessage() {
        final IAEFluidStack fluid = this.tank.getFluidInSlot(this.slot);
        if (fluid != null && fluid.getStackSize() > 0) {
            return fluid.getFluid().getAttributes().getDisplayName(fluid.getFluidStack()).deepCopy()
                    .appendString("\n" + (fluid.getStackSize() + "mB"));
        }
        return StringTextComponent.EMPTY;
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
