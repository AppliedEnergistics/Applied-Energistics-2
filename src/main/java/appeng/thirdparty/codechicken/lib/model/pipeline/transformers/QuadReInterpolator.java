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

import appeng.thirdparty.codechicken.lib.math.InterpHelper;
import appeng.thirdparty.codechicken.lib.model.CachedFormat;
import appeng.thirdparty.codechicken.lib.model.Quad;
import appeng.thirdparty.codechicken.lib.model.Quad.Vertex;
import appeng.thirdparty.codechicken.lib.model.pipeline.IPipelineElementFactory;
import appeng.thirdparty.codechicken.lib.model.pipeline.QuadTransformer;

/**
 * This transformer Re-Interpolates the Color, UV's and LightMaps. Use this after all transformations that translate
 * vertices in the pipeline.
 * <p>
 * This Transformation can only be used in the BakedPipeline.
 *
 * @author covers1624
 */
public class QuadReInterpolator extends QuadTransformer {

    public static final IPipelineElementFactory<QuadReInterpolator> FACTORY = QuadReInterpolator::new;

    private Quad interpCache = new Quad();
    private InterpHelper interpHelper = new InterpHelper();

    QuadReInterpolator() {
        super();
    }

    @Override
    public void reset(CachedFormat format) {
        super.reset(format);
        this.interpCache.reset(format);
    }

    @Override
    public void setInputQuad(Quad quad) {
        super.setInputQuad(quad);
        quad.resetInterp(this.interpHelper, quad.orientation.ordinal() >> 1);
    }

    @Override
    public boolean transform() {
        int s = this.quad.orientation.ordinal() >> 1;
        if (this.format.hasColor || this.format.hasUV || this.format.hasLightMap) {
            this.interpCache.copyFrom(this.quad);
            this.interpHelper.setup();
            for (Vertex v : this.quad.vertices) {
                this.interpHelper.locate(v.dx(s), v.dy(s));
                if (this.format.hasColor) {
                    v.interpColorFrom(this.interpHelper, this.interpCache.vertices);
                }
                if (this.format.hasUV) {
                    v.interpUVFrom(this.interpHelper, this.interpCache.vertices);
                }
                if (this.format.hasLightMap) {
                    v.interpLightMapFrom(this.interpHelper, this.interpCache.vertices);
                }
            }
        }
        return true;
    }
}
