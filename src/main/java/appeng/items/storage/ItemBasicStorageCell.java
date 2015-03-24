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

package appeng.items.storage;


import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.base.Optional;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.materials.MaterialType;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;


public class ItemBasicStorageCell extends AEBaseItem implements IStorageCell, IItemGroup
{
	final MaterialType component;
	final int totalBytes;
	final int perType;
	final double idleDrain;

	public ItemBasicStorageCell( MaterialType whichCell, int kilobytes )
	{
		super( ItemBasicStorageCell.class, Optional.of( kilobytes + "k" ) );

		this.setFeature( EnumSet.of( AEFeature.StorageCells ) );
		this.setMaxStackSize( 1 );
		this.totalBytes = kilobytes * 1024;
		this.component = whichCell;

		switch( this.component )
		{
			case Cell1kPart:
				this.idleDrain = 0.5;
				this.perType = 8;
				break;
			case Cell4kPart:
				this.idleDrain = 1.0;
				this.perType = 32;
				break;
			case Cell16kPart:
				this.idleDrain = 1.5;
				this.perType = 128;
				break;
			case Cell64kPart:
				this.idleDrain = 2.0;
				this.perType = 512;
				break;
			default:
				this.idleDrain = 0.0;
				this.perType = 8;
		}
	}

	@Override
	public void addCheckedInformation( ItemStack stack, EntityPlayer player, List<String> lines, boolean displayAdditionalInformation )
	{
		IMEInventoryHandler inventory = AEApi.instance().registries().cell().getCellInventory( stack, null, StorageChannel.ITEMS );

		if( inventory instanceof ICellInventoryHandler )
		{
			ICellInventoryHandler handler = (ICellInventoryHandler) inventory;
			ICellInventory cellInventory = handler.getCellInv();

			if( cellInventory != null )
			{
				lines.add( cellInventory.getUsedBytes() + " " + GuiText.Of.getLocal() + ' ' + cellInventory.getTotalBytes() + ' ' + GuiText.BytesUsed.getLocal() );

				lines.add( cellInventory.getStoredItemTypes() + " " + GuiText.Of.getLocal() + ' ' + cellInventory.getTotalItemTypes() + ' ' + GuiText.Types.getLocal() );

				if( handler.isPreformatted() )
				{
					String List = ( handler.getIncludeExcludeMode() == IncludeExclude.WHITELIST ? GuiText.Included : GuiText.Excluded ).getLocal();

					if( handler.isFuzzy() )
						lines.add( GuiText.Partitioned.getLocal() + " - " + List + ' ' + GuiText.Fuzzy.getLocal() );
					else
						lines.add( GuiText.Partitioned.getLocal() + " - " + List + ' ' + GuiText.Precise.getLocal() );
				}
			}
		}
	}

	@Override
	public int getBytes( ItemStack cellItem )
	{
		return this.totalBytes;
	}

	@Override
	public int BytePerType( ItemStack cell )
	{
		return this.perType;
	}

	@Override
	public int getTotalTypes( ItemStack cellItem )
	{
		return 63;
	}

	@Override
	public boolean isBlackListed( ItemStack cellItem, IAEItemStack requestedAddition )
	{
		return false;
	}

	@Override
	public boolean storableInStorageCell()
	{
		return false;
	}

	@Override
	public boolean isStorageCell( ItemStack i )
	{
		return true;
	}

	@Override
	public double getIdleDrain()
	{
		return this.idleDrain;
	}

	@Override
	public String getUnlocalizedGroupName( Set<ItemStack> others, ItemStack is )
	{
		return GuiText.StorageCells.getUnlocalized();
	}

	@Override
	public boolean isEditable( ItemStack is )
	{
		return true;
	}

	@Override
	public IInventory getUpgradesInventory( ItemStack is )
	{
		return new CellUpgrades( is, 2 );
	}

	@Override
	public IInventory getConfigInventory( ItemStack is )
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode( ItemStack is )
	{
		String fz = Platform.openNbtData( is ).getString( "FuzzyMode" );
		try
		{
			return FuzzyMode.valueOf( fz );
		}
		catch( Throwable t )
		{
			return FuzzyMode.IGNORE_ALL;
		}
	}

	@Override
	public void setFuzzyMode( ItemStack is, FuzzyMode fzMode )
	{
		Platform.openNbtData( is ).setString( "FuzzyMode", fzMode.name() );
	}

	@Override
	public ItemStack onItemRightClick( ItemStack stack, World world, EntityPlayer player )
	{
		this.disassembleDrive( stack, world, player );
		return stack;
	}

	private boolean disassembleDrive( ItemStack stack, World world, EntityPlayer player )
	{
		if( player.isSneaking() )
		{
			if( Platform.isClient() )
				return false;

			InventoryPlayer playerInventory = player.inventory;
			IMEInventoryHandler inv = AEApi.instance().registries().cell().getCellInventory( stack, null, StorageChannel.ITEMS );
			if( inv != null && playerInventory.getCurrentItem() == stack )
			{
				InventoryAdaptor ia = InventoryAdaptor.getAdaptor( player, ForgeDirection.UNKNOWN );
				IItemList<IAEItemStack> list = inv.getAvailableItems( StorageChannel.ITEMS.createList() );
				if( list.isEmpty() && ia != null )
				{
					playerInventory.setInventorySlotContents( playerInventory.currentItem, null );

					ItemStack extraB = ia.addItems( this.component.stack( 1 ) );
					ItemStack extraA = ia.addItems( AEApi.instance().materials().materialEmptyStorageCell.stack( 1 ) );

					if( extraA != null )
						player.dropPlayerItemWithRandomChoice( extraA, false );
					if( extraB != null )
						player.dropPlayerItemWithRandomChoice( extraB, false );

					if( player.inventoryContainer != null )
						player.inventoryContainer.detectAndSendChanges();

					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onItemUseFirst( ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ )
	{
		return this.disassembleDrive( stack, world, player );
	}

	@Override
	public ItemStack getContainerItem( ItemStack itemStack )
	{
		return AEApi.instance().materials().materialEmptyStorageCell.stack( 1 );
	}

	@Override
	public boolean hasContainerItem( ItemStack stack )
	{
		return AEConfig.instance.isFeatureEnabled( AEFeature.enableDisassemblyCrafting );
	}
}
