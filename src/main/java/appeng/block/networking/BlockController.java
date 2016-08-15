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

package appeng.block.networking;


import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.core.features.AEFeature;
import appeng.tile.networking.TileController;


public class BlockController extends AEBaseTileBlock
{

	public enum ControllerBlockState implements IStringSerializable
	{
		offline, online, conflicted;

		@Override
		public String getName()
		{
			return this.name();
		}

	}

    /**
     * Controls the rendering of the controller block (connected texture style).
     * inside_a and inside_b are alternating patterns for a controller that is enclosed by other controllers,
     * and since they are always offline, they do not have the usual sub-states.
     */
	public enum ControllerRenderType implements IStringSerializable
	{
		block, column, inside_a, inside_b;

		@Override
		public String getName()
		{
			return this.name();
		}

	}

	public static final PropertyEnum<ControllerBlockState> CONTROLLER_STATE = PropertyEnum.create( "state", ControllerBlockState.class );

	public static final PropertyEnum<ControllerRenderType> CONTROLLER_TYPE = PropertyEnum.create( "type", ControllerRenderType.class );

	@Override
	protected IProperty[] getAEStates()
	{
		return new IProperty[] { AE_BLOCK_FORWARD, AE_BLOCK_UP, CONTROLLER_STATE, CONTROLLER_TYPE };
	}

	/**
	 * This will compute the AE_BLOCK_FORWARD, AE_BLOCK_UP and CONTROLLER_TYPE block states based on adjacent
	 * controllers and the network state of this controller (offline, online, conflicted). This is used to
	 * get a rudimentary connected texture feel for the controller based on how it is placed.
	 */
	@Override
	public IBlockState getActualState( IBlockState state, IBlockAccess world, BlockPos pos )
	{

		// Only used for columns, really
		EnumFacing up = EnumFacing.UP;
		EnumFacing forward = EnumFacing.NORTH;
		ControllerRenderType type = ControllerRenderType.block;

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		// Detect whether controllers are on both sides of the x, y, and z axes
		final boolean xx = this.getTileEntity( world, x - 1, y, z ) instanceof TileController && this.getTileEntity( world, x + 1, y, z ) instanceof TileController;
		final boolean yy = this.getTileEntity( world, x, y - 1, z ) instanceof TileController && this.getTileEntity( world, x, y + 1, z ) instanceof TileController;
		final boolean zz = this.getTileEntity( world, x, y, z - 1 ) instanceof TileController && this.getTileEntity( world, x, y, z + 1 ) instanceof TileController;

		if( xx && !yy && !zz )
		{
			up = EnumFacing.EAST;
			forward = EnumFacing.UP;
			type = ControllerRenderType.column;
		}
		else if( !xx && yy && !zz )
		{
			up = EnumFacing.UP;
			forward = EnumFacing.NORTH;
			type = ControllerRenderType.column;
		}
		else if( !xx && !yy && zz )
		{
			up = EnumFacing.NORTH;
			forward = EnumFacing.UP;
			type = ControllerRenderType.column;
		}
		else if( ( xx ? 1 : 0 ) + ( yy ? 1 : 0 ) + ( zz ? 1 : 0 ) >= 2 )
		{
			final int v = ( Math.abs( x ) + Math.abs( y ) + Math.abs( z ) ) % 2;

			// While i'd like this to be based on the blockstate randomization feature, this generates
			// an alternating pattern based on world position, so this is not 100% doable with blockstates.
			if( v == 0 )
			{
				type = ControllerRenderType.inside_a;
			}
			else
			{
				type = ControllerRenderType.inside_b;
			}
		}

		return state.withProperty( AE_BLOCK_FORWARD, forward ).withProperty( AE_BLOCK_UP, up ).withProperty( CONTROLLER_TYPE, type );
	}

	@Override
	public int getMetaFromState( final IBlockState state )
	{
		return state.getValue( CONTROLLER_STATE ).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta( final int meta )
	{
		ControllerBlockState state = ControllerBlockState.values()[meta];
		return this.getDefaultState().withProperty( CONTROLLER_STATE, state );
	}

	@Override
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	public BlockController()
	{
		super( Material.IRON );
		this.setTileEntity( TileController.class );
		this.setHardness( 6 );
		this.setFeature( EnumSet.of( AEFeature.Channels ) );
		this.setDefaultState( getDefaultState().withProperty( CONTROLLER_STATE, ControllerBlockState.offline ).withProperty( CONTROLLER_TYPE, ControllerRenderType.block ) );
	}

	@Override
	public void neighborChanged( final IBlockState state, final World w, final BlockPos pos, final Block neighborBlock )
	{
		final TileController tc = this.getTileEntity( w, pos );
		if( tc != null )
		{
			tc.onNeighborChange( false );
		}
	}
}
