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


import javax.annotation.OverridingMethodsMustInvokeSuper;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

import appeng.thirdparty.codechicken.lib.model.CachedFormat;
import appeng.thirdparty.codechicken.lib.model.ISmartVertexConsumer;
import appeng.thirdparty.codechicken.lib.model.Quad;


/**
 * Base class for a simple QuadTransformer.
 * Operates on BakedQuads.
 * QuadTransformers can be piped into each other at no performance penalty.
 *
 * @author covers1624
 */
public abstract class QuadTransformer implements IVertexConsumer, ISmartVertexConsumer, IPipelineConsumer
{

	protected CachedFormat format;
	protected IVertexConsumer consumer;
	protected Quad quad;

	/**
	 * Used for the BakedPipeline.
	 */
	protected QuadTransformer()
	{
		quad = new Quad();
	}

	public QuadTransformer( IVertexConsumer consumer )
	{
		this( consumer.getVertexFormat(), consumer );
	}

	public QuadTransformer( VertexFormat format, IVertexConsumer consumer )
	{
		this( CachedFormat.lookup( format ), consumer );
	}

	public QuadTransformer( CachedFormat format, IVertexConsumer consumer )
	{
		this.format = format;
		this.consumer = consumer;
		quad = new Quad( format );
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void reset( CachedFormat format )
	{
		this.format = format;
		quad.reset( format );
	}

	@Override
	public void setParent( IVertexConsumer parent )
	{
		this.consumer = parent;
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void setInputQuad( Quad quad )
	{
		if( consumer instanceof IPipelineConsumer )
		{
			( (IPipelineConsumer) consumer ).setInputQuad( quad );
		}
	}

	//@formatter:off
	@Override public VertexFormat getVertexFormat() { return format.format; }
	@Override public void setQuadTint( int tint ) { quad.setQuadTint( tint ); }
	@Override public void setQuadOrientation( EnumFacing orientation ) { quad.setQuadOrientation( orientation ); }
	@Override public void setApplyDiffuseLighting( boolean diffuse ) { quad.setApplyDiffuseLighting( diffuse ); }
	@Override public void setTexture( TextureAtlasSprite texture ) { quad.setTexture( texture ); }
	//@formatter:on

	@Override
	public void put( int element, float... data )
	{
		quad.put( element, data );
		if( quad.full )
		{
			onFull();
		}
	}

	@Override
	public void put( Quad quad )
	{
		this.quad.put( quad );
		onFull();
	}

	/**
	 * Called to transform the vertices.
	 *
	 * @return If the transformer should pipe the quad.
	 */
	public abstract boolean transform();

	public void onFull()
	{
		if( transform() )
		{
			quad.pipe( consumer );
		}
	}

	//Should be small enough.
	private final static double EPSILON = 0.00001;

	public static boolean epsComp( float a, float b )
	{
		if( a == b )
		{
			return true;
		}
		else
		{
			return Math.abs( a - b ) < EPSILON;
		}
	}
}
