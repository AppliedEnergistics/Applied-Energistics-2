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

package appeng.client.render;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Vec3f;

import appeng.api.storage.data.IAEItemStack;
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
    private static final ISlimReadableNumberConverter SLIM_CONVERTER = ReadableNumberConverter.INSTANCE;
    private static final IWideReadableNumberConverter WIDE_CONVERTER = ReadableNumberConverter.INSTANCE;

    public void renderStackSize(TextRenderer fontRenderer, IAEItemStack aeStack, int xPos, int yPos) {
        if (aeStack != null) {
            if (aeStack.getStackSize() == 0 && aeStack.isCraftable()) {
                final Text craftLabelText = AEConfig.instance().isUseLargeFonts() ? GuiText.LargeFontCraft.text()
                        : GuiText.SmallFontCraft.text();

                renderSizeLabel(fontRenderer, xPos, yPos, craftLabelText);
            }

            if (aeStack.getStackSize() > 0) {
                final String stackSize = this.getToBeRenderedStackSize(aeStack.getStackSize());

                renderSizeLabel(fontRenderer, xPos, yPos, new LiteralText(stackSize));
            }

        }
    }

    public static void renderSizeLabel(TextRenderer fontRenderer, float xPos, float yPos, Text text) {

        final float scaleFactor = AEConfig.instance().isUseLargeFonts() ? 0.85f : 0.5f;
        final float inverseScaleFactor = 1.0f / scaleFactor;
        final int offset = AEConfig.instance().isUseLargeFonts() ? 0 : -1;

        AffineTransformation tm = new AffineTransformation(new Vec3f(0, 0, 300), // Taken from
                // ItemRenderer.renderItemOverlayIntoGUI
                null, new Vec3f(scaleFactor, scaleFactor, scaleFactor), null);

        RenderSystem.disableBlend();
        final int X = (int) ((xPos + offset + 16.0f - fontRenderer.getWidth(text) * scaleFactor) * inverseScaleFactor);
        final int Y = (int) ((yPos + offset + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
        VertexConsumerProvider.Immediate buffer = VertexConsumerProvider
                .immediate(Tessellator.getInstance().getBuffer());
        fontRenderer.draw(text, X, Y, 16777215, true, tm.getMatrix(), buffer, false, 0, 15728880);
        buffer.draw();
        RenderSystem.enableBlend();
    }

    private String getToBeRenderedStackSize(final long originalSize) {
        if (AEConfig.instance().isUseLargeFonts()) {
            return SLIM_CONVERTER.toSlimReadableForm(originalSize);
        } else {
            return WIDE_CONVERTER.toWideReadableForm(originalSize);
        }
    }

}
