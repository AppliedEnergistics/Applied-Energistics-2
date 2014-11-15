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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
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

public class TileGrinder extends AEBaseInvTile implements ICrankable
{

	int points;

	final int inputs[] = new int[] { 0, 1, 2 };
	final int sides[] = new int[] { 0, 1, 2, 3, 4, 5 };
	final AppEngInternalInventory inv = new AppEngInternalInventory( this, 7 );

	@Override
	public void setOrientation(ForgeDirection inForward, ForgeDirection inUp)
	{
		super.setOrientation( inForward, inUp );
		getBlockType().onNeighborBlockChange( worldObj, xCoord, yCoord, zCoord, Platform.air );
	}

	private void addItem(InventoryAdaptor sia, ItemStack output)
	{
		if ( output == null )
			return;

		ItemStack notAdded = sia.addItems( output );
		if ( notAdded != null )
		{
			WorldCoord wc = new WorldCoord( xCoord, yCoord, zCoord );

			wc.add( getForward(), 1 );

			List<ItemStack> out = new ArrayList<ItemStack>();
			out.add( notAdded );

			Platform.spawnDrops( worldObj, wc.x, wc.y, wc.z, out );
		}
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		if ( AEApi.instance().registries().grinder().getRecipeForInput( itemstack ) == null )
			return false;

		return i >= 0 && i <= 2;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return i >= 3 && i <= 5;
	}

	@Override
	public IInventory getInternalInventory()
	{
		return inv;
	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection side)
	{
		return sides;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{

	}

	@Override
	public boolean canTurn()
	{
		if ( Platform.isClient() )
			return false;

		if ( null == this.getStackInSlot( 6 ) ) // Add if there isn't one...
		{
			IInventory src = new WrapperInventoryRange( this, inputs, true );
			for (int x = 0; x < src.getSizeInventory(); x++)
			{
				ItemStack item = src.getStackInSlot( x );
				if ( item == null )
					continue;

				IGrinderEntry r = AEApi.instance().registries().grinder().getRecipeForInput( item );
				if ( r != null )
				{
					if ( item.stackSize >= r.getInput().stackSize )
					{
						item.stackSize -= r.getInput().stackSize;
						ItemStack ais = item.copy();
						ais.stackSize = r.getInput().stackSize;

						if ( item.stackSize <= 0 )
							item = null;

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
		if ( Platform.isClient() )
			return;

		points++;

		ItemStack processing = this.getStackInSlot( 6 );
		IGrinderEntry r = AEApi.instance().registries().grinder().getRecipeForInput( processing );
		if ( r != null )
		{
			if ( r.getEnergyCost() > points )
				return;

			points = 0;
			InventoryAdaptor sia = InventoryAdaptor.getAdaptor( new WrapperInventoryRange( this, 3, 3, true ), ForgeDirection.EAST );

			addItem( sia, r.getOutput() );

			float chance = (Platform.getRandomInt() % 2000) / 2000.0f;
			if ( chance <= r.getOptionalChance() )
				addItem( sia, r.getOptionalOutput() );

			chance = (Platform.getRandomInt() % 2000) / 2000.0f;
			if ( chance <= r.getSecondOptionalChance() )
				addItem( sia, r.getSecondOptionalOutput() );

			this.setInventorySlotContents( 6, null );
		}
	}

	@Override
	public boolean canCrankAttach(ForgeDirection directionToCrank)
	{
		return getUp().equals( directionToCrank );
	}

}
