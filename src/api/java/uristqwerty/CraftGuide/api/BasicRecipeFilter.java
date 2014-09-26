package uristqwerty.CraftGuide.api;

import net.minecraft.item.ItemStack;

/**
 * Supports a simple filter, allowing recipes to be removed
 * if they match certain conditions.
 */
public interface BasicRecipeFilter
{
	/**
	 * When CraftGuide loads or reloads its recipe list, this
	 * method will be called for every recipe. If this method
	 * returns false for a recipe, that recipe will be removed.
	 *
	 * @param recipe
	 * @param recipeType
	 * @return
	 */
	public boolean shouldKeepRecipe(CraftGuideRecipe recipe, ItemStack recipeType);
}
