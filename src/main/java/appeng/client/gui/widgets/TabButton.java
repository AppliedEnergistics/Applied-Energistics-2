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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class TabButton extends Button implements ITooltip {
    public static final ResourceLocation TEXTURE_STATES = new ResourceLocation("appliedenergistics2",
            "textures/guis/states.png");
    private final ItemRenderer itemRenderer;
    private int hideEdge = 0;
    private int myIcon = -1;
    private ItemStack myItem;

    public TabButton(final int x, final int y, final int ico, final ITextComponent message, final ItemRenderer ir,
            IPressable onPress) {
        super(x, y, 22, 22, message, onPress);

        this.myIcon = ico;
        this.itemRenderer = ir;
    }

    /**
     * Using itemstack as an icon
     *
     * @param x       x pos of button
     * @param y       y pos of button
     * @param ico     used icon
     * @param message mouse over message
     * @param ir      renderer
     */
    public TabButton(final int x, final int y, final ItemStack ico, final ITextComponent message, final ItemRenderer ir,
            IPressable onPress) {
        super(x, y, 22, 22, message, onPress);
        this.myItem = ico;
        this.itemRenderer = ir;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, final int x, final int y, float partial) {
        final Minecraft minecraft = Minecraft.getInstance();

        if (this.visible) {
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            minecraft.textureManager.bindTexture(TEXTURE_STATES);

            RenderSystem.enableAlphaTest();

            int uv_x = (this.hideEdge > 0 ? 11 : 13);

            final int offsetX = this.hideEdge > 0 ? 1 : 0;

            blit(matrixStack, this.x, this.y, uv_x * 16, 0, 25, 22);

            if (this.myIcon >= 0) {
                final int uv_y = this.myIcon / 16;
                uv_x = this.myIcon - uv_y * 16;

                blit(matrixStack, offsetX + this.x + 3, this.y + 3, uv_x * 16, uv_y * 16, 16, 16);
            }

            RenderSystem.disableAlphaTest();

            if (this.myItem != null) {
                this.itemRenderer.zLevel = 100.0F;

                RenderHelper.enableStandardItemLighting();
                this.itemRenderer.renderItemAndEffectIntoGUI(this.myItem, offsetX + this.x + 3, this.y + 3);
                RenderHelper.disableStandardItemLighting();

                this.itemRenderer.zLevel = 0.0F;
            }
        }
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
        return 22;
    }

    @Override
    public int getHeight() {
        return 22;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    public int getHideEdge() {
        return this.hideEdge;
    }

    public void setHideEdge(final int hideEdge) {
        this.hideEdge = hideEdge;
    }
}
