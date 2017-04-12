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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.Iterator;


public class ContainerCellWorkbench extends ContainerUpgradeable
{
	private final TileCellWorkbench workBench;
	private final AppEngNullInventory nullInventory = new AppEngNullInventory();
	@GuiSync( 2 )
	public CopyMode copyMode = CopyMode.CLEAR_ON_REMOVE;
	private ItemStack prevStack = null;
	private int lastUpgrades = 0;

	public ContainerCellWorkbench( final InventoryPlayer ip, final TileCellWorkbench te )
	{
		super( ip, te );
		this.workBench = te;
	}

	public void setFuzzy( final FuzzyMode valueOf )
	{
		final ICellWorkbenchItem cwi = this.workBench.getCell();
		if( cwi != null )
		{
			cwi.setFuzzyMode( this.workBench.getInventoryByName( "cell" ).getStackInSlot( 0 ), valueOf );
		}
	}

	public void nextWorkBenchCopyMode()
	{
		this.workBench.getConfigManager().putSetting( Settings.COPY_MODE, Platform.nextEnum( this.getWorkBenchCopyMode() ) );
	}

	private CopyMode getWorkBenchCopyMode()
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
		final IInventory cell = this.getUpgradeable().getInventoryByName( "cell" );
		this.addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.WORKBENCH_CELL, cell, 0, 152, 8, this.getInventoryPlayer() ) );

		final IInventory inv = this.getUpgradeable().getInventoryByName( "config" );
		final IInventory upgradeInventory = new Upgrades();
		// null, 3 * 8 );

		int offset = 0;
		final int x = 8;
		final int y = 29;
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
				final int iSLot = zz * 8 + z;
				this.addSlotToContainer( new OptionalSlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgradeInventory, this, iSLot, 187 + zz * 18, 8 + 18 * z, iSLot, this.getInventoryPlayer() ) );
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
	public int availableUpgrades()
	{
		final ItemStack is = this.workBench.getInventoryByName( "cell" ).getStackInSlot( 0 );
		if( this.prevStack != is )
		{
			this.prevStack = is;
			this.lastUpgrades = this.getCellUpgradeInventory().getSizeInventory();
		}
		return this.lastUpgrades;
	}

	@Override
	public void detectAndSendChanges()
	{
		final ItemStack is = this.workBench.getInventoryByName( "cell" ).getStackInSlot( 0 );
		if( Platform.isServer() )
		{
			if( this.workBench.getWorldObj().getTileEntity( this.workBench.xCoord, this.workBench.yCoord, this.workBench.zCoord ) != this.workBench )
			{
				this.setValidContainer( false );
			}

			for( final Object crafter : this.crafters )
			{
				final ICrafting icrafting = (ICrafting) crafter;

				if( this.prevStack != is )
				{
					// if the bars changed an item was probably made, so just send shit!
					for( final Object s : this.inventorySlots )
					{
						if( s instanceof OptionalSlotRestrictedInput )
						{
							final OptionalSlotRestrictedInput sri = (OptionalSlotRestrictedInput) s;
							icrafting.sendSlotContents( this, sri.slotNumber, sri.getStack() );
						}
					}
					( (EntityPlayerMP) icrafting ).isChangingQuantityOnly = false;
				}
			}

			this.setCopyMode( this.getWorkBenchCopyMode() );
			this.setFuzzyMode( this.getWorkBenchFuzzyMode() );
		}

		this.prevStack = is;
		this.standardDetectAndSendChanges();
	}

	@Override
	public boolean isSlotEnabled( final int idx )
	{
		return idx < this.availableUpgrades();
	}

	public IInventory getCellUpgradeInventory()
	{
		final IInventory upgradeInventory = this.workBench.getCellUpgradeInventory();

		return upgradeInventory == null ? this.nullInventory : upgradeInventory;
	}

	@Override
	public void onUpdate( final String field, final Object oldValue, final Object newValue )
	{
		if( field.equals( "copyMode" ) )
		{
			this.workBench.getConfigManager().putSetting( Settings.COPY_MODE, this.getCopyMode() );
		}

		super.onUpdate( field, oldValue, newValue );
	}

	public void clear()
	{
		final IInventory inv = this.getUpgradeable().getInventoryByName( "config" );
		for( int x = 0; x < inv.getSizeInventory(); x++ )
		{
			inv.setInventorySlotContents( x, null );
		}
		this.detectAndSendChanges();
	}

	private FuzzyMode getWorkBenchFuzzyMode()
	{
		final ICellWorkbenchItem cwi = this.workBench.getCell();
		if( cwi != null )
		{
			return cwi.getFuzzyMode( this.workBench.getInventoryByName( "cell" ).getStackInSlot( 0 ) );
		}
		return FuzzyMode.IGNORE_ALL;
	}

	public void partition()
	{
		final IInventory inv = this.getUpgradeable().getInventoryByName( "config" );

		final IMEInventory<IAEItemStack> cellInv = AEApi.instance().registries().cell().getCellInventory( this.getUpgradeable().getInventoryByName( "cell" ).getStackInSlot( 0 ), null, StorageChannel.ITEMS );

		Iterator<IAEItemStack> i = new NullIterator<IAEItemStack>();
		if( cellInv != null )
		{
			final IItemList<IAEItemStack> list = cellInv.getAvailableItems( AEApi.instance().storage().createItemList() );
			i = list.iterator();
		}

		for( int x = 0; x < inv.getSizeInventory(); x++ )
		{
			if( i.hasNext() )
			{
				final ItemStack g = i.next().getItemStack();
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

	public CopyMode getCopyMode()
	{
		return this.copyMode;
	}

	private void setCopyMode( final CopyMode copyMode )
	{
		this.copyMode = copyMode;
	}

	private class Upgrades implements IInventory
	{

		@Override
		public int getSizeInventory()
		{
			return ContainerCellWorkbench.this.getCellUpgradeInventory().getSizeInventory();
		}

		@Override
		public ItemStack getStackInSlot( final int i )
		{
			return ContainerCellWorkbench.this.getCellUpgradeInventory().getStackInSlot( i );
		}

		@Override
		public ItemStack decrStackSize( final int i, final int j )
		{
			final IInventory inv = ContainerCellWorkbench.this.getCellUpgradeInventory();
			final ItemStack is = inv.decrStackSize( i, j );
			inv.markDirty();
			return is;
		}

		@Override
		public ItemStack getStackInSlotOnClosing( final int i )
		{
			final IInventory inv = ContainerCellWorkbench.this.getCellUpgradeInventory();
			final ItemStack is = inv.getStackInSlotOnClosing( i );
			inv.markDirty();
			return is;
		}

		@Override
		public void setInventorySlotContents( final int i, final ItemStack itemstack )
		{
			final IInventory inv = ContainerCellWorkbench.this.getCellUpgradeInventory();
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
		public boolean isUseableByPlayer( final EntityPlayer entityplayer )
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
		public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
		{
			return ContainerCellWorkbench.this.getCellUpgradeInventory().isItemValidForSlot( i, itemstack );
		}
	}
}
