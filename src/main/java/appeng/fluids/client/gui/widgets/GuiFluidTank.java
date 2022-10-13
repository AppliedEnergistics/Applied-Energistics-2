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


import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.widgets.GuiCustomSlot;
import appeng.client.gui.widgets.ITooltip;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.fluids.util.IAEFluidTank;
import appeng.helpers.InventoryAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class GuiFluidTank extends GuiCustomSlot implements ITooltip {
    private final IAEFluidTank tank;
    private final int slot;
    private final int width;
    private final int height;
    private boolean darkened = false;

    public GuiFluidTank(IAEFluidTank tank, int slot, int id, int x, int y, int w, int h) {
        super(id, x, y);
        this.tank = tank;
        this.slot = slot;
        this.width = w;
        this.height = h;
    }

    public GuiFluidTank(IAEFluidTank tank, int slot, int id, int x, int y, int w, int h, boolean darkened) {
        super(id, x, y);
        this.tank = tank;
        this.slot = slot;
        this.width = w;
        this.height = h;
        this.darkened = darkened;
    }

    @Override
    public void drawContent(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        final IAEFluidStack fs = this.getFluidStack();
        if (fs != null) {
            GlStateManager.disableBlend();
            GlStateManager.disableLighting();

            //drawRect( this.x, this.y, this.x + this.width, this.y + this.height, AEColor.GRAY.blackVariant | 0xFF000000 );

            final IAEFluidStack fluid = this.tank.getFluidInSlot(this.slot);
            if (fluid != null && fluid.getStackSize() > 0) {
                mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

                float red = (fluid.getFluid().getColor() >> 16 & 255) / 255.0F;
                float green = (fluid.getFluid().getColor() >> 8 & 255) / 255.0F;
                float blue = (fluid.getFluid().getColor() & 255) / 255.0F;
                if (darkened) {
                    red = red * 0.4F;
                    green = green * 0.4F;
                    blue = blue * 0.4F;
                }
                GlStateManager.color(red, green, blue);

                TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(fluid.getFluid().getStill().toString());
                int scaledHeight = (int) (this.height * ((float) fluid.getStackSize() / this.tank.getTankProperties()[this.slot].getCapacity()));
                scaledHeight = Math.min(this.height, scaledHeight);

                int iconHeightRemainder = scaledHeight % 16;
                if (iconHeightRemainder > 0) {
                    this.drawTexturedModalRect(this.xPos(), this.yPos() + this.getHeight() - iconHeightRemainder, sprite, 16, iconHeightRemainder);
                }
                for (int i = 0; i < scaledHeight / 16; i++) {
                    this.drawTexturedModalRect(this.xPos(), this.yPos() + this.getHeight() - iconHeightRemainder - (i + 1) * 16, sprite, 16, 16);
                }
            }
        }
    }

    @Override
    public String getMessage() {
        final IAEFluidStack fluid = this.tank.getFluidInSlot(this.slot);
        if (fluid != null && fluid.getStackSize() > 0) {
            String desc = fluid.getFluid().getLocalizedName(fluid.getFluidStack());

            return desc + "\n" + fluid.getStackSize() + "/" + this.tank.getTankProperties()[this.slot].getCapacity() + "mB";
        }
        return null;
    }

    @Override
    public int xPos() {
        return this.x;
    }

    @Override
    public int yPos() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    public IAEFluidStack getFluidStack() {
        return this.tank.getFluidInSlot(this.slot);
    }

    @Override
    public void slotClicked(ItemStack clickStack, final int mouseButton) {
        if (getFluidStack() != null) {
            NetworkHandler.instance().sendToServer(new PacketInventoryAction(InventoryAction.FILL_ITEM, slot, id));
        } else {
            NetworkHandler.instance().sendToServer(new PacketInventoryAction(InventoryAction.EMPTY_ITEM, slot, id));
        }
    }

}
