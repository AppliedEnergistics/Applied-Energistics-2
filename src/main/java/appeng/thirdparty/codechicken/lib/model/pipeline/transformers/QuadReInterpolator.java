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


import appeng.thirdparty.codechicken.lib.math.InterpHelper;
import appeng.thirdparty.codechicken.lib.model.CachedFormat;
import appeng.thirdparty.codechicken.lib.model.Quad;
import appeng.thirdparty.codechicken.lib.model.Quad.Vertex;
import appeng.thirdparty.codechicken.lib.model.pipeline.IPipelineElementFactory;
import appeng.thirdparty.codechicken.lib.model.pipeline.QuadTransformer;


/**
 * This transformer Re-Interpolates the Color, UV's and LightMaps.
 * Use this after all transformations that translate vertices in the pipeline.
 *
 * This Transformation can only be used in the BakedPipeline.
 *
 * @author covers1624
 */
public class QuadReInterpolator extends QuadTransformer
{

	public static final IPipelineElementFactory<QuadReInterpolator> FACTORY = QuadReInterpolator::new;

	private Quad interpCache = new Quad();
	private InterpHelper interpHelper = new InterpHelper();

	QuadReInterpolator()
	{
		super();
	}

	@Override
	public void reset( CachedFormat format )
	{
		super.reset( format );
		interpCache.reset( format );
	}

	@Override
	public void setInputQuad( Quad quad )
	{
		super.setInputQuad( quad );
		quad.resetInterp( interpHelper, quad.orientation.ordinal() >> 1 );
	}

	@Override
	public boolean transform()
	{
		int s = quad.orientation.ordinal() >> 1;
		if( format.hasColor || format.hasUV || format.hasLightMap )
		{
			interpCache.copyFrom( quad );
			interpHelper.setup();
			for( Vertex v : quad.vertices )
			{
				interpHelper.locate( v.dx( s ), v.dy( s ) );
				if( format.hasColor )
				{
					v.interpColorFrom( interpHelper, interpCache.vertices );
				}
				if( format.hasUV )
				{
					v.interpUVFrom( interpHelper, interpCache.vertices );
				}
				if( format.hasLightMap )
				{
					v.interpLightMapFrom( interpHelper, interpCache.vertices );
				}
			}
		}
		return true;
	}
}
