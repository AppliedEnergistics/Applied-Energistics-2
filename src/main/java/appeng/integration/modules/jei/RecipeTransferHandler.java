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

package appeng.integration.modules.jei;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import appeng.container.implementations.ContainerPatternTerm;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.transfer.RecipeTransferErrorInternal;
import mezz.jei.transfer.RecipeTransferErrorTooltip;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;

import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketJEIRecipe;
import appeng.util.Platform;


class RecipeTransferHandler<T extends Container> implements IRecipeTransferHandler<T>
{

	private final Class<T> containerClass;

	RecipeTransferHandler( Class<T> containerClass )
	{
		this.containerClass = containerClass;
	}

	@Override
	public Class<T> getContainerClass()
	{
		return this.containerClass;
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe( T container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer )
	{
		final String recipeType = recipeLayout.getRecipeCategory().getUid();

		if (!recipeType.equals( VanillaRecipeCategoryUid.INFORMATION) && !recipeType.equals(VanillaRecipeCategoryUid.FUEL))
		{
			if( container instanceof ContainerPatternTerm )
			{
				if( !( (ContainerPatternTerm) container ).isCraftingMode() )
				{
					if( recipeType.equals( VanillaRecipeCategoryUid.CRAFTING ) )
					{
						return new RecipeTransferErrorTooltip( I18n.format( "gui.appliedenergistics2.CraftingPattern" ) );
					}
				}
				else if( !recipeType.equals( VanillaRecipeCategoryUid.CRAFTING ) )
				{
					return new RecipeTransferErrorTooltip( I18n.format( "gui.appliedenergistics2.ProcessingPattern" ) );
				}
			}
			else
			{
				return RecipeTransferErrorInternal.INSTANCE;
			}
		}

		if( !doTransfer )
		{
			return null;
		}

		Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();

		final NBTTagCompound recipe = new NBTTagCompound();
		final NBTTagList outputs = new NBTTagList();


		int slotIndex = 0;
		for( Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> ingredientEntry : ingredients.entrySet() )
		{
			IGuiIngredient<ItemStack> ingredient = ingredientEntry.getValue();
			if( !ingredient.isInput() )
			{
				ItemStack output = ingredient.getDisplayedIngredient();
				if( output != null )
				{
					final NBTTagCompound tag = new NBTTagCompound();
					output.writeToNBT( tag );
					outputs.appendTag( tag );
				}
				continue;
			}

			for( final Slot slot : container.inventorySlots )
			{
				if( slot instanceof SlotCraftingMatrix || slot instanceof SlotFakeCraftingMatrix )
				{
					if( slot.getSlotIndex() == slotIndex )
					{
						final NBTTagList tags = new NBTTagList();
						final List<ItemStack> list = new ArrayList<>();
						final ItemStack displayed = ingredient.getDisplayedIngredient();

						// prefer currently displayed item
						if( displayed != null && !displayed.isEmpty() )
						{
							list.add( displayed );
						}

						// prefer pure crystals.
						for( ItemStack stack : ingredient.getAllIngredients() )
						{
							if( Platform.isRecipePrioritized( stack ) )
							{
								list.add( 0, stack );
							}
							else
							{
								list.add( stack );
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

			slotIndex++;
		}

		recipe.setTag( "outputs", outputs );

		try
		{
			NetworkHandler.instance().sendToServer( new PacketJEIRecipe( recipe ) );
		}
		catch( IOException e )
		{
			AELog.debug( e );
		}

		return null;
	}
}
