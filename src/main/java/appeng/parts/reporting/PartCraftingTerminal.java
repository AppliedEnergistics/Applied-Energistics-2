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

package appeng.parts.reporting;


import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.client.texture.CableBusTextures;
import appeng.core.sync.GuiBridge;
import appeng.helpers.Reflected;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;


public final class PartCraftingTerminal extends PartTerminal
{
	private final AppEngInternalInventory craftingGrid = new AppEngInternalInventory( this, 9 );

	@Reflected
	public PartCraftingTerminal( ItemStack is )
	{
		super( is );

		this.frontBright = CableBusTextures.PartCraftingTerm_Bright;
		this.frontColored = CableBusTextures.PartCraftingTerm_Colored;
		this.frontDark = CableBusTextures.PartCraftingTerm_Dark;
		// frontSolid = CableBusTextures.PartCraftingTerm_Solid;
	}

	@Override
	public final void getDrops( List<ItemStack> drops, boolean wrenched )
	{
		super.getDrops( drops, wrenched );

		for( ItemStack is : this.craftingGrid )
		{
			if( is != null )
			{
				drops.add( is );
			}
		}
	}

	@Override
	public final void readFromNBT( NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.craftingGrid.readFromNBT( data, "craftingGrid" );
	}

	@Override
	public final void writeToNBT( NBTTagCompound data )
	{
		super.writeToNBT( data );
		this.craftingGrid.writeToNBT( data, "craftingGrid" );
	}

	@Override
	public GuiBridge getGui( EntityPlayer p )
	{
		int x = (int) p.posX;
		int y = (int) p.posY;
		int z = (int) p.posZ;
		if( this.getHost().getTile() != null )
		{
			x = this.tile.xCoord;
			y = this.tile.yCoord;
			z = this.tile.zCoord;
		}

		if( GuiBridge.GUI_CRAFTING_TERMINAL.hasPermissions( this.getHost().getTile(), x, y, z, this.side, p ) )
		{
			return GuiBridge.GUI_CRAFTING_TERMINAL;
		}
		return GuiBridge.GUI_ME;
	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack )
	{
		this.host.markForSave();
	}

	@Override
	public final IInventory getInventoryByName( String name )
	{
		if( name.equals( "crafting" ) )
		{
			return this.craftingGrid;
		}
		return super.getInventoryByName( name );
	}
}
