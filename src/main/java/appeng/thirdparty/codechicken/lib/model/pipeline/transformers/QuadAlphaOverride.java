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
 * This transformer simply overrides the alpha of the quad.
 * Only operates if the format has color.
 *
 * @author covers1624
 */
public class QuadAlphaOverride extends QuadTransformer
{

	public static final IPipelineElementFactory<QuadAlphaOverride> FACTORY = QuadAlphaOverride::new;

	private float alphaOverride;

	QuadAlphaOverride()
	{
		super();
	}

	public QuadAlphaOverride( IVertexConsumer consumer, float alphaOverride )
	{
		super( consumer );
		this.alphaOverride = alphaOverride;
	}

	public QuadAlphaOverride setAlphaOverride( float alphaOverride )
	{
		this.alphaOverride = alphaOverride;
		return this;
	}

	@Override
	public boolean transform()
	{
		if( format.hasColor )
		{
			for( Vertex v : quad.vertices )
			{
				v.color[3] = alphaOverride;
			}
		}
		return true;
	}
}
