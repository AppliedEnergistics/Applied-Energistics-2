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

package appeng.items.tools.powered;


import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.contents.PortableCellViewer;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.storage.CellInventoryHandler;
import appeng.util.Platform;
import com.google.common.base.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;


public class ToolPortableCell extends AEBasePoweredItem implements IStorageCell, IGuiItem, IItemGroup
{
	public ToolPortableCell()
	{
		super( AEConfig.instance.portableCellBattery, Optional.<String>absent() );
		this.setFeature( EnumSet.of( AEFeature.PortableCell, AEFeature.StorageCells, AEFeature.PoweredTools ) );
	}

	@Override
	public ItemStack onItemRightClick( final ItemStack item, final World w, final EntityPlayer player )
	{
		Platform.openGUI( player, null, ForgeDirection.UNKNOWN, GuiBridge.GUI_PORTABLE_CELL );
		return item;
	}

	@SideOnly( Side.CLIENT )
	@Override
	public boolean isFull3D()
	{
		return false;
	}

	@Override
	public void addCheckedInformation( final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
	{
		super.addCheckedInformation( stack, player, lines, displayMoreInfo );

		final IMEInventory<IAEItemStack> cdi = AEApi.instance().registries().cell().getCellInventory( stack, null, StorageChannel.ITEMS );

		if( cdi instanceof CellInventoryHandler )
		{
			final ICellInventory cd = ( (ICellInventoryHandler) cdi ).getCellInv();
			if( cd != null )
			{
				lines.add( cd.getUsedBytes() + " " + GuiText.Of.getLocal() + ' ' + cd.getTotalBytes() + ' ' + GuiText.BytesUsed.getLocal() );
				lines.add( cd.getStoredItemTypes() + " " + GuiText.Of.getLocal() + ' ' + cd.getTotalItemTypes() + ' ' + GuiText.Types.getLocal() );
			}
		}
	}

	@Override
	public int getBytes( final ItemStack cellItem )
	{
		return 512;
	}

	@Override
	public int BytePerType( final ItemStack cell )
	{
		return 8;
	}

	@Override
	public int getBytesPerType( final ItemStack cellItem )
	{
		return 8;
	}

	@Override
	public int getTotalTypes( final ItemStack cellItem )
	{
		return 27;
	}

	@Override
	public boolean isBlackListed( final ItemStack cellItem, final IAEItemStack requestedAddition )
	{
		return false;
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
		return 0.5;
	}

	@Override
	public String getUnlocalizedGroupName( final Set<ItemStack> others, final ItemStack is )
	{
		return GuiText.StorageCells.getUnlocalized();
	}

	@Override
	public boolean isEditable( final ItemStack is )
	{
		return true;
	}

	@Override
	public IInventory getUpgradesInventory( final ItemStack is )
	{
		return new CellUpgrades( is, 2 );
	}

	@Override
	public IInventory getConfigInventory( final ItemStack is )
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode( final ItemStack is )
	{
		final String fz = Platform.openNbtData( is ).getString( "FuzzyMode" );
		try
		{
			return FuzzyMode.valueOf( fz );
		}
		catch( final Throwable t )
		{
			return FuzzyMode.IGNORE_ALL;
		}
	}

	@Override
	public void setFuzzyMode( final ItemStack is, final FuzzyMode fzMode )
	{
		Platform.openNbtData( is ).setString( "FuzzyMode", fzMode.name() );
	}

	@Override
	public IGuiItemObject getGuiObject( final ItemStack is, final World w, final int x, final int y, final int z )
	{
		return new PortableCellViewer( is, x );
	}
}
