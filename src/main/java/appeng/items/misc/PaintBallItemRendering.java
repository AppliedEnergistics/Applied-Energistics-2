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

package appeng.items.misc;

import appeng.api.util.AEColor;
import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;

public class PaintBallItemRendering extends ItemRenderingCustomizer {

    private final boolean lumen;

    private final AEColor color;

    public PaintBallItemRendering(AEColor color, boolean lumen) {
        this.lumen = lumen;
        this.color = color;
    }

    @Override
    public void customize(IItemRendering rendering) {
        final int colorValue = lumen ? color.mediumVariant : color.mediumVariant;
        final int r = (colorValue >> 16) & 0xff;
        final int g = (colorValue >> 8) & 0xff;
        final int b = (colorValue) & 0xff;

        int renderColor;
        if (lumen) {
            final float fail = 0.7f;
            final int full = (int) (255 * 0.3);
            renderColor = (int) (full + r * fail) << 16 | (int) (full + g * fail) << 8 | (int) (full + b * fail)
                    | 0xff << 24;
        } else {
            renderColor = r << 16 | g << 8 | b | 0xff << 24;
        }

        rendering.color((is, tintIndex) -> renderColor);
    }

}
