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


import appeng.thirdparty.codechicken.lib.model.Quad.Vertex;
import appeng.thirdparty.codechicken.lib.model.pipeline.IPipelineElementFactory;
import appeng.thirdparty.codechicken.lib.model.pipeline.QuadTransformer;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;


/**
 * This transformer simply overrides the alpha of the quad.
 * Only operates if the format has color.
 *
 * @author covers1624
 */
public class QuadAlphaOverride extends QuadTransformer {

    public static final IPipelineElementFactory<QuadAlphaOverride> FACTORY = QuadAlphaOverride::new;

    private float alphaOverride;

    QuadAlphaOverride() {
        super();
    }

    public QuadAlphaOverride(IVertexConsumer consumer, float alphaOverride) {
        super(consumer);
        this.alphaOverride = alphaOverride;
    }

    public QuadAlphaOverride setAlphaOverride(float alphaOverride) {
        this.alphaOverride = alphaOverride;
        return this;
    }

    @Override
    public boolean transform() {
        if (this.format.hasColor) {
            for (Vertex v : this.quad.vertices) {
                v.color[3] = this.alphaOverride;
            }
        }
        return true;
    }
}
