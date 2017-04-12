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

package appeng.tile;


import appeng.block.AEBaseBlock;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nullable;


public abstract class AEBaseInvTile extends AEBaseTile implements ISidedInventory, IAEAppEngInventory
{

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_AEBaseInvTile( final net.minecraft.nbt.NBTTagCompound data )
	{
		final IInventory inv = this.getInternalInventory();
		final NBTTagCompound opt = data.getCompoundTag( "inv" );
		for( int x = 0; x < inv.getSizeInventory(); x++ )
		{
			final NBTTagCompound item = opt.getCompoundTag( "item" + x );
			inv.setInventorySlotContents( x, ItemStack.loadItemStackFromNBT( item ) );
		}
	}

	public abstract IInventory getInternalInventory();

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_AEBaseInvTile( final net.minecraft.nbt.NBTTagCompound data )
	{
		final IInventory inv = this.getInternalInventory();
		final NBTTagCompound opt = new NBTTagCompound();
		for( int x = 0; x < inv.getSizeInventory(); x++ )
		{
			final NBTTagCompound item = new NBTTagCompound();
			final ItemStack is = this.getStackInSlot( x );
			if( is != null )
			{
				is.writeToNBT( item );
			}
			opt.setTag( "item" + x, item );
		}
		data.setTag( "inv", opt );
	}

	@Override
	public int getSizeInventory()
	{
		return this.getInternalInventory().getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot( final int i )
	{
		return this.getInternalInventory().getStackInSlot( i );
	}

	@Override
	public ItemStack decrStackSize( final int i, final int j )
	{
		return this.getInternalInventory().decrStackSize( i, j );
	}

	@Override
	public ItemStack getStackInSlotOnClosing( final int i )
	{
		return null;
	}

	@Override
	public void setInventorySlotContents( final int i, @Nullable final ItemStack itemstack )
	{
		this.getInternalInventory().setInventorySlotContents( i, itemstack );
	}

	/**
	 * Returns the name of the inventory
	 */
	@Override
	public String getInventoryName()
	{
		return this.getCustomName();
	}

	/**
	 * Returns if the inventory is named
	 */
	@Override
	public boolean hasCustomInventoryName()
	{
		return this.hasCustomName();
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer( final EntityPlayer p )
	{
		final double squaredMCReach = 64.0D;

		return this.worldObj.getTileEntity( this.xCoord, this.yCoord, this.zCoord ) == this && p.getDistanceSq( this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D ) <= squaredMCReach;
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{
		return true;
	}

	@Override
	public abstract void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added );

	@Override
	public final int[] getAccessibleSlotsFromSide( final int side )
	{
		final Block blk = this.worldObj.getBlock( this.xCoord, this.yCoord, this.zCoord );
		if( blk instanceof AEBaseBlock )
		{
			final ForgeDirection mySide = ForgeDirection.getOrientation( side );
			return this.getAccessibleSlotsBySide( ( (AEBaseBlock) blk ).mapRotation( this, mySide ) );
		}
		return this.getAccessibleSlotsBySide( ForgeDirection.getOrientation( side ) );
	}

	@Override
	public boolean canInsertItem( final int slotIndex, final ItemStack insertingItem, final int side )
	{
		return this.isItemValidForSlot( slotIndex, insertingItem );
	}

	@Override
	public boolean canExtractItem( final int slotIndex, final ItemStack extractedItem, final int side )
	{
		return true;
	}

	public abstract int[] getAccessibleSlotsBySide( ForgeDirection whichSide );
}
