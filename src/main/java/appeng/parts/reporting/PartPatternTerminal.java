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

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.core.sync.GuiBridge;
import appeng.helpers.Reflected;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;


public final class PartPatternTerminal extends PartTerminal
{
	private final AppEngInternalInventory crafting = new AppEngInternalInventory( this, 9 );
	private final AppEngInternalInventory output = new AppEngInternalInventory( this, 3 );
	private final AppEngInternalInventory pattern = new AppEngInternalInventory( this, 2 );

	private boolean craftingMode = true;

	@Reflected
	public PartPatternTerminal( ItemStack is )
	{
		super( is );

		this.frontBright = CableBusTextures.PartPatternTerm_Bright;
		this.frontColored = CableBusTextures.PartPatternTerm_Colored;
		this.frontDark = CableBusTextures.PartPatternTerm_Dark;
	}

	@Override
	public final void getDrops( List<ItemStack> drops, boolean wrenched )
	{
		for( ItemStack is : this.pattern )
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
		this.setCraftingRecipe( data.getBoolean( "craftingMode" ) );
		this.pattern.readFromNBT( data, "pattern" );
		this.output.readFromNBT( data, "outputList" );
		this.crafting.readFromNBT( data, "craftingGrid" );
	}

	@Override
	public final void writeToNBT( NBTTagCompound data )
	{
		super.writeToNBT( data );
		data.setBoolean( "craftingMode", this.craftingMode );
		this.pattern.writeToNBT( data, "pattern" );
		this.output.writeToNBT( data, "outputList" );
		this.crafting.writeToNBT( data, "craftingGrid" );
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

		if( GuiBridge.GUI_PATTERN_TERMINAL.hasPermissions( this.getHost().getTile(), x, y, z, this.side, p ) )
		{
			return GuiBridge.GUI_PATTERN_TERMINAL;
		}
		return GuiBridge.GUI_ME;
	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack )
	{
		if( inv == this.pattern && slot == 1 )
		{
			ItemStack is = this.pattern.getStackInSlot( 1 );
			if( is != null && is.getItem() instanceof ICraftingPatternItem )
			{
				ICraftingPatternItem pattern = (ICraftingPatternItem) is.getItem();
				ICraftingPatternDetails details = pattern.getPatternForItem( is, this.getHost().getTile().getWorldObj() );
				if( details != null )
				{
					this.setCraftingRecipe( details.isCraftable() );

					for( int x = 0; x < this.crafting.getSizeInventory() && x < details.getInputs().length; x++ )
					{
						IAEItemStack item = details.getInputs()[x];
						this.crafting.setInventorySlotContents( x, item == null ? null : item.getItemStack() );
					}

					for( int x = 0; x < this.output.getSizeInventory() && x < details.getOutputs().length; x++ )
					{
						IAEItemStack item = details.getOutputs()[x];
						this.output.setInventorySlotContents( x, item == null ? null : item.getItemStack() );
					}
				}
			}
		}
		else if( inv == this.crafting )
		{
			this.fixCraftingRecipes();
		}

		this.host.markForSave();
	}

	private void fixCraftingRecipes()
	{
		if( this.craftingMode )
		{
			for( int x = 0; x < this.crafting.getSizeInventory(); x++ )
			{
				ItemStack is = this.crafting.getStackInSlot( x );
				if( is != null )
				{
					is.stackSize = 1;
				}
			}
		}
	}

	public final boolean isCraftingRecipe()
	{
		return this.craftingMode;
	}

	public final void setCraftingRecipe( boolean craftingMode )
	{
		this.craftingMode = craftingMode;
		this.fixCraftingRecipes();
	}

	@Override
	public final IInventory getInventoryByName( String name )
	{
		if( name.equals( "crafting" ) )
		{
			return this.crafting;
		}

		if( name.equals( "output" ) )
		{
			return this.output;
		}

		if( name.equals( "pattern" ) )
		{
			return this.pattern;
		}

		return super.getInventoryByName( name );
	}
}
