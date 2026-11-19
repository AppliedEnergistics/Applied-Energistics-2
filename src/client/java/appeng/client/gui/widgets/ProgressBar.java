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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import appeng.client.gui.style.SpriteRenderer;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.menu.interfaces.IProgressProvider;

public class ProgressBar extends AbstractWidget implements ITooltip {

    /**
     * The default sprite for a filled progress bar. Its empty track is part of the screen background (see
     * {@code ae2:progress_bar_track}).
     */
    public static final Identifier DEFAULT_SPRITE = AppEng.makeId("progress_bar");

    private final IProgressProvider source;
    private final Identifier sprite;
    private final Direction layout;
    private final Component titleName;
    private Component fullMsg;

    public ProgressBar(IProgressProvider source, Direction dir) {
        this(source, DEFAULT_SPRITE, dir, null);
    }

    public ProgressBar(IProgressProvider source, Direction dir, Component title) {
        this(source, DEFAULT_SPRITE, dir, title);
    }

    public ProgressBar(IProgressProvider source, Identifier sprite, Direction dir, @Nullable Component title) {
        super(0, 0, 0, 0, Component.empty());
        var spriteContents = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI)
                .getSprite(sprite).contents();
        this.setSize(spriteContents.width(), spriteContents.height());
        this.source = source;
        this.sprite = sprite;
        this.layout = dir;
        this.titleName = title;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            int max = this.source.getMaxProgress();
            int current = this.source.getCurrentProgress();

            if (current > max) {
                current = max;
            }

            int srcX = 0;
            int srcY = 0;
            int srcW = this.width;
            int srcH = this.height;
            int destX = getX();
            int destY = getY();

            if (this.layout == Direction.VERTICAL) {
                int diff = this.height - (max > 0 ? this.height * current / max : 0);
                destY += diff;
                srcY += diff;
                srcH -= diff;
            } else {
                int diff = this.width - (max > 0 ? this.width * current / max : 0);
                srcX += diff;
                srcW -= diff;
            }

            SpriteRenderer.of(guiGraphics).blitRegion(sprite, srcX, srcY, srcW, srcH, destX, destY, srcW, srcH, false,
                    -1);
        }
    }

    public void setFullMsg(Component msg) {
        this.fullMsg = msg;
    }

    @Override
    public List<Component> getTooltipMessage() {
        if (this.fullMsg != null) {
            return Collections.singletonList(this.fullMsg);
        }

        Component result = this.titleName != null ? this.titleName : Component.empty();
        return Arrays.asList(
                result,
                Component.literal(this.source.getCurrentProgress() + " ")
                        .append(GuiText.Of.text().copy().append(" " + this.source.getMaxProgress())));
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(getX() - 2, getY() - 2, width + 4, height + 4);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return true;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
    }

    public enum Direction {
        HORIZONTAL, VERTICAL
    }
}
