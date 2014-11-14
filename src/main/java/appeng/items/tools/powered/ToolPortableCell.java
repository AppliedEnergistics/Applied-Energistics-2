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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
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

public class ToolPortableCell extends AEBasePoweredItem implements IStorageCell, IGuiItem, IItemGroup
{

	public ToolPortableCell() {
		super( ToolPortableCell.class, null );
		setFeature( EnumSet.of( AEFeature.PortableCell, AEFeature.StorageCells, AEFeature.PoweredTools ) );
		maxStoredPower = AEConfig.instance.portableCellBattery;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack item, World w, EntityPlayer player)
	{
		Platform.openGUI( player, null, ForgeDirection.UNKNOWN, GuiBridge.GUI_PORTABLE_CELL );
		return item;
	}
	
	@Override
	public boolean onItemUse(ItemStack item, EntityPlayer player, World w, int x, int y, int z, int side,
			float hitX, float hitY, float hitZ)
	{
		onItemRightClick( item, w, player );
		return true;
	}

	@Override
	public void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> lines, boolean displayAdditionalInformation )
	{
		super.addCheckedInformation( stack, player, lines, displayAdditionalInformation );

		IMEInventory<IAEItemStack> cdi = AEApi.instance().registries().cell().getCellInventory( stack, null, StorageChannel.ITEMS );

		if ( cdi instanceof CellInventoryHandler )
		{
			ICellInventory cd = ((ICellInventoryHandler) cdi).getCellInv();
			if ( cd != null )
			{
				lines.add( cd.getUsedBytes() + " " + GuiText.Of.getLocal() + " " + cd.getTotalBytes() + " " + GuiText.BytesUsed.getLocal() );
				lines.add( cd.getStoredItemTypes() + " " + GuiText.Of.getLocal() + " " + cd.getTotalItemTypes() + " " + GuiText.Types.getLocal() );
			}
		}
	}

	@Override
	public int getBytes(ItemStack cellItem)
	{
		return 512;
	}

	@Override
	public int BytePerType(ItemStack cell)
	{
		return 8;
	}

	@Override
	public int getTotalTypes(ItemStack cellItem)
	{
		return 27;
	}

	@Override
	public boolean isBlackListed(ItemStack cellItem, IAEItemStack requestedAddition)
	{
		return false;
	}

	@Override
	public boolean storableInStorageCell()
	{
		return false;
	}

	@Override
	public boolean isStorageCell(ItemStack i)
	{
		return true;
	}

	@Override
	public double getIdleDrain()
	{
		return 0.5;
	}

	@Override
	public IInventory getUpgradesInventory(ItemStack is)
	{
		return new CellUpgrades( is, 2 );
	}

	@Override
	public IInventory getConfigInventory(ItemStack is)
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode(ItemStack is)
	{
		String fz = Platform.openNbtData( is ).getString( "FuzzyMode" );
		try
		{
			return FuzzyMode.valueOf( fz );
		}
		catch (Throwable t)
		{
			return FuzzyMode.IGNORE_ALL;
		}
	}

	@Override
	public String getUnlocalizedGroupName(Set<ItemStack> others, ItemStack is)
	{
		return GuiText.StorageCells.getUnlocalized();
	}

	@Override
	public void setFuzzyMode(ItemStack is, FuzzyMode fzMode)
	{
		Platform.openNbtData( is ).setString( "FuzzyMode", fzMode.name() );
	}

	@Override
	public boolean isEditable(ItemStack is)
	{
		return true;
	}

	@Override
	public IGuiItemObject getGuiObject(ItemStack is, World w, int x, int y, int z)
	{
		return new PortableCellViewer( is );
	}
}
