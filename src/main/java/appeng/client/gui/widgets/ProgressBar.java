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

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import appeng.client.gui.style.Blitter;
import appeng.container.interfaces.IProgressProvider;
import appeng.core.localization.GuiText;

public class ProgressBar extends Widget implements ITooltip {

    private final IProgressProvider source;
    private final Blitter blitter;
    private final Direction layout;
    private final Rectangle2d sourceRect;
    private final ITextComponent titleName;
    private ITextComponent fullMsg;

    public ProgressBar(IProgressProvider source, Blitter blitter, Direction dir) {
        this(source, blitter, dir, null);
    }

    public ProgressBar(final IProgressProvider source, Blitter blitter,
            final Direction dir, final ITextComponent title) {
        super(0, 0, blitter.getSrcWidth(), blitter.getSrcHeight(), StringTextComponent.EMPTY);
        this.source = source;
        this.blitter = blitter.copy();
        this.layout = dir;
        this.titleName = title;
        this.sourceRect = new Rectangle2d(
                blitter.getSrcX(),
                blitter.getSrcY(),
                blitter.getSrcWidth(),
                blitter.getSrcHeight());
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            int max = this.source.getMaxProgress();
            int current = this.source.getCurrentProgress();

            int srcX = sourceRect.getX();
            int srcY = sourceRect.getY();
            int srcW = sourceRect.getWidth();
            int srcH = sourceRect.getHeight();
            int destX = x;
            int destY = y;

            if (this.layout == Direction.VERTICAL) {
                int diff = this.height - (max > 0 ? (this.height * current) / max : 0);
                destY += diff;
                srcY += diff;
                srcH -= diff;
            } else {
                int diff = this.width - (max > 0 ? (this.width * current) / max : 0);
                srcX += diff;
                srcW -= diff;
            }

            blitter.src(srcX, srcY, srcW, srcH).dest(destX, destY).blit(matrices, getBlitOffset());
        }
    }

    public void setFullMsg(final ITextComponent msg) {
        this.fullMsg = msg;
    }

    @Override
    public List<ITextComponent> getTooltipMessage() {
        if (this.fullMsg != null) {
            return Collections.singletonList(this.fullMsg);
        }

        ITextComponent result = this.titleName != null ? this.titleName : StringTextComponent.EMPTY;
        return Arrays.asList(
                result,
                new StringTextComponent(this.source.getCurrentProgress() + " ")
                        .append(GuiText.Of.text().deepCopy().appendString(" " + this.source.getMaxProgress())));
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

    public enum Direction {
        HORIZONTAL, VERTICAL
    }
}
