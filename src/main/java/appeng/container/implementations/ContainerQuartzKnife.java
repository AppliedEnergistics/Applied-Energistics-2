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


import javax.annotation.Nonnull;

import appeng.items.contents.QuartzKnifeObj;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;


public class ContainerQuartzKnife extends AEBaseContainer
{

	private final QuartzKnifeObj toolInv;

	private final IItemHandler inSlot = new AppEngInternalInventory( null, 1, 1 );
	private String myName = "";

	public ContainerQuartzKnife( final InventoryPlayer ip, final QuartzKnifeObj te )
	{
		super( ip, null, null );
		this.toolInv = te;

		this.addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.METAL_INGOTS, this.inSlot, 0, 94, 44, ip ) );
		this.addSlotToContainer( new QuartzKniveSlot( this.inSlot, 0, 134, 44, -1 ) );

		this.lockPlayerInventorySlot( ip.currentItem );

		this.bindPlayerInventory( ip, 0, 184 - /* height of player inventory */82 );
	}

	public void setName( final String value )
	{
		this.myName = value;
	}

	@Override
	public void detectAndSendChanges()
	{
		final ItemStack currentItem = this.getPlayerInv().getCurrentItem();

		if( currentItem != this.toolInv.getItemStack() )
		{
			if( !currentItem.isEmpty() )
			{
				if( ItemStack.areItemsEqual( this.toolInv.getItemStack(), currentItem ) )
				{
					this.getPlayerInv().setInventorySlotContents( this.getPlayerInv().currentItem, this.toolInv.getItemStack() );
				}
				else
				{
					this.setValidContainer( false );
				}
			}
			else
			{
				this.setValidContainer( false );
			}
		}

		super.detectAndSendChanges();
	}

	@Override
	public void onContainerClosed( final EntityPlayer par1EntityPlayer )
	{
		if( this.inSlot.getStackInSlot( 0 ) != null )
		{
			par1EntityPlayer.dropItem( this.inSlot.getStackInSlot( 0 ), false );
		}
	}

	private class QuartzKniveSlot extends SlotOutput
	{
		QuartzKniveSlot( IItemHandler a, int b, int c, int d, int i )
		{
			super( a, b, c, d, i );
		}

		@Override
		public ItemStack getStack()
		{
			final IItemHandler baseInv = this.getItemHandler();
			final ItemStack input = baseInv.getStackInSlot( 0 );
			if( input == ItemStack.EMPTY )
			{
				return ItemStack.EMPTY;
			}

			if( SlotRestrictedInput.isMetalIngot( input ) )
			{
				if( ContainerQuartzKnife.this.myName.length() > 0 )
				{
					return AEApi.instance().definitions().materials().namePress().maybeStack( 1 ).map( namePressStack ->
					                                                                                   {
						                                                                                   final NBTTagCompound compound = Platform.openNbtData( namePressStack );
						                                                                                   compound.setString( "InscribeName", ContainerQuartzKnife.this.myName );

						                                                                                   return namePressStack;
					                                                                                   } ).orElse( ItemStack.EMPTY );
				}
			}
			return ItemStack.EMPTY;
		}

		@Override
		@Nonnull
		public ItemStack decrStackSize( int amount )
		{
			ItemStack ret = this.getStack();
			if( !ret.isEmpty() )
			{
				this.makePlate();
			}
			return ret;
		}

		@Override
		public void putStack( final ItemStack stack )
		{
			if( stack.isEmpty() )
			{
				this.makePlate();
			}
		}

		private void makePlate()
		{
			if( Platform.isServer() )
			{
				if( !this.getItemHandler().extractItem( 0, 1, false ).isEmpty() )
				{
					final ItemStack item = ContainerQuartzKnife.this.toolInv.getItemStack();
					final ItemStack before = item.copy();
					item.damageItem( 1, ContainerQuartzKnife.this.getPlayerInv().player );

					if( item.getCount() == 0 )
					{
						ContainerQuartzKnife.this.getPlayerInv()
								.setInventorySlotContents( ContainerQuartzKnife.this.getPlayerInv().currentItem, ItemStack.EMPTY );
						MinecraftForge.EVENT_BUS.post( new PlayerDestroyItemEvent( ContainerQuartzKnife.this.getPlayerInv().player, before, null ) );
					}

					ContainerQuartzKnife.this.detectAndSendChanges();
				}
			}
		}
	}
}
