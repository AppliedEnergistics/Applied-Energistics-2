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

import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.config.CopyMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.OptionalSlotRestrictedInput;
import appeng.container.slot.SlotFakeTypeOnly;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.inventory.AppEngNullInventory;
import appeng.tile.misc.TileCellWorkbench;
import appeng.util.Platform;
import appeng.util.iterators.NullIterator;

public class ContainerCellWorkbench extends ContainerUpgradeable
{

	final TileCellWorkbench workBench;
	final AppEngNullInventory ni = new AppEngNullInventory();

	public IInventory getCellUpgradeInventory()
	{
		IInventory ri = workBench.getCellUpgradeInventory();
		return ri == null ? ni : ri;
	}

	public void setFuzzy(FuzzyMode valueOf)
	{
		ICellWorkbenchItem cwi = workBench.getCell();
		if ( cwi != null )
			cwi.setFuzzyMode( workBench.getInventoryByName( "cell" ).getStackInSlot( 0 ), valueOf );
	}

	private FuzzyMode getFuzzyMode()
	{
		ICellWorkbenchItem cwi = workBench.getCell();
		if ( cwi != null )
			return cwi.getFuzzyMode( workBench.getInventoryByName( "cell" ).getStackInSlot( 0 ) );
		return FuzzyMode.IGNORE_ALL;
	}

	public void nextCopyMode()
	{
		workBench.getConfigManager().putSetting( Settings.COPY_MODE, Platform.nextEnum( getCopyMode() ) );
	}

	public CopyMode getCopyMode()
	{
		return (CopyMode) workBench.getConfigManager().getSetting( Settings.COPY_MODE );
	}

	class Upgrades implements IInventory
	{

		@Override
		public int getSizeInventory()
		{
			return getCellUpgradeInventory().getSizeInventory();
		}

		@Override
		public ItemStack getStackInSlot(int i)
		{
			return getCellUpgradeInventory().getStackInSlot( i );
		}

		@Override
		public ItemStack decrStackSize(int i, int j)
		{
			IInventory inv = getCellUpgradeInventory();
			ItemStack is = inv.decrStackSize( i, j );
			inv.markDirty();
			return is;
		}

		@Override
		public ItemStack getStackInSlotOnClosing(int i)
		{
			IInventory inv = getCellUpgradeInventory();
			ItemStack is = inv.getStackInSlotOnClosing( i );
			inv.markDirty();
			return is;
		}

		@Override
		public void setInventorySlotContents(int i, ItemStack itemstack)
		{
			IInventory inv = getCellUpgradeInventory();
			inv.setInventorySlotContents( i, itemstack );
			inv.markDirty();
		}

		@Override
		public String getInventoryName()
		{
			return "Upgrades";
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
		public boolean isUseableByPlayer(EntityPlayer entityplayer)
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
		public boolean isItemValidForSlot(int i, ItemStack itemstack)
		{
			return getCellUpgradeInventory().isItemValidForSlot( i, itemstack );
		}
	}

	IInventory UpgradeInventoryWrapper;

	ItemStack prevStack = null;
	int lastUpgrades = 0;

	@GuiSync(2)
	public CopyMode copyMode = CopyMode.CLEAR_ON_REMOVE;

	public ContainerCellWorkbench(InventoryPlayer ip, TileCellWorkbench te) {
		super( ip, te );
		workBench = te;
	}

	@Override
	protected int getHeight()
	{
		return 251;
	}

	@Override
	public int availableUpgrades()
	{
		ItemStack is = workBench.getInventoryByName( "cell" ).getStackInSlot( 0 );
		if ( prevStack != is )
		{
			prevStack = is;
			return lastUpgrades = getCellUpgradeInventory().getSizeInventory();
		}
		return lastUpgrades;
	}

	@Override
	public boolean isSlotEnabled(int idx)
	{
		return idx < availableUpgrades();
	}

	@Override
	protected void setupConfig()
	{
		int x = 8;
		int y = 29;
		int offset = 0;

		IInventory cell = upgradeable.getInventoryByName( "cell" );
		addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.WORKBENCH_CELL, cell, 0, 152, 8, invPlayer ) );

