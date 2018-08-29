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

package appeng.thirdparty.codechicken.lib.model;


import net.minecraftforge.client.model.pipeline.IVertexConsumer;


/**
 * Marks a standard IVertexConsumer as compatible with {@link Quad}.
 *
 * @author covers1624
 */
public interface ISmartVertexConsumer extends IVertexConsumer
{

	/**
	 * Assumes the data is already completely unpacked.
	 * You must always copy the data from the quad provided to an internal cache.
	 * basically:
	 * this.quad.put(quad);
	 *
	 * @param quad The quad to copy data from.
	 */
	void put( Quad quad );
}
