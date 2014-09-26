package net.mcft.copy.betterstorage.api.crafting;

import java.util.List;

import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IStationRecipe {
	
	/** If the input matches this recipe, returns a new station crafting
	 *  instance, specific for this input, or null if it didn't match. */
	StationCrafting checkMatch(ItemStack[] input, RecipeBounds bounds);
	
	// NEI related functions
	
	/** Returns a list of sample inputs that will match the recipe.
	 *  Used to display and cycle recipes for the NEI addon.
	 *  Return null if the recipe should not be displayed in NEI. */
	@SideOnly(Side.CLIENT)
	List<IRecipeInput[]> getSampleInputs();
	
	/** Returns a list of input items that may be used in this recipe.
	 *  Used to show recipes using these inputs for the NEI addon.
	 *  Return null if the inputs should be grabbed from getSampleInputs. */
	@SideOnly(Side.CLIENT)
	List<IRecipeInput> getPossibleInputs();
	
	/** Returns a list of output items that can result from this recipe.
	 *  Used to show recipes matching these outputs for the NEI addon.
	 *  Return null if the output made from getSampleInputs should be used. */
	@SideOnly(Side.CLIENT)
	List<ItemStack> getPossibleOutputs();
	
}
