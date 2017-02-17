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


import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.MissingDefinition;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IReadOnly;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.Platform;


public final class ItemReadOnlyStorageCell extends AEBaseItem implements IStorageCell, IItemGroup, IReadOnly
{
	private final int totalBytes;
	private final int perType;
	private final double idleDrain;

	public ItemReadOnlyStorageCell()
	{
		this.setMaxStackSize( 1 );
		this.totalBytes = 64 * 1024;
		this.idleDrain = 2.0;
		this.perType = 512;
	}

	@Override
	public void addCheckedInformation( final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
	{
		final IMEInventoryHandler<?> inventory = AEApi.instance().registries().cell().getCellInventory( stack, null, StorageChannel.ITEMS );

		if( inventory instanceof ICellInventoryHandler )
		{
			final ICellInventoryHandler handler = (ICellInventoryHandler) inventory;
			final ICellInventory cellInventory = handler.getCellInv();

			if( cellInventory != null )
			{
				if( getReadOnly( stack ) )
				{
					if( cellInventory.getUsedBytes() == 0 )
					{
						lines.add( GuiText.Empty.getLocal() );
					}
					else
					{
						lines.add( cellInventory.getUsedBytes() + " " + GuiText.BytesUsed.getLocal() );

						lines.add( cellInventory.getStoredItemTypes() + " " + GuiText.Types.getLocal() );
					}
				}
				else
				{
					lines.add( cellInventory.getUsedBytes() + " " + GuiText.Of.getLocal() + ' ' + cellInventory.getTotalBytes() + ' ' + GuiText.BytesUsed.getLocal() );

					lines.add( cellInventory.getStoredItemTypes() + " " + GuiText.Of.getLocal() + ' ' + cellInventory.getTotalItemTypes() + ' ' + GuiText.Types.getLocal() );
				}
			}
		}
	}

	@Override
	public int getBytes( final ItemStack cellItem )
	{
		return this.totalBytes;
	}

	@Override
	public int getBytesPerType( final ItemStack cellItem )
	{
		return this.perType;
	}

	@Override
	public int getTotalTypes( final ItemStack cellItem )
	{
		return 63;
	}

	@Override
	public boolean isBlackListed( final ItemStack cellItem, final IAEItemStack requestedAddition )
	{
		return getReadOnly( cellItem );
	}

	@Override
	public boolean storableInStorageCell()
	{
		return false;
	}

	@Override
	public boolean isStorageCell( final ItemStack i )
	{
		return true;
	}

	@Override
	public double getIdleDrain()
	{
		return this.idleDrain;
	}

	@Override
	public String getUnlocalizedGroupName( final Set<ItemStack> others, final ItemStack is )
	{
		return GuiText.StorageCells.getUnlocalized();
	}

	@Override
	public boolean isEditable( final ItemStack is )
	{
		return false;
	}

	@Override
	public IInventory getUpgradesInventory( final ItemStack is )
	{
		return new CellUpgrades( is, 0 );
	}

	@Override
	public IInventory getConfigInventory( final ItemStack is )
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode( final ItemStack is )
	{
		return FuzzyMode.IGNORE_ALL;
	}

	@Override
	public void setFuzzyMode( final ItemStack is, final FuzzyMode fzMode )
	{
		// No fuzzy mode, the cell won't accept upgrades
		// Similar to ItemCreativeStorageCell
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick( final ItemStack stack, final World world, final EntityPlayer player, final EnumHand hand )
	{
		this.lockDrive( stack, player );
		return new ActionResult<>( EnumActionResult.SUCCESS, stack );
	}

	private boolean lockDrive( final ItemStack stack, final EntityPlayer player )
	{
		if( player.isSneaking() )
		{
			if( Platform.isClient() )
			{
				return false;
			}

			setReadOnly( stack, true );
			return true;
		}
		return false;
	}

	@Override
	public EnumActionResult onItemUseFirst( final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand )
	{
		return this.lockDrive( stack, player ) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
	}

	@Override
	public ItemStack getContainerItem( final ItemStack itemStack )
	{
		return AEApi.instance().definitions().materials().emptyStorageCell().maybeStack( 1 )
				.orElseThrow( () -> new MissingDefinition( "Tried to use empty storage cells while basic storage cells are defined." ) );
	}

	@Override
	public boolean hasContainerItem( final ItemStack stack )
	{
		return AEConfig.instance().isFeatureEnabled( AEFeature.ENABLE_DISASSEMBLY_CRAFTING );
	}

	@Override
	public void setReadOnly( ItemStack is, boolean readOnly )
	{
		final NBTTagCompound c = Platform.openNbtData( is );
		c.setBoolean( "ReadOnly", readOnly );
	}

	@Override
	public boolean getReadOnly( ItemStack is )
	{
		final NBTTagCompound c = Platform.openNbtData( is );
		return c.hasKey( "ReadOnly" ) && c.getBoolean( "ReadOnly" );
	}
}
