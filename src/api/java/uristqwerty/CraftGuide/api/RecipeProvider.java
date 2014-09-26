package uristqwerty.CraftGuide.api;

/**
 * When CraftGuide (re)loads its recipe list, every Object that implements this
 * interface, and has been registered with CraftGuide, will have its generateRecipes
 * method called.
 */
public interface RecipeProvider
{
	/**
	 * Called by CraftGuide when it is (re)populating its recipe list, as a request
	 * for the implementing Object to provide recipes through the IRecipeGenerator
	 * instance passed to it.
	 * @param generator
	 * @see RecipeGenerator
	 */
	void generateRecipes(RecipeGenerator generator);
}
