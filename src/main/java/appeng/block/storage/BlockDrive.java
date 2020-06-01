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

package appeng.block.storage;


import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.client.UnlistedProperty;
import appeng.core.sync.GuiBridge;
import appeng.tile.storage.TileDrive;
import appeng.util.Platform;


public class BlockDrive extends AEBaseTileBlock
{

	public static final UnlistedProperty<DriveSlotsState> SLOTS_STATE = new UnlistedProperty<>( "drive_slots_state", DriveSlotsState.class );

	public BlockDrive()
	{
		super( Material.IRON );
	}

	@Override
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new ExtendedBlockState( this, this.getAEStates(), new IUnlistedProperty[] {
				SLOTS_STATE,
				FORWARD,
				UP
		} );
	}

	@Override
	public BlockState getExtendedState( BlockState state, IBlockReader world, BlockPos pos )
	{
		TileDrive te = this.getTileEntity( world, pos );
		IExtendedBlockState extState = (IExtendedBlockState) super.getExtendedState( state, world, pos );
		return extState.with( SLOTS_STATE, te == null ? DriveSlotsState.createEmpty( 10 ) : DriveSlotsState.fromChestOrDrive( te ) );
	}

	@Override
	public ActionResultType onActivated(final World w, final BlockPos pos, final PlayerEntity p, final Hand hand, final @Nullable ItemStack heldItem, final BlockRayTraceResult hit)
	{
		if( p.isShiftKeyDown() )
		{
			return false;
		}

		final TileDrive tg = this.getTileEntity( w, pos );
		if( tg != null )
		{
			if( Platform.isServer() )
			{
				Platform.openGUI( p, tg, AEPartLocation.fromFacing(hit), GuiBridge.GUI_DRIVE );
			}
			return true;
		}
		return false;
	}
}
