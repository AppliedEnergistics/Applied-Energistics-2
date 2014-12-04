package appeng.api.features;

import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Lets you manipulate Grinder Recipes, by adding or editing existing ones.
 */
public interface IGrinderRegistry
{

	/**
	 * Current list of registered recipes, you can modify this if you want too.
	 * 
	 * @return currentlyRegisteredRecipes
	 */
	public List<IGrinderEntry> getRecipes();

	/**
	 * add a new recipe the easy way, in &#8594; out, how many turns., duplicates will not be added.
	 * 
	 * @param in input
	 * @param out output
	 * @param turns amount of turns to turn the input into the output
	 */
	public void addRecipe(ItemStack in, ItemStack out, int turns);

	/**
	 * add a new recipe with optional outputs, duplicates will not be added.
	 * 
	 * @param in input
	 * @param out output
	 * @param optional optional output
	 * @param chance chance to get the optional output within 0.0 - 1.0
	 * @param turns amount of turns to turn the input into the outputs
	 */
	void addRecipe(ItemStack in, ItemStack out, ItemStack optional, float chance, int turns);

	/**
	 * add a new recipe with optional outputs, duplicates will not be added.
	 * 
	 * @param in input
	 * @param out output
	 * @param optional optional output
	 * @param chance chance to get the optional output within 0.0 - 1.0
	 * @param optional2 second optional output
	 * @param chance2 chance to get the second optional output within 0.0 - 1.0
	 * @param turns amount of turns to turn the input into the outputs
	 */
	void addRecipe(ItemStack in, ItemStack out, ItemStack optional, float chance, ItemStack optional2, float chance2, int turns);

	/**
	 * Searches for a recipe for a given input, and returns it.
	 * 
	 * @param input input
	 * @return identified recipe or null
	 */
	public IGrinderEntry getRecipeForInput(ItemStack input);

}
