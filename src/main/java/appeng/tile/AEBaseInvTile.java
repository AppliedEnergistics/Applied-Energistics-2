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


import java.util.EnumMap;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import appeng.block.AEBaseBlock;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;


public abstract class AEBaseInvTile extends AEBaseTile implements ISidedInventory, IAEAppEngInventory
{

	private EnumMap<EnumFacing, IItemHandler> sidedItemHandler = new EnumMap<>( EnumFacing.class );

	private IItemHandler itemHandler;

	@Override
	public String getName()
	{
		return this.getCustomName();
	}

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
	public ItemStack removeStackFromSlot( final int i )
	{
		return null;
	}

	@Override
	public void setInventorySlotContents( final int i, @Nullable final ItemStack itemstack )
	{
		this.getInternalInventory().setInventorySlotContents( i, itemstack );
	}

	/**
	 * Returns if the inventory is named
	 */
	@Override
	public boolean hasCustomName()
	{
		return this.getInternalInventory().hasCustomName();
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

		return this.worldObj.getTileEntity( this.pos ) == this && p.getDistanceSq( this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D ) <= squaredMCReach;
	}

	@Override
	public void openInventory( final EntityPlayer player )
	{

	}

	;

	@Override
	public void closeInventory( final EntityPlayer player )
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
	public int[] getSlotsForFace( final EnumFacing side )
	{
		final Block blk = this.worldObj.getBlockState( this.pos ).getBlock();
		if( blk instanceof AEBaseBlock )
		{
			return this.getAccessibleSlotsBySide( ( (AEBaseBlock) blk ).mapRotation( this, side ) );
		}
		return this.getAccessibleSlotsBySide( side );
	}

	@Override
	public boolean canInsertItem( final int slotIndex, final ItemStack insertingItem, final EnumFacing side )
	{
		return this.isItemValidForSlot( slotIndex, insertingItem );
	}

	@Override
	public boolean canExtractItem( final int slotIndex, final ItemStack extractedItem, final EnumFacing side )
	{
		return true;
	}

	@Override
	public void clear()
	{
		this.getInternalInventory().clear();
	}

	@Override
	public int getField( final int id )
	{
		return 0;
	}

	@Override
	public void setField( final int id, final int value )
	{

	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		if( this.hasCustomName() )
		{
			return new TextComponentString( this.getCustomName() );
		}
		return new TextComponentTranslation( this.getBlockType().getUnlocalizedName() );
	}

	public abstract int[] getAccessibleSlotsBySide( EnumFacing whichSide );

	@Override
	public boolean hasCapability( Capability<?> capability, EnumFacing facing )
	{
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability( capability, facing );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public <T> T getCapability( Capability<T> capability, @Nullable EnumFacing facing )
	{
		if( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
		{
			if( facing == null )
			{
				if( itemHandler == null )
				{
					itemHandler = new InvWrapper( getInternalInventory() );
				}
				return (T) itemHandler;
			}
			else
			{
				return (T) sidedItemHandler.computeIfAbsent( facing, side -> new SidedInvWrapper( this, side ) );
			}
		}
		return super.getCapability( capability, facing );
	}

}
