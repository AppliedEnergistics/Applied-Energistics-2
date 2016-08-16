/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.render.model;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Strings;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.property.IExtendedBlockState;

import appeng.decorative.solid.GlassState;
import appeng.decorative.solid.QuartzGlassBlock;


public class GlassBakedModel implements IBakedModel
{

	private static final byte[][][] OFFSETS = generateOffsets();

	// Alternating textures based on position
	static final ResourceLocation TEXTURE_A = new ResourceLocation( "appliedenergistics2:blocks/glass/BlockQuartzGlassA" );
	static final ResourceLocation TEXTURE_B = new ResourceLocation( "appliedenergistics2:blocks/glass/BlockQuartzGlassB" );
	static final ResourceLocation TEXTURE_C = new ResourceLocation( "appliedenergistics2:blocks/glass/BlockQuartzGlassC" );
	static final ResourceLocation TEXTURE_D = new ResourceLocation( "appliedenergistics2:blocks/glass/BlockQuartzGlassD" );

	// Frame texture
	static final ResourceLocation[] TEXTURES_FRAME = generateTexturesFrame();

	// Generates the required textures for the frame
	private static ResourceLocation[] generateTexturesFrame()
	{
		return IntStream.range( 1, 16 )
				.mapToObj( Integer::toBinaryString )
				.map( s -> Strings.padStart( s, 4, '0' ) )
				.map( s -> new ResourceLocation( "appliedenergistics2:blocks/glass/BlockQuartzGlassFrame" + s ) )
				.toArray( ResourceLocation[]::new );
	}

	private final TextureAtlasSprite[] glassTextures;

	private final TextureAtlasSprite[] frameTextures;

	private final VertexFormat vertexFormat;

	public GlassBakedModel( VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter )
	{
		this.glassTextures = new TextureAtlasSprite[] {
				bakedTextureGetter.apply( TEXTURE_A ),
				bakedTextureGetter.apply( TEXTURE_B ),
				bakedTextureGetter.apply( TEXTURE_C ),
				bakedTextureGetter.apply( TEXTURE_D )
		};

		this.vertexFormat = format;

		// The first frame texture would be empty, so we simply leave it set to null here
		this.frameTextures = new TextureAtlasSprite[16];
		for( int i = 0; i < TEXTURES_FRAME.length; i++ )
		{
			this.frameTextures[1 + i] = bakedTextureGetter.apply( TEXTURES_FRAME[i] );
		}
	}

	@Override
	public List<BakedQuad> getQuads( @Nullable IBlockState state, @Nullable EnumFacing side, long rand )
	{

		if( !( state instanceof IExtendedBlockState ) || side == null )
		{
			return Collections.emptyList();
		}

		IExtendedBlockState extState = (IExtendedBlockState) state;

		GlassState glassState = extState.getValue( QuartzGlassBlock.GLASS_STATE );

		final int cx = Math.abs( glassState.getX() % 10 );
		final int cy = Math.abs( glassState.getY() % 10 );
		final int cz = Math.abs( glassState.getZ() % 10 );

		int u = OFFSETS[cx][cy][cz] % 4;
		int v = OFFSETS[9 - cx][9 - cy][9 - cz] % 4;

		int texIdx = Math.abs( ( OFFSETS[cx][cy][cz] + ( glassState.getX() + glassState.getY() + glassState.getZ() ) ) % 4 );

		if( texIdx < 2 )
		{
			u /= 2;
			v /= 2;
		}

		TextureAtlasSprite glassTexture = glassTextures[texIdx];

		// Render the glass side
		List<BakedQuad> quads = new ArrayList<>( 5 ); // At most 5

		List<Vec3d> corners = RenderHelper.getFaceCorners( side );
		quads.add( createQuad( side, corners, glassTexture, u, v ) );

		/*
			This needs some explanation:
			The bit-field contains 4-bits, one for each direction that a frame may be drawn.
			Converted to a number, the bit-field is then used as an index into the list of
			frame textures, which have been created in such a way that their filenames
			indicate, in which directions they contain borders.
			i.e. bitmask = 0101 means a border should be drawn up and down (in terms of u,v space).
			Converted to a number, this bitmask is 5. So the texture at index 5 is used.
			That texture had "0101" in its filename to indicate this.
		 */
		int edgeBitmask = makeBitmask( glassState, side );
		TextureAtlasSprite sideSprite = frameTextures[edgeBitmask];
		if( sideSprite != null )
		{
			quads.add( createQuad( side, corners, sideSprite, 0, 0 ) );
		}

		return quads;
	}

