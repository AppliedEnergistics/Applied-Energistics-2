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
