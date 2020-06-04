/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.block.qnb;


import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.state.BooleanProperty;
import net.minecraft.block.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import appeng.block.AEBaseTileBlock;
import appeng.helpers.ICustomCollision;
import appeng.tile.qnb.TileQuantumBridge;


public abstract class BlockQuantumBase extends AEBaseTileBlock implements ICustomCollision
{

	public static final BooleanProperty FORMED = BooleanProperty.create( "formed" );

	public static final QnbFormedStateProperty FORMED_STATE = new QnbFormedStateProperty();

	public BlockQuantumBase( final Material mat )
	{
		super( mat );
		final float shave = 2.0f / 16.0f;
		this.boundingBox = new AxisAlignedBB( shave, shave, shave, 1.0f - shave, 1.0f - shave, 1.0f - shave );
		this.setLightOpacity( 0 );
		this.setFullSize( this.setOpaque( false ) );
		this.setDefaultState( this.getDefaultState().with( FORMED, false ) );
	}

	@Override
	protected IProperty[] getAEStates()
	{
		return new IProperty[] { FORMED };
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new ExtendedBlockState( this, this.getAEStates(), new IUnlistedProperty[] { FORMED_STATE } );
	}

	@Override
	public BlockState getExtendedState( BlockState state, IBlockReader world, BlockPos pos )
	{
		IExtendedBlockState extState = (IExtendedBlockState) state;

		TileQuantumBridge bridge = this.getTileEntity( world, pos );
		if( bridge != null )
		{
			QnbFormedState formedState = new QnbFormedState( bridge.getAdjacentQuantumBridges(), bridge.isCorner(), bridge.isPowered() );
			extState = extState.with( FORMED_STATE, formedState );
		}

		return extState;
	}

	@Override
	public BlockState getActualState( BlockState state, IBlockReader worldIn, BlockPos pos )
	{
		TileQuantumBridge bridge = this.getTileEntity( worldIn, pos );
		if( bridge != null )
		{
			state = state.with( FORMED, bridge.isFormed() );
		}
		return state;
	}

	@Override
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public void neighborChanged( BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving )
	{
		final TileQuantumBridge bridge = this.getTileEntity( world, pos );
		if( bridge != null )
		{
			bridge.neighborUpdate();
		}
	}

	@Override
	public void onReplaced(BlockState state, World w, BlockPos pos, BlockState newState, boolean isMoving) {
		final TileQuantumBridge bridge = this.getTileEntity( w, pos );
		if( bridge != null )
		{
			bridge.breakCluster();
		}

		super.onReplaced( state, w, pos, newState, isMoving );
	}

	@Override
	public boolean isFullCube( BlockState state )
	{
		return false;
	}
}
