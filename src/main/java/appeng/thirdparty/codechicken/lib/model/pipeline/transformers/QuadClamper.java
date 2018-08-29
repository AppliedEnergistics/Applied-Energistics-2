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


import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

import appeng.thirdparty.codechicken.lib.model.Quad.Vertex;
import appeng.thirdparty.codechicken.lib.model.pipeline.IPipelineElementFactory;
import appeng.thirdparty.codechicken.lib.model.pipeline.QuadTransformer;


/**
 * This transformer simply clamps the vertices inside the provided box.
 * You probably want to Re-Interpolate the UV's, Color, and Lmap, see {@link QuadReInterpolator}
 *
 * @author covers1624
 */
public class QuadClamper extends QuadTransformer
{

	public static IPipelineElementFactory<QuadClamper> FACTORY = QuadClamper::new;

	private AxisAlignedBB clampBounds;

	QuadClamper()
	{
		super();
	}

	public QuadClamper( IVertexConsumer parent, AxisAlignedBB bounds )
	{
		super( parent );
		this.clampBounds = bounds;
	}

	public void setClampBounds( AxisAlignedBB bounds )
	{
		this.clampBounds = bounds;
	}

	@Override
	public boolean transform()
	{
		int s = quad.orientation.ordinal() >> 1;

		quad.clamp( clampBounds );

		//Check if the quad would be invisible and cull it.
		Vertex[] vertices = quad.vertices;
		float x1 = vertices[0].dx( s );
		float x2 = vertices[1].dx( s );
		float x3 = vertices[2].dx( s );
		float x4 = vertices[3].dx( s );

		float y1 = vertices[0].dy( s );
		float y2 = vertices[1].dy( s );
		float y3 = vertices[2].dy( s );
		float y4 = vertices[3].dy( s );

		//These comparisons are safe as we are comparing clamped values.
		boolean flag1 = x1 == x2 && x2 == x3 && x3 == x4;
		boolean flag2 = y1 == y2 && y2 == y3 && y3 == y4;
		return !flag1 && !flag2;
	}
}
