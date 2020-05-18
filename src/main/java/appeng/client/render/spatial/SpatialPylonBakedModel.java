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

package appeng.client.render.spatial;


import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraftforge.common.property.IExtendedBlockState;

import appeng.block.spatial.BlockSpatialPylon;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.tile.spatial.TileSpatialPylon;


/**
 * The baked model that will be used for rendering the spatial pylon.
 */
class SpatialPylonBakedModel implements IBakedModel
{

	private final Map<SpatialPylonTextureType, TextureAtlasSprite> textures;

	private final VertexFormat format;

	SpatialPylonBakedModel( VertexFormat format, Map<SpatialPylonTextureType, TextureAtlasSprite> textures )
	{
		this.textures = ImmutableMap.copyOf( textures );
		this.format = format;
	}

	@Override
	public List<BakedQuad> getQuads( @Nullable BlockState state, @Nullable Direction side, long rand )
	{
		int flags = this.getFlags( state );

		CubeBuilder builder = new CubeBuilder( this.format );

		if( flags != 0 )
		{
			Direction ori = null;
			int displayAxis = flags & TileSpatialPylon.DISPLAY_Z;
			if( displayAxis == TileSpatialPylon.DISPLAY_X )
			{
				ori = Direction.EAST;

				if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MAX )
				{
					builder.setUvRotation( Direction.SOUTH, 1 );
					builder.setUvRotation( Direction.NORTH, 1 );
					builder.setUvRotation( Direction.UP, 2 );
					builder.setUvRotation( Direction.DOWN, 2 );
				}
				else if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MIN )
				{
					builder.setUvRotation( Direction.SOUTH, 2 );
					builder.setUvRotation( Direction.NORTH, 2 );
					builder.setUvRotation( Direction.UP, 1 );
					builder.setUvRotation( Direction.DOWN, 1 );
				}
				else
				{
					builder.setUvRotation( Direction.SOUTH, 1 );
					builder.setUvRotation( Direction.NORTH, 1 );
					builder.setUvRotation( Direction.UP, 1 );
					builder.setUvRotation( Direction.DOWN, 1 );
				}
			}

			else if( displayAxis == TileSpatialPylon.DISPLAY_Y )
			{
				ori = Direction.UP;
				if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MAX )
				{
					builder.setUvRotation( Direction.NORTH, 3 );
					builder.setUvRotation( Direction.SOUTH, 3 );
					builder.setUvRotation( Direction.EAST, 3 );
					builder.setUvRotation( Direction.WEST, 3 );
				}
			}

			else if( displayAxis == TileSpatialPylon.DISPLAY_Z )
			{
				ori = Direction.NORTH;
				if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MAX )
				{
					builder.setUvRotation( Direction.EAST, 2 );
					builder.setUvRotation( Direction.WEST, 1 );
				}
				else if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MIN )
				{
					builder.setUvRotation( Direction.EAST, 1 );
					builder.setUvRotation( Direction.WEST, 2 );
					builder.setUvRotation( Direction.UP, 3 );
					builder.setUvRotation( Direction.DOWN, 3 );
				}
				else
				{
					builder.setUvRotation( Direction.EAST, 1 );
					builder.setUvRotation( Direction.WEST, 2 );
				}
			}

			builder.setTextures( this.textures.get( getTextureTypeFromSideOutside( flags, ori, Direction.UP ) ),
					this.textures.get( getTextureTypeFromSideOutside( flags, ori, Direction.DOWN ) ),
					this.textures.get( getTextureTypeFromSideOutside( flags, ori, Direction.NORTH ) ),
					this.textures.get( getTextureTypeFromSideOutside( flags, ori, Direction.SOUTH ) ),
					this.textures.get( getTextureTypeFromSideOutside( flags, ori, Direction.EAST ) ),
					this.textures.get( getTextureTypeFromSideOutside( flags, ori, Direction.WEST ) ) );
			builder.addCube( 0, 0, 0, 16, 16, 16 );

			if( ( flags & TileSpatialPylon.DISPLAY_POWERED_ENABLED ) == TileSpatialPylon.DISPLAY_POWERED_ENABLED )
			{
				builder.setRenderFullBright( true );
			}

			builder.setTextures( this.textures.get( getTextureTypeFromSideInside( flags, ori, Direction.UP ) ),
					this.textures.get( getTextureTypeFromSideInside( flags, ori, Direction.DOWN ) ),
					this.textures.get( getTextureTypeFromSideInside( flags, ori, Direction.NORTH ) ),
					this.textures.get( getTextureTypeFromSideInside( flags, ori, Direction.SOUTH ) ),
					this.textures.get( getTextureTypeFromSideInside( flags, ori, Direction.EAST ) ),
					this.textures.get( getTextureTypeFromSideInside( flags, ori, Direction.WEST ) ) );
			builder.addCube( 0, 0, 0, 16, 16, 16 );
		}
		else
		{
			builder.setTexture( this.textures.get( SpatialPylonTextureType.BASE ) );
			builder.addCube( 0, 0, 0, 16, 16, 16 );

			builder.setTexture( this.textures.get( SpatialPylonTextureType.DIM ) );
			builder.addCube( 0, 0, 0, 16, 16, 16 );
		}

		return builder.getOutput();
	}

	private int getFlags( BlockState state )
	{
		if( !( state instanceof IExtendedBlockState ) )
		{
			return 0;
		}

		IExtendedBlockState extState = (IExtendedBlockState) state;

		return extState.getValue( BlockSpatialPylon.STATE );
	}

	private static SpatialPylonTextureType getTextureTypeFromSideOutside( int flags, Direction ori, Direction dir )
	{
		if( ori == dir || ori.getOpposite() == dir )
		{
			return SpatialPylonTextureType.BASE;
		}

		if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_MIDDLE )
		{
			return SpatialPylonTextureType.BASE_SPANNED;
		}
		else if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MIN )
		{
			return SpatialPylonTextureType.BASE_END;
		}
		else if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MAX )
		{
			return SpatialPylonTextureType.BASE_END;
		}

		return SpatialPylonTextureType.BASE;
	}

	private static SpatialPylonTextureType getTextureTypeFromSideInside( int flags, Direction ori, Direction dir )
	{
		final boolean good = ( flags & TileSpatialPylon.DISPLAY_ENABLED ) == TileSpatialPylon.DISPLAY_ENABLED;

		if( ori == dir || ori.getOpposite() == dir )
		{
			return good ? SpatialPylonTextureType.DIM : SpatialPylonTextureType.RED;
		}

		if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_MIDDLE )
		{
			return good ? SpatialPylonTextureType.DIM_SPANNED : SpatialPylonTextureType.RED_SPANNED;
		}
		else if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MIN )
		{
			return good ? SpatialPylonTextureType.DIM_END : SpatialPylonTextureType.RED_END;
		}
		else if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MAX )
		{
			return good ? SpatialPylonTextureType.DIM_END : SpatialPylonTextureType.RED_END;
		}

		return SpatialPylonTextureType.BASE;
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return true;
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
		return this.textures.get( SpatialPylonTextureType.DIM );
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.NONE;
	}
}
