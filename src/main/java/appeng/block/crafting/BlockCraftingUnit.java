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

package appeng.block.crafting;


import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockStateContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.BooleanProperty;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.IProperty;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.client.UnlistedProperty;
import appeng.client.render.crafting.CraftingCubeState;
import appeng.core.sync.GuiBridge;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.Platform;


public class BlockCraftingUnit extends AEBaseTileBlock
{
	public static final BooleanProperty FORMED = BooleanProperty.create( "formed" );
	public static final BooleanProperty POWERED = BooleanProperty.create( "powered" );
	public static final UnlistedProperty<CraftingCubeState> STATE = new UnlistedProperty<>( "state", CraftingCubeState.class );

	public final CraftingUnitType type;

	public BlockCraftingUnit( final CraftingUnitType type )
	{
		super( Material.IRON );

		this.type = type;
	}

	@Override
	protected IProperty[] getAEStates()
	{
		return new IProperty[] { POWERED, FORMED };
	}

	@Override
	public IExtendedBlockState getExtendedState( BlockState state, IBlockReader world, BlockPos pos )
	{

		EnumSet<Direction> connections = EnumSet.noneOf( Direction.class );

		for( Direction facing : Direction.values() )
		{
			if( this.isConnected( world, pos, facing ) )
			{
				connections.add( facing );
			}
		}

		IExtendedBlockState extState = (IExtendedBlockState) state;

		return extState.with( STATE, new CraftingCubeState( connections ) );
	}

	private boolean isConnected( IBlockReader world, BlockPos pos, Direction side )
	{
		BlockPos adjacentPos = pos.offset( side );
		return world.getBlockState( adjacentPos ).getBlock() instanceof BlockCraftingUnit;
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new ExtendedBlockState( this, this.getAEStates(), new IUnlistedProperty[] { STATE } );
	}

	@Override
	public BlockState getStateFromMeta( final int meta )
	{
		return this.getDefaultState().with( POWERED, ( meta & 1 ) == 1 ).with( FORMED, ( meta & 2 ) == 2 );
	}

	@Override
	public int getMetaFromState( final BlockState state )
	{
		boolean p = state.getValue( POWERED );
		boolean f = state.getValue( FORMED );
		return ( p ? 1 : 0 ) | ( f ? 2 : 0 );
	}

	@Override
	public void neighborChanged( final BlockState state, final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos, boolean isMoving )
	{
		final TileCraftingTile cp = this.getTileEntity( worldIn, pos );
		if( cp != null )
		{
			cp.updateMultiBlock();
		}
	}

	@Override
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public void onReplaced(BlockState state, World w, BlockPos pos, BlockState newState, boolean isMoving)
	{
		final TileCraftingTile cp = this.getTileEntity( w, pos );
		if( cp != null )
		{
			cp.breakCluster();
		}

		super.onReplaced( state, w, pos, newState, isMoving );
	}

	@Override
	public boolean onBlockActivated( final World w, final BlockPos pos, final BlockState state, final PlayerEntity p, final Hand hand, final Direction side, final float hitX, final float hitY, final float hitZ )
	{
		final TileCraftingTile tg = this.getTileEntity( w, pos );

		if( tg != null && !p.isCrouching() && tg.isFormed() && tg.isActive() )
		{
			if( Platform.isClient() )
			{
				return true;
			}

			Platform.openGUI( p, tg, AEPartLocation.fromFacing( side ), GuiBridge.GUI_CRAFTING_CPU );
			return true;
		}

		return super.onBlockActivated( w, pos, state, p, hand, side, hitX, hitY, hitZ );
	}

	public enum CraftingUnitType
	{
		UNIT, ACCELERATOR, STORAGE_1K, STORAGE_4K, STORAGE_16K, STORAGE_64K, MONITOR
	}
}
