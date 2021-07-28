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

import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.client.gui.Icon;

public class TabButton extends Button implements ITooltip {
    private final ItemRenderer itemRenderer;
    private boolean hideEdge;
    private Icon icon = null;
    private ItemStack item;

    public TabButton(final Icon ico, final Component message, final ItemRenderer ir,
            OnPress onPress) {
        super(0, 0, 22, 22, message, onPress);

        this.icon = ico;
        this.itemRenderer = ir;
    }

    /**
     * Using itemstack as an icon
     *
     * @param ico     used icon
     * @param message mouse over message
     * @param ir      renderer
     */
    public TabButton(final ItemStack ico, final Component message, final ItemRenderer ir,
            OnPress onPress) {
        super(0, 0, 22, 22, message, onPress);
        this.item = ico;
        this.itemRenderer = ir;
    }

    @Override
    public void renderButton(PoseStack matrixStack, final int x, final int y, float partial) {
        if (this.visible) {
            // Selects the button border from the sprite-sheet, where each type occupies a
            // 2x2 slot
            Icon backdrop;
            if (this.hideEdge) {
                backdrop = this.isFocused() ? Icon.TAB_BUTTON_BACKGROUND_BORDERLESS_FOCUS
                        : Icon.TAB_BUTTON_BACKGROUND_BORDERLESS;
            } else {
                backdrop = this.isFocused() ? Icon.TAB_BUTTON_BACKGROUND_FOCUS : Icon.TAB_BUTTON_BACKGROUND;
            }

            backdrop.getBlitter().dest(this.x, this.y).blit(matrixStack, getBlitOffset());

            final int offsetX = this.hideEdge ? 1 : 0;
            if (this.icon != null) {
                this.icon.getBlitter().dest(offsetX + this.x + 3, this.y + 3).blit(matrixStack, getBlitOffset());
            }

            if (this.item != null) {
                this.itemRenderer.blitOffset = 100.0F;
                this.itemRenderer.renderAndDecorateItem(this.item, offsetX + this.x + 3, this.y + 3);
                this.itemRenderer.blitOffset = 0.0F;
            }
        }
    }

    @Override
    public List<Component> getTooltipMessage() {
        return Collections.singletonList(getMessage());
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
        return 22;
    }

    @Override
    public int getTooltipAreaHeight() {
        return 22;
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return this.visible;
    }

    public boolean getHideEdge() {
        return this.hideEdge;
    }

    public void setHideEdge(final boolean hideEdge) {
        this.hideEdge = hideEdge;
    }
}
