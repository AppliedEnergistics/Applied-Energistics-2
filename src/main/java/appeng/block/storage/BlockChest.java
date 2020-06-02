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
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
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

import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.GuiBridge;
import appeng.tile.storage.TileChest;
import appeng.util.Platform;


public class BlockChest extends AEBaseTileBlock
{

	private final static PropertyEnum<DriveSlotState> SLOT_STATE = PropertyEnum.create( "slot_state", DriveSlotState.class );

	public BlockChest()
	{
		super( Material.IRON );
		this.setDefaultState( this.getDefaultState().with( SLOT_STATE, DriveSlotState.EMPTY ) );
	}

	@Override
	protected IProperty[] getAEStates()
	{
		return new IProperty[] { SLOT_STATE };
	}

	@Override
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public BlockState getActualState( BlockState state, IBlockReader worldIn, BlockPos pos )
	{
		DriveSlotState slotState = DriveSlotState.EMPTY;

		TileChest te = this.getTileEntity( worldIn, pos );

		if( te != null )
		{
			if( te.getCellCount() >= 1 )
			{
				slotState = DriveSlotState.fromCellStatus( te.getCellStatus( 0 ) );
			}
			// Power-state has to be checked separately
			if( !te.isPowered() && slotState != DriveSlotState.EMPTY )
			{
				slotState = DriveSlotState.OFFLINE;
			}
		}

		return super.getActualState( state, worldIn, pos )
				.with( SLOT_STATE, slotState );
	}

	@Override
	public ActionResultType onActivated(final World w, final BlockPos pos, final PlayerEntity p, final Hand hand, final @Nullable ItemStack heldItem, final BlockRayTraceResult hit)
	{
		final TileChest tg = this.getTileEntity( w, pos );
		if( tg != null && !p.isCrouching() )
		{
			if( Platform.isClient() )
			{
				return ActionResultType.SUCCESS;
			}

			if( hit != tg.getUp() )
			{
				Platform.openGUI( p, tg, AEPartLocation.fromFacing(hit), GuiBridge.GUI_CHEST );
			}
			else
			{
				if( !tg.openGui( p ) )
				{
					p.sendMessage( PlayerMessages.ChestCannotReadStorageCell.get() );
				}
			}

			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}
}
