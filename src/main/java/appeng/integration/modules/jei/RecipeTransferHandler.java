package appeng.integration.modules.jei;


import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
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
		return containerClass;
	}

	@Override
	public String getRecipeCategoryUid()
	{
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe( T container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer )
	{

		if( !doTransfer )
		{
			return null;
		}

		Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();

		final NBTTagCompound recipe = new NBTTagCompound();

		int slotIndex = 0;
		for( Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> ingredientEntry : ingredients.entrySet() )
		{
			IGuiIngredient<ItemStack> ingredient = ingredientEntry.getValue();
			if( !ingredient.isInput() )
			{
				continue;
			}

			for( final Slot slot : container.inventorySlots )
			{
				if( slot instanceof SlotCraftingMatrix || slot instanceof SlotFakeCraftingMatrix )
				{
					if( slot.getSlotIndex() == slotIndex )
					{
						final NBTTagList tags = new NBTTagList();
						final List<ItemStack> list = new LinkedList<ItemStack>();

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

		try
		{
			NetworkHandler.instance.sendToServer( new PacketJEIRecipe( recipe ) );
		}
		catch( IOException e )
		{
			AELog.debug( e );
		}

		return null;
	}
}
