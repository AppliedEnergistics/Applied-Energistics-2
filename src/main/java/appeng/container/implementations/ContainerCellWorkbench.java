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


public final class ContainerCellWorkbench extends ContainerUpgradeable
{
	private final TileCellWorkbench workBench;
	private final AppEngNullInventory nullInventory = new AppEngNullInventory();
	@GuiSync( 2 )
	public CopyMode copyMode = CopyMode.CLEAR_ON_REMOVE;
	private ItemStack prevStack = null;
	private int lastUpgrades = 0;
	private ItemStack LastCell;

	public ContainerCellWorkbench( InventoryPlayer ip, TileCellWorkbench te )
	{
		super( ip, te );
		this.workBench = te;
	}

	public final void setFuzzy( FuzzyMode valueOf )
	{
		ICellWorkbenchItem cwi = this.workBench.getCell();
		if( cwi != null )
		{
			cwi.setFuzzyMode( this.workBench.getInventoryByName( "cell" ).getStackInSlot( 0 ), valueOf );
		}
	}

	public final void nextCopyMode()
	{
		this.workBench.getConfigManager().putSetting( Settings.COPY_MODE, Platform.nextEnum( this.getCopyMode() ) );
	}

	public final CopyMode getCopyMode()
	{
		return (CopyMode) this.workBench.getConfigManager().getSetting( Settings.COPY_MODE );
	}

	@Override
	protected int getHeight()
	{
		return 251;
	}

