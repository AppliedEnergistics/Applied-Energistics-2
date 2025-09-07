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

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import appeng.core.AEConfig;

public class StackSizeRenderer {
    public static void renderSizeLabel(GuiGraphics guiGraphics, Font fontRenderer, float xPos, float yPos,
            String text) {
        renderSizeLabel(guiGraphics, fontRenderer, xPos, yPos, text, AEConfig.instance().isUseLargeFonts());
    }

    public static void renderSizeLabel(GuiGraphics guiGraphics, Font fontRenderer, float xPos, float yPos, String text,
            boolean largeFonts) {
        float scaleFactor = largeFonts ? 0.85f : 0.666f;
        float inverseScaleFactor = 1.0f / scaleFactor;
        int offset = largeFonts ? 0 : -1;

        int x = (int) ((xPos + offset + 16.0f + 2.0f - fontRenderer.width(text) * scaleFactor)
                * inverseScaleFactor);
        int y = (int) ((yPos + offset + 16.0f - 5.0f * scaleFactor) * inverseScaleFactor);

        guiGraphics.nextStratum();
        var stack = guiGraphics.pose();
        stack.pushMatrix();
        stack.scale(scaleFactor);

        guiGraphics.drawString(fontRenderer, text, x, y, -1);

        stack.popMatrix();
    }

}
