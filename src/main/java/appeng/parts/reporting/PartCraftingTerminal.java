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

package appeng.parts.reporting;


import appeng.client.texture.CableBusTextures;
import appeng.core.sync.GuiBridge;
import appeng.helpers.Reflected;
import appeng.tile.inventory.AppEngInternalInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;


public class PartCraftingTerminal extends AbstractPartTerminal
{
	private static final CableBusTextures FRONT_BRIGHT_ICON = CableBusTextures.PartCraftingTerm_Bright;
	private static final CableBusTextures FRONT_DARK_ICON = CableBusTextures.PartCraftingTerm_Dark;
	private static final CableBusTextures FRONT_COLORED_ICON = CableBusTextures.PartCraftingTerm_Colored;

	private final AppEngInternalInventory craftingGrid = new AppEngInternalInventory( this, 9 );

	@Reflected
	public PartCraftingTerminal( final ItemStack is )
	{
		super( is );
	}

	@Override
	public void getDrops( final List<ItemStack> drops, final boolean wrenched )
	{
		super.getDrops( drops, wrenched );

		for( final ItemStack is : this.craftingGrid )
		{
			if( is != null )
			{
				drops.add( is );
			}
		}
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.craftingGrid.readFromNBT( data, "craftingGrid" );
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );
		this.craftingGrid.writeToNBT( data, "craftingGrid" );
	}

	@Override
	public GuiBridge getGui( final EntityPlayer p )
	{
		int x = (int) p.posX;
		int y = (int) p.posY;
		int z = (int) p.posZ;
		if( this.getHost().getTile() != null )
		{
			x = this.getTile().xCoord;
			y = this.getTile().yCoord;
			z = this.getTile().zCoord;
		}

		if( GuiBridge.GUI_CRAFTING_TERMINAL.hasPermissions( this.getHost().getTile(), x, y, z, this.getSide(), p ) )
		{
			return GuiBridge.GUI_CRAFTING_TERMINAL;
		}
		return GuiBridge.GUI_ME;
	}

	@Override
	public IInventory getInventoryByName( final String name )
	{
		if( name.equals( "crafting" ) )
		{
			return this.craftingGrid;
		}
		return super.getInventoryByName( name );
	}

	@Override
	public CableBusTextures getFrontBright()
	{
		return FRONT_BRIGHT_ICON;
	}

	@Override
	public CableBusTextures getFrontColored()
	{
		return FRONT_COLORED_ICON;
	}

	@Override
	public CableBusTextures getFrontDark()
	{
		return FRONT_DARK_ICON;
	}
}
