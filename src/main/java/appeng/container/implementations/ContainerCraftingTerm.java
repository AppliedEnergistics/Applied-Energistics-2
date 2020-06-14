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


import appeng.api.config.SecurityPermissions;
import appeng.container.ContainerLocator;
import appeng.container.helper.ContainerHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

import appeng.api.storage.ITerminalHost;
import appeng.container.ContainerNull;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotCraftingTerm;
import appeng.helpers.IContainerCraftingPacket;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperInvItemHandler;


public class ContainerCraftingTerm extends ContainerMEMonitorable implements IAEAppEngInventory, IContainerCraftingPacket
{

	public static ContainerType<ContainerCraftingTerm> TYPE;

	private static final ContainerHelper<ContainerCraftingTerm, ITerminalHost> helper
			= new ContainerHelper<>(ContainerCraftingTerm::new, ITerminalHost.class, SecurityPermissions.CRAFT);

	public static ContainerCraftingTerm fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
		return helper.fromNetwork(windowId, inv, buf);
	}

	public static boolean open(PlayerEntity player, ContainerLocator locator) {
		return helper.open(player, locator);
	}

	private final PartCraftingTerminal ct;
	private final AppEngInternalInventory output = new AppEngInternalInventory( this, 1 );
	private final SlotCraftingMatrix[] craftingSlots = new SlotCraftingMatrix[9];
	private final SlotCraftingTerm outputSlot;
	private IRecipe<CraftingInventory> currentRecipe;

	public ContainerCraftingTerm(int id, final PlayerInventory ip, final ITerminalHost monitorable )
	{
		super( TYPE, id, ip, monitorable, false );
		this.ct = (PartCraftingTerminal) monitorable;

		final IItemHandler crafting = this.ct.getInventoryByName( "crafting" );

		for( int y = 0; y < 3; y++ )
		{
			for( int x = 0; x < 3; x++ )
			{
				this.addSlot( this.craftingSlots[x + y * 3] = new SlotCraftingMatrix( this, crafting, x + y * 3, 37 + x * 18, -72 + y * 18 ) );
			}
		}

		this.addSlot( this.outputSlot = new SlotCraftingTerm( this.getPlayerInv().player, this.getActionSource(), this
				.getPowerSource(), monitorable, crafting, crafting, this.output, 131, -72 + 18, this ) );

		this.bindPlayerInventory( ip, 0, 0 );

		this.onCraftMatrixChanged( new WrapperInvItemHandler( crafting ) );
	}

	/**
	 * Callback for when the crafting matrix is changed.
	 */

	@Override
	public void onCraftMatrixChanged( IInventory inventory )
	{
		final ContainerNull cn = new ContainerNull();
		final CraftingInventory ic = new CraftingInventory( cn, 3, 3 );

		for( int x = 0; x < 9; x++ )
		{
			ic.setInventorySlotContents( x, this.craftingSlots[x].getStack() );
		}

		if( this.currentRecipe == null || !this.currentRecipe.matches( ic, this.getPlayerInv().player.world ) )
		{
			World world = this.getPlayerInv().player.world;
			this.currentRecipe = world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, ic, world).orElse(null);
		}

		if( this.currentRecipe == null )
		{
			this.outputSlot.putStack( ItemStack.EMPTY );
		}
		else
		{
			final ItemStack craftingResult = this.currentRecipe.getCraftingResult( ic );

			this.outputSlot.putStack( craftingResult );
		}
	}

	@Override
	public void saveChanges()
	{

	}

	@Override
	public void onChangeInventory( final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack )
	{

	}

	@Override
	public IItemHandler getInventoryByName( final String name )
	{
		if( name.equals( "player" ) )
		{
			return new PlayerInvWrapper( this.getPlayerInventory() );
		}
		return this.ct.getInventoryByName( name );
	}

	@Override
	public boolean useRealItems()
	{
		return true;
	}

	public IRecipe<CraftingInventory> getCurrentRecipe()
	{
		return this.currentRecipe;
	}
}
