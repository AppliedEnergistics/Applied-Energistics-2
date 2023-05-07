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

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.client.gui.Icon;
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
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partial) {

        if (this.visible) {
            var icon = this.getIcon();

            Blitter blitter = icon.getBlitter();
            if (!this.active) {
                blitter.opacity(0.5f);
            }

            if (this.halfSize) {
                this.width = 8;
                this.height = 8;
            }

            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend(); // FIXME: This should be the _default_ state, but some vanilla widget disables

            if (isFocused()) {
                // Draw 1px border with 4 quads, don't rely on the background as it can be disabled.
                // top
                fill(poseStack, getX() - 1, getY() - 1, getX() + width + 1, getY(), 0xFFFFFFFF);
                // left
                fill(poseStack, getX() - 1, getY(), getX(), getY() + height, 0xFFFFFFFF);
                // right
                fill(poseStack, getX() + width, getY(), getX() + width + 1, getY() + height, 0xFFFFFFFF);
                // bottom
                fill(poseStack, getX() - 1, getY() + height, getX() + width + 1, getY() + height + 1, 0xFFFFFFFF);
            }

            if (this.halfSize) {
                poseStack.pushPose();
                poseStack.translate(getX(), getY(), 0.0F);
                poseStack.scale(0.5f, 0.5f, 1.f);

                if (!disableBackground) {
                    Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter().dest(0, 0).blit(poseStack);
                }
                blitter.dest(0, 0).blit(poseStack);
                poseStack.popPose();
            } else {
                if (!disableBackground) {
                    Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter().dest(getX(), getY()).blit(poseStack);
                }
                icon.getBlitter().dest(getX(), getY()).blit(poseStack);
            }
            RenderSystem.enableDepthTest();

            var item = this.getItemOverlay();
            if (item != null) {
                Minecraft.getInstance().getItemRenderer().renderGuiItem(poseStack, new ItemStack(item), getX(), getY());
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
