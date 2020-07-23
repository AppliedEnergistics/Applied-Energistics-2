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
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;

/**
 * This transformer tints quads.. Feed it the output of
 * BlockColors.colorMultiplier.
 *
 * @author covers1624
 */
public class QuadTinter implements RenderContext.QuadTransform {

    private int tint;

    QuadTinter() {
        super();
    }

    public QuadTinter(int tint) {
        this.tint = tint | 0xFF000000;
    }

    public QuadTinter setTint(int tint) {
        this.tint = tint;
        return this;
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        // Nuke tintIndex.
        quad.colorIndex(-1);
        for (int i = 0; i < 4; i++) {
            int color = quad.spriteColor(i, 0);
            color = ColorHelper.multiplyColor(color, tint);
            quad.spriteColor(i, 0, color);
        }
        return true;
    }
}
