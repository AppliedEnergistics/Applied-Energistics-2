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

package appeng.integration.modules.NEIHelpers;


import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketNEIRecipe;
import appeng.util.Platform;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.LinkedList;
import java.util.List;


public class NEICraftingHandler implements IOverlayHandler
{

	public NEICraftingHandler( final int x, final int y )
	{
	}

	@Override
	public void overlayRecipe( final GuiContainer gui, final IRecipeHandler recipe, final int recipeIndex, final boolean shift )
	{
		try
		{
			final List ingredients = recipe.getIngredientStacks( recipeIndex );
			this.overlayRecipe( gui, ingredients, shift );
		}
		catch( final Exception ignored )
		{
		}
		catch( final Error ignored )
		{
		}
	}

	private void overlayRecipe( final GuiContainer gui, final List<PositionedStack> ingredients, final boolean shift )
	{
		try
		{
			final NBTTagCompound recipe = new NBTTagCompound();

			if( gui instanceof GuiCraftingTerm || gui instanceof GuiPatternTerm )
			{
				for( final PositionedStack positionedStack : ingredients )
				{
					final int col = ( positionedStack.relx - 25 ) / 18;
					final int row = ( positionedStack.rely - 6 ) / 18;
					if( positionedStack.items != null && positionedStack.items.length > 0 )
					{
						for( final Slot slot : (List<Slot>) gui.inventorySlots.inventorySlots )
						{
							if( slot instanceof SlotCraftingMatrix || slot instanceof SlotFakeCraftingMatrix )
							{
								if( slot.getSlotIndex() == col + row * 3 )
								{
									final NBTTagList tags = new NBTTagList();
									final List<ItemStack> list = new LinkedList<ItemStack>();

									// prefer pure crystals.
									for( int x = 0; x < positionedStack.items.length; x++ )
									{
										if( Platform.isRecipePrioritized( positionedStack.items[x] ) )
										{
											list.add( 0, positionedStack.items[x] );
										}
										else
										{
											list.add( positionedStack.items[x] );
										}
									}

									for( final ItemStack is : list )
									{
										final NBTTagCompound tag = new NBTTagCompound();
										is.writeToNBT( tag );
										tags.appendTag( tag );
									}

									recipe.setTag( "#" + slot.getSlotIndex(), tags );
									break;
								}
							}
						}
					}
				}

				NetworkHandler.instance.sendToServer( new PacketNEIRecipe( recipe ) );
			}
		}
		catch( final Exception ignored )
		{
		}
		catch( final Error ignored )
		{
		}
	}
}
