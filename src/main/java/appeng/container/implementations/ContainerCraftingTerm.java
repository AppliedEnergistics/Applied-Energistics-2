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


import appeng.api.storage.ITerminalHost;
import appeng.container.ContainerNull;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotCraftingTerm;
import appeng.helpers.IContainerCraftingPacket;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;


public class ContainerCraftingTerm extends ContainerMEMonitorable implements IAEAppEngInventory, IContainerCraftingPacket
{

	private final PartCraftingTerminal ct;
	private final AppEngInternalInventory output = new AppEngInternalInventory( this, 1 );
	private final SlotCraftingMatrix[] craftingSlots = new SlotCraftingMatrix[9];
	private final SlotCraftingTerm outputSlot;

	public ContainerCraftingTerm( final InventoryPlayer ip, final ITerminalHost monitorable )
	{
		super( ip, monitorable, false );
		this.ct = (PartCraftingTerminal) monitorable;

		final IInventory crafting = this.ct.getInventoryByName( "crafting" );

		for( int y = 0; y < 3; y++ )
		{
			for( int x = 0; x < 3; x++ )
			{
				this.addSlotToContainer( this.craftingSlots[x + y * 3] = new SlotCraftingMatrix( this, crafting, x + y * 3, 37 + x * 18, -72 + y * 18 ) );
			}
		}

		this.addSlotToContainer( this.outputSlot = new SlotCraftingTerm( this.getPlayerInv().player, this.getActionSource(), this.getPowerSource(), monitorable, crafting, crafting, this.output, 131, -72 + 18, this ) );

		this.bindPlayerInventory( ip, 0, 0 );

		this.onCraftMatrixChanged( crafting );
	}

	/**
	 * Callback for when the crafting matrix is changed.
	 */
	@Override
	public void onCraftMatrixChanged( final IInventory par1IInventory )
	{
		final ContainerNull cn = new ContainerNull();
		final InventoryCrafting ic = new InventoryCrafting( cn, 3, 3 );

		for( int x = 0; x < 9; x++ )
		{
			ic.setInventorySlotContents( x, this.craftingSlots[x].getStack() );
		}

		this.outputSlot.putStack( CraftingManager.getInstance().findMatchingRecipe( ic, this.getPlayerInv().player.worldObj ) );
	}

	@Override
	public void saveChanges()
	{

	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack )
	{

	}

	@Override
	public IInventory getInventoryByName( final String name )
	{
		if( name.equals( "player" ) )
		{
			return this.getInventoryPlayer();
		}
		return this.ct.getInventoryByName( name );
	}

	@Override
	public boolean useRealItems()
	{
		return true;
	}
}
