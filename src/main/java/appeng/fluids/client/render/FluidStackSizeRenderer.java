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

package appeng.fluids.client.render;


import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AEConfig;
import appeng.util.ISlimReadableNumberConverter;
import appeng.util.IWideReadableNumberConverter;
import appeng.util.ReadableNumberConverter;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @author yueh
 * @version rv6
 * @since rv6
 */
public class FluidStackSizeRenderer {

    private static final String[] NUMBER_FORMATS = new String[]{"#.000", "#.00", "#.0", "#"};

    private static final ISlimReadableNumberConverter SLIM_CONVERTER = ReadableNumberConverter.INSTANCE;
    private static final IWideReadableNumberConverter WIDE_CONVERTER = ReadableNumberConverter.INSTANCE;

    public void renderStackSize(FontRenderer fontRenderer, IAEFluidStack aeStack, int xPos, int yPos) {
        if (aeStack != null) {
            final float scaleFactor = AEConfig.instance().useTerminalUseLargeFont() ? 0.85f : 0.5f;
            final float inverseScaleFactor = 1.0f / scaleFactor;
            final int offset = AEConfig.instance().useTerminalUseLargeFont() ? 0 : -1;

            final boolean unicodeFlag = fontRenderer.getUnicodeFlag();
            fontRenderer.setUnicodeFlag(false);

            if (aeStack.getStackSize() > 0) {
                final String stackSize = this.getToBeRenderedStackSize(aeStack.getStackSize());

                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                GlStateManager.pushMatrix();
                GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);
                final int X = (int) (((float) xPos + offset + 16.0f - fontRenderer.getStringWidth(stackSize) * scaleFactor) * inverseScaleFactor);
                final int Y = (int) (((float) yPos + offset + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
                fontRenderer.drawStringWithShadow(stackSize, X, Y, 16777215);
                GlStateManager.popMatrix();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
                GlStateManager.enableBlend();
            }

            fontRenderer.setUnicodeFlag(unicodeFlag);
        }
    }

    private String getToBeRenderedStackSize(final long originalSize) {
        // Handle any value below 100 (large font) or 1000 (small font) Buckets with a custom formatter,
        // otherwise pass it to the normal number converter
        if (originalSize < 1000 * 100 && AEConfig.instance().useTerminalUseLargeFont()) {
            return this.getSlimRenderedStacksize(originalSize);
        } else if (originalSize < 1000 * 1000 && !AEConfig.instance().useTerminalUseLargeFont()) {
            return this.getWideRenderedStacksize(originalSize);
        }

        if (AEConfig.instance().useTerminalUseLargeFont()) {
            return SLIM_CONVERTER.toSlimReadableForm(originalSize / 1000);
        } else {
            return WIDE_CONVERTER.toWideReadableForm(originalSize / 1000);
        }
    }

    private String getSlimRenderedStacksize(final long originalSize) {
        final int log = 1 + (int) Math.floor(Math.log10(originalSize)) / 2;

        return this.getRenderedFluidStackSize(originalSize, log);
    }

    private String getWideRenderedStacksize(final long originalSize) {
        final int log = (int) Math.floor(Math.log10(originalSize)) / 2;

        return this.getRenderedFluidStackSize(originalSize, log);
    }

    private String getRenderedFluidStackSize(final long originalSize, final int log) {
        final int index = Math.max(0, Math.min(3, log));

        final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        final DecimalFormat format = new DecimalFormat(NUMBER_FORMATS[index]);
        format.setDecimalFormatSymbols(symbols);
        format.setRoundingMode(RoundingMode.DOWN);

        return format.format(originalSize / 1000d);
    }

}
