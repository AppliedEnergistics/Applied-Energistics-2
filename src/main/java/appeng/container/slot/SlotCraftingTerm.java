/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.container.slot;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.BlockPos;
import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.items.storage.ItemViewCell;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorPlayerHand;
import appeng.util.item.AEItemStack;


public class SlotCraftingTerm extends AppEngCraftingSlot
{

	protected final IInventory craftInv;
	protected final IInventory pattern;

	private final BaseActionSource mySrc;
	private final IEnergySource energySrc;
	private final IStorageMonitorable storage;
	private final IContainerCraftingPacket container;

	public SlotCraftingTerm( EntityPlayer player, BaseActionSource mySrc, IEnergySource energySrc, IStorageMonitorable storage, IInventory cMatrix, IInventory secondMatrix, IInventory output, int x, int y, IContainerCraftingPacket ccp )
	{
		super( player, cMatrix, output, 0, x, y );
		this.energySrc = energySrc;
		this.storage = storage;
		this.mySrc = mySrc;
		this.pattern = cMatrix;
		this.craftInv = secondMatrix;
		this.container = ccp;
	}

	public IInventory getCraftingMatrix()
	{
		return this.craftInv;
	}

	@Override
	public boolean canTakeStack( EntityPlayer par1EntityPlayer )
	{
		return false;
	}

	@Override
	public void onPickupFromSlot( EntityPlayer p, ItemStack is )
	{
	}

	public void doClick( InventoryAction action, EntityPlayer who )
	{
		if( this.getStack() == null )
		{
			return;
		}
		if( Platform.isClient() )
		{
			return;
		}

		IMEMonitor<IAEItemStack> inv = this.storage.getItemInventory();
		int howManyPerCraft = this.getStack().stackSize;
		int maxTimesToCraft = 0;

		InventoryAdaptor ia = null;
		if( action == InventoryAction.CRAFT_SHIFT ) // craft into player inventory...
		{
			ia = InventoryAdaptor.getAdaptor( who, null );
			maxTimesToCraft = (int) Math.floor( (double) this.getStack().getMaxStackSize() / (double) howManyPerCraft );
		}
		else if( action == InventoryAction.CRAFT_STACK ) // craft into hand, full stack
		{
			ia = new AdaptorPlayerHand( who );
			maxTimesToCraft = (int) Math.floor( (double) this.getStack().getMaxStackSize() / (double) howManyPerCraft );
		}
		else
		// pick up what was crafted...
		{
			ia = new AdaptorPlayerHand( who );
			maxTimesToCraft = 1;
		}

		maxTimesToCraft = this.capCraftingAttempts( maxTimesToCraft );

		if( ia == null )
		{
			return;
		}

		ItemStack rs = Platform.cloneItemStack( this.getStack() );
		if( rs == null )
		{
			return;
		}

		for( int x = 0; x < maxTimesToCraft; x++ )
		{
			if( ia.simulateAdd( rs ) == null )
			{
				IItemList<IAEItemStack> all = inv.getStorageList();
				ItemStack extra = ia.addItems( this.craftItem( who, rs, inv, all ) );
				if( extra != null )
				{
					List<ItemStack> drops = new ArrayList<ItemStack>();
					drops.add( extra );
					Platform.spawnDrops( who.worldObj, new BlockPos( (int) who.posX, (int) who.posY, (int) who.posZ ), drops );
					return;
				}
			}
		}
	}

	protected int capCraftingAttempts( int maxTimesToCraft )
	{
		return maxTimesToCraft;
	}

	public ItemStack craftItem( EntityPlayer p, ItemStack request, IMEMonitor<IAEItemStack> inv, IItemList all )
	{
		// update crafting matrix...
		ItemStack is = this.getStack();

		if( is != null && Platform.isSameItem( request, is ) )
		{
			ItemStack[] set = new ItemStack[this.pattern.getSizeInventory()];

			// add one of each item to the items on the board...
			if( Platform.isServer() )
			{
				InventoryCrafting ic = new InventoryCrafting( new ContainerNull(), 3, 3 );
				for( int x = 0; x < 9; x++ )
				{
					ic.setInventorySlotContents( x, this.pattern.getStackInSlot( x ) );
				}

				IRecipe r = Platform.findMatchingRecipe( ic, p.worldObj );

				if( r == null )
				{
					Item target = request.getItem();
					if( target.isDamageable() && target.isRepairable() )
					{
						boolean isBad = false;
						for( int x = 0; x < ic.getSizeInventory(); x++ )
						{
							ItemStack pis = ic.getStackInSlot( x );
							if( pis == null )
							{
								continue;
							}
							if( pis.getItem() != target )
							{
								isBad = true;
							}
						}
						if( !isBad )
						{
							super.onPickupFromSlot( p, is );
							// actually necessary to cleanup this case...
							p.openContainer.onCraftMatrixChanged( this.craftInv );
							return request;
						}
					}
					return null;
				}

				is = r.getCraftingResult( ic );

				if( inv != null )
				{
					for( int x = 0; x < this.pattern.getSizeInventory(); x++ )
					{
						if( this.pattern.getStackInSlot( x ) != null )
						{
							set[x] = Platform.extractItemsByRecipe( this.energySrc, this.mySrc, inv, p.worldObj, r, is, ic, this.pattern.getStackInSlot( x ), x, all, Actionable.MODULATE, ItemViewCell.createFilter( this.container.getViewCells() ) );
							ic.setInventorySlotContents( x, set[x] );
						}
					}
				}
			}

			if( this.preCraft( p, inv, set, is ) )
			{
				this.makeItem( p, is );

				this.postCraft( p, inv, set, is );
			}

			// shouldn't be necessary...
			p.openContainer.onCraftMatrixChanged( this.craftInv );

			return is;
		}

		return null;
	}

	public boolean preCraft( EntityPlayer p, IMEMonitor<IAEItemStack> inv, ItemStack[] set, ItemStack result )
	{
		return true;
	}

	public void makeItem( EntityPlayer p, ItemStack is )
	{
		super.onPickupFromSlot( p, is );
	}

	public void postCraft( EntityPlayer p, IMEMonitor<IAEItemStack> inv, ItemStack[] set, ItemStack result )
	{
		List<ItemStack> drops = new ArrayList<ItemStack>();

		// add one of each item to the items on the board...
		if( Platform.isServer() )
		{
			// set new items onto the crafting table...
			for( int x = 0; x < this.craftInv.getSizeInventory(); x++ )
			{
				if( this.craftInv.getStackInSlot( x ) == null )
				{
					this.craftInv.setInventorySlotContents( x, set[x] );
				}
				else if( set[x] != null )
				{
					// eek! put it back!
					IAEItemStack fail = inv.injectItems( AEItemStack.create( set[x] ), Actionable.MODULATE, this.mySrc );
					if( fail != null )
					{
						drops.add( fail.getItemStack() );
					}
				}
			}
		}

		if( drops.size() > 0 )
		{
			Platform.spawnDrops( p.worldObj, new BlockPos( (int) p.posX, (int) p.posY, (int) p.posZ ), drops );
		}
	}
}
