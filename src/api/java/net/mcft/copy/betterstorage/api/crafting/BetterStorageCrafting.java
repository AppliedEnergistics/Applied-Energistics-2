package net.mcft.copy.betterstorage.api.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class BetterStorageCrafting {
	
	public static final List<IStationRecipe> recipes = new ArrayList<IStationRecipe>();
	
	private BetterStorageCrafting() {  }
	
	/** Adds a station recipe to the recipe list. */
	public static void addStationRecipe(IStationRecipe recipe) { recipes.add(recipe); }
	
	/** Creates and returns a crafting matching the input, or null if none was found. */
	public static StationCrafting findMatchingStationCrafting(ItemStack[] input) {
		RecipeBounds bounds = new RecipeBounds(input);
		for (IStationRecipe recipe : recipes) {
			StationCrafting crafting = recipe.checkMatch(input, bounds);
			if (crafting != null) return crafting;
		}
		return null;
	}
	
	// Utility functions
	
	/** Returns an IRecipeInput from the object. <br>
	 *  Can be an IRecipeInput, ItemStack, Item, Block or ore dictionary String. */
	public static IRecipeInput makeInput(Object obj) {
		if (obj instanceof IRecipeInput) return (IRecipeInput)obj;
		else if (obj instanceof ItemStack) return new RecipeInputItemStack((ItemStack)obj);
		else if (obj instanceof Item) return new RecipeInputItemStack(new ItemStack((Item)obj));
		else if (obj instanceof Block) return new RecipeInputItemStack(new ItemStack((Block)obj));
		else if (obj instanceof String) return new RecipeInputOreDict((String)obj, 1);
		else throw new IllegalArgumentException(
				String.format("Argument is %s, not an IRecipeInput, ItemStack, Item, Block or String.",
				              ((obj != null) ? obj.getClass().getSimpleName() : "null")));
	}
	
}