	/**
	 * Creates the bitmask that indicates, in which directions (in terms of u,v space) a border should be drawn.
	 */
	private static int makeBitmask( GlassState state, EnumFacing side )
	{
		switch( side )
		{
			case DOWN:
				return makeBitmask( state, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.WEST );
			case UP:
				return makeBitmask( state, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.EAST );
			case NORTH:
				return makeBitmask( state, EnumFacing.UP, EnumFacing.WEST, EnumFacing.DOWN, EnumFacing.EAST );
			case SOUTH:
				return makeBitmask( state, EnumFacing.UP, EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.WEST );
			case WEST:
				return makeBitmask( state, EnumFacing.UP, EnumFacing.SOUTH, EnumFacing.DOWN, EnumFacing.NORTH );
			case EAST:
				return makeBitmask( state, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.DOWN, EnumFacing.SOUTH );
			default:
				throw new IllegalArgumentException( "Unsupported side!" );
		}
	}

	private static int makeBitmask( GlassState state, EnumFacing up, EnumFacing right, EnumFacing down, EnumFacing left )
	{

		int bitmask = 0;

		if( !state.isFlushWith( up ) )
		{
			bitmask |= 1;
		}
		if( !state.isFlushWith( right ) )
		{
			bitmask |= 2;
		}
		if( !state.isFlushWith( down ) )
		{
			bitmask |= 4;
		}
		if( !state.isFlushWith( left ) )
		{
			bitmask |= 8;
		}
		return bitmask;
	}

	private BakedQuad createQuad( EnumFacing side, List<Vec3d> corners, TextureAtlasSprite sprite, float uOffset, float vOffset )
	{
		return createQuad( side, corners.get( 0 ), corners.get( 1 ), corners.get( 2 ), corners.get( 3 ), sprite, uOffset, vOffset );
	}

	private BakedQuad createQuad( EnumFacing side, Vec3d c1, Vec3d c2, Vec3d c3, Vec3d c4, TextureAtlasSprite sprite, float uOffset, float vOffset )
	{
		Vec3d normal = new Vec3d( side.getDirectionVec() );

		// Apply the u,v shift.
		// This mirrors the logic from OffsetIcon from 1.7
		float u1 = MathHelper.clamp_float( 0 - uOffset, 0, 16 );
		float u2 = MathHelper.clamp_float( 16 - uOffset, 0, 16 );
		float v1 = MathHelper.clamp_float( 0 - vOffset, 0, 16 );
		float v2 = MathHelper.clamp_float( 16 - vOffset, 0, 16 );

		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder( vertexFormat );
		builder.setTexture( sprite );
		putVertex( builder, normal, c1.xCoord, c1.yCoord, c1.zCoord, sprite, u1, v1 );
		putVertex( builder, normal, c2.xCoord, c2.yCoord, c2.zCoord, sprite, u1, v2 );
		putVertex( builder, normal, c3.xCoord, c3.yCoord, c3.zCoord, sprite, u2, v2 );
		putVertex( builder, normal, c4.xCoord, c4.yCoord, c4.zCoord, sprite, u2, v1 );
		return builder.build();
	}

	/*
	This method is as complicated as it is, because the order in which we push data into the vertexbuffer actually has to be precisely the order
	in which the vertex elements had been declared in the vertex format.
	 */
	private void putVertex( UnpackedBakedQuad.Builder builder, Vec3d normal, double x, double y, double z, TextureAtlasSprite sprite, float u, float v )
	{
		for( int e = 0; e < vertexFormat.getElementCount(); e++ )
		{
			switch( vertexFormat.getElement( e ).getUsage() )
			{
				case POSITION:
					builder.put( e, (float) x, (float) y, (float) z, 1.0f );
					break;
				case COLOR:
					builder.put( e, 1.0f, 1.0f, 1.0f, 1.0f );
					break;
				case UV:
					if( vertexFormat.getElement( e ).getIndex() == 0 )
					{
						u = sprite.getInterpolatedU( u );
						v = sprite.getInterpolatedV( v );
						builder.put( e, u, v, 0f, 1f );
						break;
					}
				case NORMAL:
					builder.put( e, (float) normal.xCoord, (float) normal.yCoord, (float) normal.zCoord, 0f );
					break;
				default:
					builder.put( e );
					break;
			}
		}
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.NONE;
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return false;
	}

	@Override
	public boolean isGui3d()
	{
		return false;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return frameTextures[frameTextures.length - 1];
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}

	private static byte[][][] generateOffsets()
	{
		final Random r = new Random( 924 );
		final byte[][][] offset = new byte[10][10][10];

		for( int x = 0; x < 10; x++ )
		{
			for( int y = 0; y < 10; y++ )
			{
				r.nextBytes( offset[x][y] );
			}
		}

		return offset;
	}
}
