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

package appeng.client.gui.me.common;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;

import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.util.ISlimReadableNumberConverter;
import appeng.util.IWideReadableNumberConverter;
import appeng.util.ReadableNumberConverter;

/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class StackSizeRenderer {
    protected static final ISlimReadableNumberConverter SLIM_CONVERTER = ReadableNumberConverter.INSTANCE;
    protected static final IWideReadableNumberConverter WIDE_CONVERTER = ReadableNumberConverter.INSTANCE;

    public void renderStackSize(Font fontRenderer, long stackSize, boolean craftable, int xPos, int yPos) {
        if (stackSize == 0 && craftable) {
            final String craftLabelText = AEConfig.instance().isUseLargeFonts() ? GuiText.LargeFontCraft.getLocal()
                    : GuiText.SmallFontCraft.getLocal();

            renderSizeLabel(fontRenderer, xPos, yPos, craftLabelText);
        }

        if (stackSize > 0) {
            renderSizeLabel(fontRenderer, xPos, yPos, this.getToBeRenderedStackSize(stackSize));
        }
    }

    public void renderSizeLabel(Font fontRenderer, float xPos, float yPos, String text) {

        final float scaleFactor = AEConfig.instance().isUseLargeFonts() ? 0.85f : 0.5f;
        final float inverseScaleFactor = 1.0f / scaleFactor;
        final int offset = AEConfig.instance().isUseLargeFonts() ? 0 : -1;

        Transformation tm = new Transformation(new Vector3f(0, 0, 300), // Taken from
                // ItemRenderer.renderItemOverlayIntoGUI
                null, new Vector3f(scaleFactor, scaleFactor, scaleFactor), null);

        RenderSystem.disableBlend();
        final int X = (int) ((xPos + offset + 16.0f - fontRenderer.width(text) * scaleFactor)
                * inverseScaleFactor);
        final int Y = (int) ((yPos + offset + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
        BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        fontRenderer.drawInBatch(text, X, Y, 0xffffff, true, tm.getMatrix(), buffer, false, 0, 15728880);
        buffer.endBatch();
        RenderSystem.enableBlend();
    }

    protected String getToBeRenderedStackSize(final long originalSize) {
        if (AEConfig.instance().isUseLargeFonts()) {
            return SLIM_CONVERTER.toSlimReadableForm(originalSize);
        } else {
            return WIDE_CONVERTER.toWideReadableForm(originalSize);
        }
    }

}
