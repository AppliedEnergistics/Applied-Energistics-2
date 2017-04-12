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

package appeng.tile.grindstone;


import appeng.api.AEApi;
import appeng.api.features.IGrinderEntry;
import appeng.api.implementations.tiles.ICrankable;
import appeng.api.util.WorldCoord;
import appeng.tile.AEBaseInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.WrapperInventoryRange;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;


public class TileGrinder extends AEBaseInvTile implements ICrankable
{

	private final int[] inputs = { 0, 1, 2 };
	private final int[] sides = { 0, 1, 2, 3, 4, 5 };
	private final AppEngInternalInventory inv = new AppEngInternalInventory( this, 7 );
	private int points;

	@Override
	public void setOrientation( final ForgeDirection inForward, final ForgeDirection inUp )
	{
		super.setOrientation( inForward, inUp );
		this.getBlockType().onNeighborBlockChange( this.worldObj, this.xCoord, this.yCoord, this.zCoord, Platform.AIR_BLOCK );
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.inv;
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{

	}

	@Override
	public boolean canInsertItem( final int slotIndex, final ItemStack insertingItem, final int side )
	{
		if( AEApi.instance().registries().grinder().getRecipeForInput( insertingItem ) == null )
		{
			return false;
		}

		return slotIndex >= 0 && slotIndex <= 2;
	}

	@Override
	public boolean canExtractItem( final int slotIndex, final ItemStack extractedItem, final int side )
	{
		return slotIndex >= 3 && slotIndex <= 5;
	}

	@Override
	public int[] getAccessibleSlotsBySide( final ForgeDirection side )
	{
		return this.sides;
	}

	@Override
	public boolean canTurn()
	{
		if( Platform.isClient() )
		{
			return false;
		}

		if( null == this.getStackInSlot( 6 ) ) // Add if there isn't one...
		{
			final IInventory src = new WrapperInventoryRange( this, this.inputs, true );
			for( int x = 0; x < src.getSizeInventory(); x++ )
			{
				ItemStack item = src.getStackInSlot( x );
				if( item == null )
				{
					continue;
				}

				final IGrinderEntry r = AEApi.instance().registries().grinder().getRecipeForInput( item );
				if( r != null )
				{
					if( item.stackSize >= r.getInput().stackSize )
					{
						item.stackSize -= r.getInput().stackSize;
						final ItemStack ais = item.copy();
						ais.stackSize = r.getInput().stackSize;

						if( item.stackSize <= 0 )
						{
							item = null;
						}

						src.setInventorySlotContents( x, item );
						this.setInventorySlotContents( 6, ais );
						return true;
					}
				}
			}
			return false;
		}
		return true;
	}

	@Override
	public void applyTurn()
	{
		if( Platform.isClient() )
		{
			return;
		}

		this.points++;

		final ItemStack processing = this.getStackInSlot( 6 );
		final IGrinderEntry r = AEApi.instance().registries().grinder().getRecipeForInput( processing );
		if( r != null )
		{
			if( r.getEnergyCost() > this.points )
			{
				return;
			}

			this.points = 0;
			final InventoryAdaptor sia = InventoryAdaptor.getAdaptor( new WrapperInventoryRange( this, 3, 3, true ), ForgeDirection.EAST );

			this.addItem( sia, r.getOutput() );

			float chance = ( Platform.getRandomInt() % 2000 ) / 2000.0f;
			if( chance <= r.getOptionalChance() )
			{
				this.addItem( sia, r.getOptionalOutput() );
			}

			chance = ( Platform.getRandomInt() % 2000 ) / 2000.0f;
			if( chance <= r.getSecondOptionalChance() )
			{
				this.addItem( sia, r.getSecondOptionalOutput() );
			}

			this.setInventorySlotContents( 6, null );
		}
	}

	private void addItem( final InventoryAdaptor sia, final ItemStack output )
	{
		if( output == null )
		{
			return;
		}

		final ItemStack notAdded = sia.addItems( output );
		if( notAdded != null )
		{
			final WorldCoord wc = new WorldCoord( this.xCoord, this.yCoord, this.zCoord );

			wc.add( this.getForward(), 1 );

			final List<ItemStack> out = new ArrayList<ItemStack>();
			out.add( notAdded );

			Platform.spawnDrops( this.worldObj, wc.x, wc.y, wc.z, out );
		}
	}

	@Override
	public boolean canCrankAttach( final ForgeDirection directionToCrank )
	{
		return this.getUp() == directionToCrank;
	}
}