		IInventory inv = upgradeable.getInventoryByName( "config" );
		UpgradeInventoryWrapper = new Upgrades();// Platform.isServer() ? new Upgrades() : new AppEngInternalInventory(
													// null, 3 * 8 );

		for (int w = 0; w < 7; w++)
			for (int z = 0; z < 9; z++)
				addSlotToContainer( new SlotFakeTypeOnly( inv, offset++, x + z * 18, y + w * 18 ) );

		for (int zz = 0; zz < 3; zz++)
			for (int z = 0; z < 8; z++)
			{
				int iSLot = zz * 8 + z;
				addSlotToContainer( new OptionalSlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, UpgradeInventoryWrapper, this, iSLot, 187 + zz * 18,
						8 + 18 * z, iSLot, invPlayer ) );
			}
		/*
		 * if ( supportCapacity() ) { for (int w = 0; w < 2; w++) for (int z = 0; z < 9; z++) addSlotToContainer( new
		 * OptionalSlotFakeTypeOnly( inv, this, offset++, x, y, z, w, 1 ) );
		 * 
		 * for (int w = 0; w < 2; w++) for (int z = 0; z < 9; z++) addSlotToContainer( new OptionalSlotFakeTypeOnly(
		 * inv, this, offset++, x, y, z, w + 2, 2 ) );
		 * 
		 * for (int w = 0; w < 2; w++) for (int z = 0; z < 9; z++) addSlotToContainer( new OptionalSlotFakeTypeOnly(
		 * inv, this, offset++, x, y, z, w + 4, 3 ) ); }
		 */
	}

	ItemStack LastCell;

	@Override
	public void onUpdate(String field, Object oldValue, Object newValue)
	{
		if ( field.equals( "copyMode" ) )
			workBench.getConfigManager().putSetting( Settings.COPY_MODE, this.copyMode );

		super.onUpdate( field, oldValue, newValue );
	}

	@Override
	public void detectAndSendChanges()
	{
		ItemStack is = workBench.getInventoryByName( "cell" ).getStackInSlot( 0 );
		if ( Platform.isServer() )
		{
			for (Object crafter : this.crafters)
			{
				ICrafting icrafting = (ICrafting) crafter;

				if ( prevStack != is )
				{
					// if the bars changed an item was probably made, so just send shit!
					for (Object s : inventorySlots)
					{
						if ( s instanceof OptionalSlotRestrictedInput )
						{
							OptionalSlotRestrictedInput sri = (OptionalSlotRestrictedInput) s;
							icrafting.sendSlotContents( this, sri.slotNumber, sri.getStack() );
						}
					}
					((EntityPlayerMP) icrafting).isChangingQuantityOnly = false;
				}
			}

			this.copyMode = getCopyMode();
			this.fzMode = getFuzzyMode();
		}

		prevStack = is;
		standardDetectAndSendChanges();
	}

	public void clear()
	{
		IInventory inv = upgradeable.getInventoryByName( "config" );
		for (int x = 0; x < inv.getSizeInventory(); x++)
			inv.setInventorySlotContents( x, null );
		detectAndSendChanges();
	}

	public void partition()
	{
		IInventory inv = upgradeable.getInventoryByName( "config" );

		IMEInventory<IAEItemStack> cellInv = AEApi.instance().registries().cell()
				.getCellInventory( upgradeable.getInventoryByName( "cell" ).getStackInSlot( 0 ), null, StorageChannel.ITEMS );

		Iterator<IAEItemStack> i = new NullIterator<IAEItemStack>();
		if ( cellInv != null )
		{
			IItemList<IAEItemStack> list = cellInv.getAvailableItems( AEApi.instance().storage().createItemList() );
			i = list.iterator();
		}

		for (int x = 0; x < inv.getSizeInventory(); x++)
		{
			if ( i.hasNext() )
			{
				ItemStack g = i.next().getItemStack();
				g.stackSize = 1;
				inv.setInventorySlotContents( x, g );
			}
			else
				inv.setInventorySlotContents( x, null );
		}

		detectAndSendChanges();
	}

}
