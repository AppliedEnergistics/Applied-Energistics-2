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

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

/**
 * This transformer simply overrides the alpha of the quad.
 *
 * @author covers1624
 */
public class QuadAlphaOverride implements RenderContext.QuadTransform {

    private static final int INV_ALPHA_MASK = 0x00_FF_FF_FF;

    private final int alphaOverride;

    public QuadAlphaOverride(float alphaOverride) {
        this.alphaOverride = (int) (alphaOverride * 255) << 24;
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        for (int i = 0; i < 4; i++) {
            int color = (quad.spriteColor(i, 0) & INV_ALPHA_MASK) | alphaOverride;
            quad.spriteColor(i, 0, color);
        }
        return true;
    }
}
