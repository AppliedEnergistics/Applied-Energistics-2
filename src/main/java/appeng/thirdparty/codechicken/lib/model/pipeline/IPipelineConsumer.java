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

package appeng.thirdparty.codechicken.lib.model.pipeline;

import net.minecraftforge.client.model.pipeline.IVertexConsumer;

import appeng.thirdparty.codechicken.lib.model.CachedFormat;
import appeng.thirdparty.codechicken.lib.model.ISmartVertexConsumer;
import appeng.thirdparty.codechicken.lib.model.Quad;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadReInterpolator;

/**
 * Anything implementing this may be used in the BakedPipeline.
 *
 * @author covers1624
 */
public interface IPipelineConsumer extends ISmartVertexConsumer {

    /**
     * The quad at the start of the transformation. This is useful for obtaining the vertex data before any
     * transformations have been applied, such as interpolation, See {@link QuadReInterpolator}. When overriding this
     * make sure you call setInputQuad on your parent consumer too.
     *
     * @param quad The quad.
     */
    void setInputQuad(Quad quad);

    /**
     * Resets the Consumer to the new format. This should resize any internal arrays if needed, ready for the new vertex
     * data.
     *
     * @param format The format to reset to.
     */
    void reset(CachedFormat format);

    /**
     * Sets the parent consumer. This consumer may choose to not pipe any data, that's fine, but if it does, it MUST
     * pipe the data to the one provided here.
     *
     * @param parent The parent.
     */
    void setParent(IVertexConsumer parent);
}
