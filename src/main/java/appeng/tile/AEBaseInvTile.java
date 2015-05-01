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


import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.block.AEBaseBlock;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;


public abstract class AEBaseInvTile extends AEBaseTile implements ISidedInventory, IAEAppEngInventory
{

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_AEBaseInvTile( net.minecraft.nbt.NBTTagCompound data )
	{
		IInventory inv = this.getInternalInventory();
		NBTTagCompound opt = data.getCompoundTag( "inv" );
		for( int x = 0; x < inv.getSizeInventory(); x++ )
		{
			NBTTagCompound item = opt.getCompoundTag( "item" + x );
			inv.setInventorySlotContents( x, ItemStack.loadItemStackFromNBT( item ) );
		}
	}

	public abstract IInventory getInternalInventory();

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_AEBaseInvTile( net.minecraft.nbt.NBTTagCompound data )
	{
		IInventory inv = this.getInternalInventory();
		NBTTagCompound opt = new NBTTagCompound();
		for( int x = 0; x < inv.getSizeInventory(); x++ )
		{
			NBTTagCompound item = new NBTTagCompound();
			ItemStack is = this.getStackInSlot( x );
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
	public ItemStack getStackInSlot( int i )
	{
		return this.getInternalInventory().getStackInSlot( i );
	}

	@Override
	public ItemStack decrStackSize( int i, int j )
	{
		return this.getInternalInventory().decrStackSize( i, j );
	}

	@Override
	public ItemStack getStackInSlotOnClosing( int i )
	{
		return null;
	}

	@Override
	public void setInventorySlotContents( int i, @Nullable ItemStack itemstack )
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
	public boolean isUseableByPlayer( EntityPlayer p )
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
	public boolean isItemValidForSlot( int i, ItemStack itemstack )
	{
		return true;
	}

	@Override
	public abstract void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added );

	@Override
	public final int[] getAccessibleSlotsFromSide( int side )
	{
		Block blk = this.worldObj.getBlock( this.xCoord, this.yCoord, this.zCoord );
		if( blk instanceof AEBaseBlock )
		{
			ForgeDirection mySide = ForgeDirection.getOrientation( side );
			return this.getAccessibleSlotsBySide( ( (AEBaseBlock) blk ).mapRotation( this, mySide ) );
		}
		return this.getAccessibleSlotsBySide( ForgeDirection.getOrientation( side ) );
	}

	@Override
	public boolean canInsertItem( int slotIndex, ItemStack insertingItem, int side )
	{
		return this.isItemValidForSlot( slotIndex, insertingItem );
	}

	@Override
	public boolean canExtractItem( int slotIndex, ItemStack extractedItem, int side )
	{
		return true;
	}

	public abstract int[] getAccessibleSlotsBySide( ForgeDirection whichSide );
}
