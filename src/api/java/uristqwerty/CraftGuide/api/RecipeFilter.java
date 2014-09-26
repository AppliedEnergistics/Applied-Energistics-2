package uristqwerty.CraftGuide.api;

import java.util.List;

import net.minecraft.item.ItemStack;

/**
 * More advanced than {@link BasicRecipeFilter}, this
 * interface also allows re-ordering and insertion of
 * entirely new recipes for each recipe type.
 */
public interface RecipeFilter
{
	/**
	 * Given a List of {@link CraftGuideRecipe}s, returns a
	 * modified list. May either return the provided list, or
	 * create a new List object. If a new List is created, it
	 * must allow element removal from its iterator.
	 * @param source
	 * @param recipeType
	 * @return
	 */
	public List<CraftGuideRecipe> filterRecipes(List<CraftGuideRecipe> source, ItemStack recipeType);
}
