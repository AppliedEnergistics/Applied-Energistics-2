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

import net.minecraftforge.client.model.pipeline.IVertexConsumer;

import appeng.thirdparty.codechicken.lib.model.Quad.Vertex;
import appeng.thirdparty.codechicken.lib.model.pipeline.IPipelineElementFactory;
import appeng.thirdparty.codechicken.lib.model.pipeline.QuadTransformer;

/**
 * This transformer tints quads.. Feed it the output of BlockColors.colorMultiplier.
 *
 * @author covers1624
 */
public class QuadTinter extends QuadTransformer {

    public static final IPipelineElementFactory<QuadTinter> FACTORY = QuadTinter::new;

    private int tint;

    QuadTinter() {
        super();
    }

    public QuadTinter(IVertexConsumer consumer, int tint) {
        super(consumer);
        this.tint = tint;
    }

    public QuadTinter setTint(int tint) {
        this.tint = tint;
        return this;
    }

    @Override
    public boolean transform() {
        // Nuke tintIndex.
        this.quad.tintIndex = -1;
        if (this.format.hasColor) {
            float r = (this.tint >> 0x10 & 0xFF) / 255F;
            float g = (this.tint >> 0x08 & 0xFF) / 255F;
            float b = (this.tint & 0xFF) / 255F;
            for (Vertex v : this.quad.vertices) {
                v.color[0] *= r;
                v.color[1] *= g;
                v.color[2] *= b;
            }
        }
        return true;
    }
}
