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


import appeng.api.storage.data.IAEItemStack;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.util.ISlimReadableNumberConverter;
import appeng.util.IWideReadableNumberConverter;
import appeng.util.ReadableNumberConverter;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class StackSizeRenderer {
    private static final ISlimReadableNumberConverter SLIM_CONVERTER = ReadableNumberConverter.INSTANCE;
    private static final IWideReadableNumberConverter WIDE_CONVERTER = ReadableNumberConverter.INSTANCE;

    public void renderStackSize(FontRenderer fontRenderer, IAEItemStack aeStack, int xPos, int yPos) {
        if (aeStack != null) {
            final float scaleFactor = AEConfig.instance().useTerminalUseLargeFont() ? 0.85f : 0.5f;
            final float inverseScaleFactor = 1.0f / scaleFactor;
            final int offset = AEConfig.instance().useTerminalUseLargeFont() ? 0 : -1;

            final boolean unicodeFlag = fontRenderer.getUnicodeFlag();
            fontRenderer.setUnicodeFlag(false);

            if (aeStack.getStackSize() == 0 && aeStack.isCraftable()) {
                final String craftLabelText = AEConfig.instance().useTerminalUseLargeFont() ? GuiText.LargeFontCraft.getLocal() : GuiText.SmallFontCraft
                        .getLocal();
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                GlStateManager.pushMatrix();
                GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);
                final int X = (int) (((float) xPos + offset + 16.0f - fontRenderer.getStringWidth(craftLabelText) * scaleFactor) * inverseScaleFactor);
                final int Y = (int) (((float) yPos + offset + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
                fontRenderer.drawStringWithShadow(craftLabelText, X, Y, 16777215);
                GlStateManager.popMatrix();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
                GlStateManager.enableBlend();
            }

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
        if (AEConfig.instance().useTerminalUseLargeFont()) {
            return SLIM_CONVERTER.toSlimReadableForm(originalSize);
        } else {
            return WIDE_CONVERTER.toWideReadableForm(originalSize);
        }
    }

}
