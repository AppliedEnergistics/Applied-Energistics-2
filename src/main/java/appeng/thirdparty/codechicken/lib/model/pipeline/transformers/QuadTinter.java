/*
 * This file is part of CodeChickenLib.
 * Copyright (c) 2018, covers1624, All rights reserved.
 *
 * CodeChickenLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * CodeChickenLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with CodeChickenLib. If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.thirdparty.codechicken.lib.model.pipeline.transformers;

import appeng.thirdparty.fabric.MutableQuadView;
import appeng.thirdparty.fabric.RenderContext;

/**
 * This transformer tints quads.. Feed it the output of BlockColors.colorMultiplier.
 *
 * @author covers1624
 */
public class QuadTinter implements RenderContext.QuadTransform {

    private final int abgr;

    public QuadTinter(int rgb) {
        this.abgr = 0xFF << 24 |
                ((rgb & 0xFF) << 16) |
                (rgb & 0xFF00) |
                ((rgb >> 16) & 0xFF);
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        // Nuke tintIndex.
        quad.colorIndex(-1);
        for (int i = 0; i < 4; i++) {
            int color = quad.spriteColor(i, 0);
            color = multiplyColor(color, abgr);
            quad.spriteColor(i, 0, color);
        }
        return true;
    }

    // Taken from Fabric Indigo's net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper
    /** Component-wise multiply. Components need to be in same order in both inputs! */
    private static int multiplyColor(int color1, int color2) {
        if (color1 == -1) {
            return color2;
        } else if (color2 == -1) {
            return color1;
        }

        final int alpha = ((color1 >> 24) & 0xFF) * ((color2 >> 24) & 0xFF) / 0xFF;
        final int red = ((color1 >> 16) & 0xFF) * ((color2 >> 16) & 0xFF) / 0xFF;
        final int green = ((color1 >> 8) & 0xFF) * ((color2 >> 8) & 0xFF) / 0xFF;
        final int blue = (color1 & 0xFF) * (color2 & 0xFF) / 0xFF;

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
