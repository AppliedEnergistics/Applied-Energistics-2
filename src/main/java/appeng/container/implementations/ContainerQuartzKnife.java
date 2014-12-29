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

package appeng.container.implementations;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import appeng.api.AEApi;
import appeng.container.AEBaseContainer;
import appeng.container.slot.QuartzKnifeOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.items.contents.QuartzKnifeObj;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;

public class ContainerQuartzKnife extends AEBaseContainer implements IAEAppEngInventory, IInventory
{

	final QuartzKnifeObj toolInv;

	final AppEngInternalInventory inSlot = new AppEngInternalInventory( this, 1 );
	final SlotRestrictedInput metals;
	final QuartzKnifeOutput output;
	String myName = "";

	public void setName(String value)
	{
		this.myName = value;
	}

	public ContainerQuartzKnife(InventoryPlayer ip, QuartzKnifeObj te) {
		super( ip, null, null );
		this.toolInv = te;

		this.addSlotToContainer( this.metals = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.METAL_INGOTS, this.inSlot, 0, 94, 44, ip ) );
		this.addSlotToContainer( this.output = new QuartzKnifeOutput( this, 0, 134, 44, -1 ) );

		this.lockPlayerInventorySlot( ip.currentItem );

		this.bindPlayerInventory( ip, 0, 184 - /* height of player inventory */82 );
	}

	@Override
	public void detectAndSendChanges()
	{
		ItemStack currentItem = this.getPlayerInv().getCurrentItem();

		if ( currentItem != this.toolInv.getItemStack() )
		{
			if ( currentItem != null )
			{
				if ( Platform.isSameItem( this.toolInv.getItemStack(), currentItem ) )
					this.getPlayerInv().setInventorySlotContents( this.getPlayerInv().currentItem, this.toolInv.getItemStack() );
				else
					this.isContainerValid = false;
			}
			else
				this.isContainerValid = false;
		}

		super.detectAndSendChanges();
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		if ( this.inSlot.getStackInSlot( 0 ) != null )
			par1EntityPlayer.dropPlayerItemWithRandomChoice( this.inSlot.getStackInSlot( 0 ), false );
	}

	@Override
	public void saveChanges()
	{

	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{

	}

	@Override
	public int getSizeInventory()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int var1)
	{
		ItemStack input = this.inSlot.getStackInSlot( 0 );
		if ( input == null )
			return null;

		if ( SlotRestrictedInput.isMetalIngot( input ) )
		{
			if ( this.myName.length() > 0 )
			{
				ItemStack name = AEApi.instance().materials().materialNamePress.stack( 1 );
				NBTTagCompound c = Platform.openNbtData( name );
				c.setString( "InscribeName", this.myName );
				return name;
			}
		}

		return null;
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2)
	{
		ItemStack is = this.getStackInSlot( 0 );
		if ( is != null )
		{
			if ( this.makePlate() )
				return is;
		}
		return null;
	}

	private boolean makePlate()
	{
		if ( this.inSlot.decrStackSize( 0, 1 ) != null )
		{
			ItemStack item = this.toolInv.getItemStack();
			item.damageItem( 1, this.getPlayerInv().player );

			if ( item.stackSize == 0 )
			{
				this.getPlayerInv().mainInventory[this.getPlayerInv().currentItem] = null;
				MinecraftForge.EVENT_BUS.post( new PlayerDestroyItemEvent( this.getPlayerInv().player, item ) );
			}

			return true;
		}
		return false;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1)
	{
		return null;
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2)
	{
		if ( var2 == null && Platform.isServer() )
			this.makePlate();
	}

	@Override
	public String getInventoryName()
	{
		return "Quartz Knife Output";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public void markDirty()
	{

	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1)
	{
		return false;
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
	public boolean isItemValidForSlot(int var1, ItemStack var2)
	{
		return false;
	}

}
