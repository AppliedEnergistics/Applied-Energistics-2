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

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.util.Icon;
import appeng.client.gui.style.Blitter;

public abstract class IconButton extends Button implements ITooltip {

    private boolean halfSize = false;

    private boolean disableClickSound = false;

    private boolean disableBackground = false;

    public IconButton(OnPress onPress) {
        super(0, 0, 16, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
    }

    public void setVisibility(boolean vis) {
        this.visible = vis;
        this.active = vis;
    }

    @Override
    public void playDownSound(SoundManager soundHandler) {
        if (!disableClickSound) {
            super.playDownSound(soundHandler);
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {

        if (this.visible) {
            var icon = this.getIcon();
            var item = this.getItemOverlay();

            if (this.halfSize) {
                this.width = 8;
                this.height = 8;
            }

            var yOffset = isHovered() ? 1 : 0;

            if (this.halfSize) {
                if (!disableBackground) {
                    Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter().dest(getX(), getY()).zOffset(10).blit(guiGraphics);
                }
                if (item != null) {
                    guiGraphics.renderItem(new ItemStack(item), getX(), getY(), 0, 20);
                } else if (icon != null) {
                    Blitter blitter = icon.getBlitter();
                    if (!this.active) {
                        blitter.opacity(0.5f);
                    }
                    blitter.dest(getX(), getY()).zOffset(20).blit(guiGraphics);
                }
            } else {
                if (!disableBackground) {
                    Icon bgIcon = isHovered() ? Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER
                            : isFocused() ? Icon.TOOLBAR_BUTTON_BACKGROUND_FOCUS : Icon.TOOLBAR_BUTTON_BACKGROUND;

                    bgIcon.getBlitter()
                            .dest(getX() - 1, getY() + yOffset, 18, 20)
                            .zOffset(2)
                            .blit(guiGraphics);
                }
                if (item != null) {
                    guiGraphics.renderItem(new ItemStack(item), getX(), getY() + 1 + yOffset, 0, 3);
                } else if (icon != null) {
                    icon.getBlitter().dest(getX(), getY() + 1 + yOffset).zOffset(3).blit(guiGraphics);
                }
            }
        }
    }

    protected abstract Icon getIcon();

    /**
     * Prioritized over {@link #getIcon()} if not null.
     */
    @Nullable
    protected Item getItemOverlay() {
        return null;
    }

    @Override
    public List<Component> getTooltipMessage() {
        return Collections.singletonList(getMessage());
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(
                getX(),
                getY(),
                this.halfSize ? 8 : 16,
                this.halfSize ? 8 : 16);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return this.visible;
    }

    public boolean isHalfSize() {
        return this.halfSize;
    }

    public void setHalfSize(boolean halfSize) {
        this.halfSize = halfSize;
    }

    public boolean isDisableClickSound() {
        return disableClickSound;
    }

    public void setDisableClickSound(boolean disableClickSound) {
        this.disableClickSound = disableClickSound;
    }

    public boolean isDisableBackground() {
        return disableBackground;
    }

    public void setDisableBackground(boolean disableBackground) {
        this.disableBackground = disableBackground;
    }

}
