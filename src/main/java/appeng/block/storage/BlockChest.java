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


import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.ContainerChest;
import appeng.container.implementations.ContainerQNB;
import appeng.core.localization.PlayerMessages;
import appeng.tile.storage.TileChest;
import appeng.util.Platform;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;


public class BlockChest extends AEBaseTileBlock<TileChest>
{

	private final static EnumProperty<DriveSlotState> SLOT_STATE = EnumProperty.create( "slot_state", DriveSlotState.class );

	public BlockChest()
	{
		super( Properties.create(Material.IRON) );
		this.setDefaultState( this.getDefaultState().with( SLOT_STATE, DriveSlotState.EMPTY ) );
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder builder) {
		super.fillStateContainer(builder);
		builder.add(SLOT_STATE);
	}

	@Override
	protected BlockState updateBlockStateFromTileEntity(BlockState currentState, TileChest te) {
		DriveSlotState slotState = DriveSlotState.EMPTY;

		if( te.getCellCount() >= 1 )
		{
			slotState = DriveSlotState.fromCellStatus( te.getCellStatus( 0 ) );
		}
		// Power-state has to be checked separately
		if( !te.isPowered() && slotState != DriveSlotState.EMPTY )
		{
			slotState = DriveSlotState.OFFLINE;
		}

		return currentState.with( SLOT_STATE, slotState );
	}

	@Override
	public ActionResultType onActivated(final World w, final BlockPos pos, final PlayerEntity p, final Hand hand, final @Nullable ItemStack heldItem, final BlockRayTraceResult hit)
	{
		final TileChest tg = this.getTileEntity( w, pos );
		if( tg != null && !p.isCrouching() )
		{
			if( w.isRemote() )
			{
				return ActionResultType.SUCCESS;
			}

			if( hit.getFace() != tg.getUp() )
			{
				ContainerOpener.openContainer(ContainerChest.TYPE, p, ContainerLocator.forTileEntitySide(tg, hit.getFace()));
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