	@Override
	protected void setupConfig()
	{
		int x = 8;
		int y = 29;
		int offset = 0;

		IInventory cell = this.upgradeable.getInventoryByName( "cell" );
		this.addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.WORKBENCH_CELL, cell, 0, 152, 8, this.invPlayer ) );

		IInventory inv = this.upgradeable.getInventoryByName( "config" );
		IInventory upgradeInventory = new Upgrades();
		// null, 3 * 8 );

		for( int w = 0; w < 7; w++ )
		{
			for( int z = 0; z < 9; z++ )
			{
				this.addSlotToContainer( new SlotFakeTypeOnly( inv, offset, x + z * 18, y + w * 18 ) );
				offset++;
			}
		}

		for( int zz = 0; zz < 3; zz++ )
		{
			for( int z = 0; z < 8; z++ )
			{
				int iSLot = zz * 8 + z;
				this.addSlotToContainer( new OptionalSlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgradeInventory, this, iSLot, 187 + zz * 18, 8 + 18 * z, iSLot, this.invPlayer ) );
			}
		}
		/*
		 * if ( supportCapacity() ) { for (int w = 0; w < 2; w++) for (int z = 0; z < 9; z++) addSlotToContainer( new
		 * OptionalSlotFakeTypeOnly( inv, this, offset++, x, y, z, w, 1 ) );
		 * for (int w = 0; w < 2; w++) for (int z = 0; z < 9; z++) addSlotToContainer( new OptionalSlotFakeTypeOnly(
		 * inv, this, offset++, x, y, z, w + 2, 2 ) );
		 * for (int w = 0; w < 2; w++) for (int z = 0; z < 9; z++) addSlotToContainer( new OptionalSlotFakeTypeOnly(
		 * inv, this, offset++, x, y, z, w + 4, 3 ) ); }
		 */
	}

	@Override
	public final int availableUpgrades()
	{
		ItemStack is = this.workBench.getInventoryByName( "cell" ).getStackInSlot( 0 );
		if( this.prevStack != is )
		{
			this.prevStack = is;
			this.lastUpgrades = this.getCellUpgradeInventory().getSizeInventory();
		}
		return this.lastUpgrades;
	}

	@Override
	public final void detectAndSendChanges()
	{
		ItemStack is = this.workBench.getInventoryByName( "cell" ).getStackInSlot( 0 );
		if( Platform.isServer() )
		{
			if( this.workBench.getWorldObj().getTileEntity( this.workBench.xCoord, this.workBench.yCoord, this.workBench.zCoord ) != this.workBench )
			{
				this.isContainerValid = false;
			}

			for( Object crafter : this.crafters )
			{
				ICrafting icrafting = (ICrafting) crafter;

				if( this.prevStack != is )
				{
					// if the bars changed an item was probably made, so just send shit!
					for( Object s : this.inventorySlots )
					{
						if( s instanceof OptionalSlotRestrictedInput )
						{
							OptionalSlotRestrictedInput sri = (OptionalSlotRestrictedInput) s;
							icrafting.sendSlotContents( this, sri.slotNumber, sri.getStack() );
						}
					}
					( (EntityPlayerMP) icrafting ).isChangingQuantityOnly = false;
				}
			}

			this.copyMode = this.getCopyMode();
			this.fzMode = this.getFuzzyMode();
		}

		this.prevStack = is;
		this.standardDetectAndSendChanges();
	}

	@Override
	public boolean isSlotEnabled( int idx )
	{
		return idx < this.availableUpgrades();
	}

	public final IInventory getCellUpgradeInventory()
	{
		final IInventory upgradeInventory = this.workBench.getCellUpgradeInventory();

		return upgradeInventory == null ? this.nullInventory : upgradeInventory;
	}

	@Override
	public void onUpdate( String field, Object oldValue, Object newValue )
	{
		if( field.equals( "copyMode" ) )
		{
			this.workBench.getConfigManager().putSetting( Settings.COPY_MODE, this.copyMode );
		}

		super.onUpdate( field, oldValue, newValue );
	}

	public final void clear()
	{
		IInventory inv = this.upgradeable.getInventoryByName( "config" );
		for( int x = 0; x < inv.getSizeInventory(); x++ )
		{
			inv.setInventorySlotContents( x, null );
		}
		this.detectAndSendChanges();
	}

	private FuzzyMode getFuzzyMode()
	{
		ICellWorkbenchItem cwi = this.workBench.getCell();
		if( cwi != null )
		{
			return cwi.getFuzzyMode( this.workBench.getInventoryByName( "cell" ).getStackInSlot( 0 ) );
		}
		return FuzzyMode.IGNORE_ALL;
	}

	public final void partition()
	{
		IInventory inv = this.upgradeable.getInventoryByName( "config" );

		IMEInventory<IAEItemStack> cellInv = AEApi.instance().registries().cell().getCellInventory( this.upgradeable.getInventoryByName( "cell" ).getStackInSlot( 0 ), null, StorageChannel.ITEMS );

		Iterator<IAEItemStack> i = new NullIterator<IAEItemStack>();
		if( cellInv != null )
		{
			IItemList<IAEItemStack> list = cellInv.getAvailableItems( AEApi.instance().storage().createItemList() );
			i = list.iterator();
		}

		for( int x = 0; x < inv.getSizeInventory(); x++ )
		{
			if( i.hasNext() )
			{
				ItemStack g = i.next().getItemStack();
				g.stackSize = 1;
				inv.setInventorySlotContents( x, g );
			}
			else
			{
				inv.setInventorySlotContents( x, null );
			}
		}

		this.detectAndSendChanges();
	}

	private final class Upgrades implements IInventory
	{

		@Override
		public final int getSizeInventory()
		{
			return ContainerCellWorkbench.this.getCellUpgradeInventory().getSizeInventory();
		}

		@Override
		public final ItemStack getStackInSlot( int i )
		{
			return ContainerCellWorkbench.this.getCellUpgradeInventory().getStackInSlot( i );
		}

		@Override
		public final ItemStack decrStackSize( int i, int j )
		{
			IInventory inv = ContainerCellWorkbench.this.getCellUpgradeInventory();
			ItemStack is = inv.decrStackSize( i, j );
			inv.markDirty();
			return is;
		}

		@Override
		public final ItemStack getStackInSlotOnClosing( int i )
		{
			IInventory inv = ContainerCellWorkbench.this.getCellUpgradeInventory();
			ItemStack is = inv.getStackInSlotOnClosing( i );
			inv.markDirty();
			return is;
		}

		@Override
		public final void setInventorySlotContents( int i, ItemStack itemstack )
		{
			IInventory inv = ContainerCellWorkbench.this.getCellUpgradeInventory();
			inv.setInventorySlotContents( i, itemstack );
			inv.markDirty();
		}

		@Override
		public final String getInventoryName()
		{
			return "Upgrades";
		}

		@Override
		public final boolean hasCustomInventoryName()
		{
			return false;
		}

		@Override
		public final int getInventoryStackLimit()
		{
			return 1;
		}

		@Override
		public final void markDirty()
		{

		}

		@Override
		public final boolean isUseableByPlayer( EntityPlayer entityplayer )
		{
			return false;
		}

		@Override
		public final void openInventory()
		{
		}

		@Override
		public final void closeInventory()
		{
		}

		@Override
		public final boolean isItemValidForSlot( int i, ItemStack itemstack )
		{
			return ContainerCellWorkbench.this.getCellUpgradeInventory().isItemValidForSlot( i, itemstack );
		}
	}
}
