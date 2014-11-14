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

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import appeng.api.storage.ITerminalHost;
import appeng.container.ContainerNull;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotCraftingTerm;
import appeng.helpers.IContainerCraftingPacket;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;

public class ContainerCraftingTerm extends ContainerMEMonitorable implements IAEAppEngInventory, IContainerCraftingPacket
{

	final AppEngInternalInventory output = new AppEngInternalInventory( this, 1 );

	final SlotCraftingMatrix[] craftingSlots = new SlotCraftingMatrix[9];
	final SlotCraftingTerm outputSlot;

	public final PartCraftingTerminal ct;

	/**
	 * Callback for when the crafting matrix is changed.
	 */
	@Override
	public void onCraftMatrixChanged(IInventory par1IInventory)
	{
		ContainerNull cn = new ContainerNull();
		InventoryCrafting ic = new InventoryCrafting( cn, 3, 3 );

		for (int x = 0; x < 9; x++)
			ic.setInventorySlotContents( x, craftingSlots[x].getStack() );

		outputSlot.putStack( CraftingManager.getInstance().findMatchingRecipe( ic, getPlayerInv().player.worldObj ) );
	}

	public ContainerCraftingTerm(InventoryPlayer ip, ITerminalHost monitorable) {
		super( ip, monitorable, false );
		ct = (PartCraftingTerminal) monitorable;

		IInventory crafting = ct.getInventoryByName( "crafting" );

		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				addSlotToContainer( craftingSlots[x + y * 3] = new SlotCraftingMatrix( this, crafting, x + y * 3, 37 + x * 18, -72 + y * 18 ) );

		addSlotToContainer( outputSlot = new SlotCraftingTerm( getPlayerInv().player, mySrc, powerSrc, monitorable, crafting, crafting, output, 131, -72 + 18, this ) );

		bindPlayerInventory( ip, 0, 0 );

		onCraftMatrixChanged( crafting );
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
	public IInventory getInventoryByName(String name)
	{
		return ct.getInventoryByName( name );
	}

	@Override
	public boolean useRealItems()
	{
		return true;
	}
}
