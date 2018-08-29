/*
 * Copyright (c) 2018 covers1624
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
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
public interface IPipelineConsumer extends ISmartVertexConsumer
{

	/**
	 * The quad at the start of the transformation.
	 * This is useful for obtaining the vertex data before any transformations have been applied,
	 * such as interpolation, See {@link QuadReInterpolator}.
	 * When overriding this make sure you call setInputQuad on your parent consumer too.
	 *
	 * @param quad The quad.
	 */
	void setInputQuad( Quad quad );

	/**
	 * Resets the Consumer to the new format.
	 * This should resize any internal arrays if needed, ready for the new vertex data.
	 *
	 * @param format The format to reset to.
	 */
	void reset( CachedFormat format );

	/**
	 * Sets the parent consumer.
	 * This consumer may choose to not pipe any data,
	 * that's fine, but if it does, it MUST pipe the data to the one provided here.
	 *
	 * @param parent The parent.
	 */
	void setParent( IVertexConsumer parent );
}
