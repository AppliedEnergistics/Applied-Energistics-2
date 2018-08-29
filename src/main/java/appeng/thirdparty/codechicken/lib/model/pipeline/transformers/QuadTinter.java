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

package appeng.thirdparty.codechicken.lib.model.pipeline.transformers;


import net.minecraftforge.client.model.pipeline.IVertexConsumer;

import appeng.thirdparty.codechicken.lib.model.Quad.Vertex;
import appeng.thirdparty.codechicken.lib.model.pipeline.IPipelineElementFactory;
import appeng.thirdparty.codechicken.lib.model.pipeline.QuadTransformer;


/**
 * This transformer tints quads..
 * Feed it the output of BlockColors.colorMultiplier.
 *
 * @author covers1624
 */
public class QuadTinter extends QuadTransformer
{

	public static final IPipelineElementFactory<QuadTinter> FACTORY = QuadTinter::new;

	private int tint;

	QuadTinter()
	{
		super();
	}

	public QuadTinter( IVertexConsumer consumer, int tint )
	{
		super( consumer );
		this.tint = tint;
	}

	public QuadTinter setTint( int tint )
	{
		this.tint = tint;
		return this;
	}

	@Override
	public boolean transform()
	{
		//Nuke tintIndex.
		quad.tintIndex = -1;
		if( format.hasColor )
		{
			float r = (float) ( tint >> 0x10 & 0xFF ) / 255F;
			float g = (float) ( tint >> 0x08 & 0xFF ) / 255F;
			float b = (float) ( tint & 0xFF ) / 255F;
			for( Vertex v : quad.vertices )
			{
				v.color[0] *= r;
				v.color[1] *= g;
				v.color[2] *= b;
			}
		}
		return true;
	}
}
