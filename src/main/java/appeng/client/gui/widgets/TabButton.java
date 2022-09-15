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
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.client.gui.Icon;

public class TabButton extends Button implements ITooltip {
    private final ItemRenderer itemRenderer;
    private Style style = Style.BOX;
    private Icon icon = null;
    private ItemStack item;

    private boolean selected;

    public enum Style {
        CORNER,
        BOX,
        HORIZONTAL
    }

    public TabButton(Icon ico, Component message, ItemRenderer ir,
            OnPress onPress) {
        super(0, 0, 22, 22, message, onPress, Button.DEFAULT_NARRATION);

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
    public TabButton(ItemStack ico, Component message, ItemRenderer ir,
            OnPress onPress) {
        super(0, 0, 22, 22, message, onPress, Button.DEFAULT_NARRATION);
        this.item = ico;
        this.itemRenderer = ir;
    }

    @Override
    public void renderButton(PoseStack poseStack, int x, int y, float partial) {
        if (this.visible) {
            // Selects the button border from the sprite-sheet, where each type occupies a
            // 2x2 slot
            var backdrop = switch (this.style) {
                case CORNER -> this.isFocused() ? Icon.TAB_BUTTON_BACKGROUND_BORDERLESS_FOCUS
                        : Icon.TAB_BUTTON_BACKGROUND_BORDERLESS;
                case BOX -> this.isFocused() ? Icon.TAB_BUTTON_BACKGROUND_FOCUS : Icon.TAB_BUTTON_BACKGROUND;
                case HORIZONTAL -> {
                    if (this.isFocused()) {
                        yield Icon.HORIZONTAL_TAB_FOCUS;
                    } else if (this.selected) {
                        yield Icon.HORIZONTAL_TAB_SELECTED;
                    }
                    yield Icon.HORIZONTAL_TAB;
                }
            };

            backdrop.getBlitter().dest(getX(), getY()).blit(poseStack, getBlitOffset());

            var iconX = switch (this.style) {
                case CORNER -> 4;
                case BOX -> 3;
                case HORIZONTAL -> 1;
            };
            var iconY = 3;

            if (this.icon != null) {
                this.icon.getBlitter().dest(getX() + iconX, getY() + iconY).blit(poseStack, getBlitOffset());
            }

            if (this.item != null) {
                this.itemRenderer.blitOffset = 100.0F;
                this.itemRenderer.renderAndDecorateItem(this.item, getX() + iconX, getY() + iconY);
                this.itemRenderer.blitOffset = 0.0F;
            }
        }
    }

    @Override
    public List<Component> getTooltipMessage() {
        return Collections.singletonList(getMessage());
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(getX(), getY(), 22, 22);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return this.visible;
    }

    public Style getStyle() {
        return this.style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
