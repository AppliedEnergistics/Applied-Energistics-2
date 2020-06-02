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

package appeng.block.misc;


import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.PropertyFloat;

import appeng.block.AEBaseTileBlock;
import appeng.helpers.ICustomCollision;
import appeng.tile.misc.TileSkyCompass;


public class BlockSkyCompass extends AEBaseTileBlock implements ICustomCollision
{

	// Rotation is expressed as radians
	public static final PropertyFloat ROTATION = new PropertyFloat( "rotation" );

	public BlockSkyCompass()
	{
		super( Material.MISCELLANEOUS );
		this.setLightOpacity( 0 );
		this.setFullSize( false );
		this.setOpaque( false );
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new ExtendedBlockState( this, this.getAEStates(), new IUnlistedProperty[] { FORWARD, UP, ROTATION } );
	}

	@Override
	public boolean isValidOrientation(final IWorld w, final BlockPos pos, final Direction forward, final Direction up )
	{
		final TileSkyCompass sc = this.getTileEntity( w, pos );
		if( sc != null )
		{
			return false;
		}
		return this.canPlaceAt( w, pos, forward.getOpposite() );
	}

	private boolean canPlaceAt( final World w, final BlockPos pos, final Direction dir )
	{
		return w.isSideSolid( pos.offset( dir ), dir.getOpposite(), false );
	}

	@Override
	public void neighborChanged( BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving )
	{
		final TileSkyCompass sc = this.getTileEntity( world, pos );
		final Direction forward = sc.getForward();
		if( !this.canPlaceAt( world, pos, forward.getOpposite() ) )
		{
			this.dropTorch( world, pos );
		}
	}

	private void dropTorch( final World w, final BlockPos pos )
	{
		final BlockState prev = w.getBlockState( pos );
		w.destroyBlock( pos, true );
		w.notifyBlockUpdate( pos, prev, w.getBlockState( pos ), 3 );
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader w, BlockPos pos)
	{
		for( final Direction dir : Direction.values() )
		{
			if( this.canPlaceAt( w, pos, dir ) )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( final World w, final BlockPos pos, final Entity thePlayer, final boolean b )
	{
		final TileSkyCompass tile = this.getTileEntity( w, pos );
		if( tile != null )
		{
			final Direction forward = tile.getForward();

			double minX = 0;
			double minY = 0;
			double minZ = 0;
			double maxX = 1;
			double maxY = 1;
			double maxZ = 1;

			switch( forward )
			{
				case DOWN:
					minZ = minX = 5.0 / 16.0;
					maxZ = maxX = 11.0 / 16.0;
					maxY = 1.0;
					minY = 14.0 / 16.0;
					break;
				case EAST:
					minZ = minY = 5.0 / 16.0;
					maxZ = maxY = 11.0 / 16.0;
					maxX = 2.0 / 16.0;
					minX = 0.0;
					break;
				case NORTH:
					minY = minX = 5.0 / 16.0;
					maxY = maxX = 11.0 / 16.0;
					maxZ = 1.0;
					minZ = 14.0 / 16.0;
					break;
				case SOUTH:
					minY = minX = 5.0 / 16.0;
					maxY = maxX = 11.0 / 16.0;
					maxZ = 2.0 / 16.0;
					minZ = 0.0;
					break;
				case UP:
					minZ = minX = 5.0 / 16.0;
					maxZ = maxX = 11.0 / 16.0;
					maxY = 2.0 / 16.0;
					minY = 0.0;
					break;
				case WEST:
					minZ = minY = 5.0 / 16.0;
					maxZ = maxY = 11.0 / 16.0;
					maxX = 1.0;
					minX = 14.0 / 16.0;
					break;
				default:
					break;
			}

			return Collections.singletonList( new AxisAlignedBB( minX, minY, minZ, maxX, maxY, maxZ ) );
		}
		return Collections.singletonList( new AxisAlignedBB( 0.0, 0, 0.0, 1.0, 1.0, 1.0 ) );
	}

	@Override
	public void addCollidingBlockToList( final World w, final BlockPos pos, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e )
	{

	}

	@Override
	public BlockRenderType getRenderType( BlockState state )
	{
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean isFullBlock( BlockState state )
	{
		return false;
	}

}
