package uristqwerty.CraftGuide.api;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

/**
 * This interface contains methods that can be used to provide CraftGuide with
 * crafting recipes.
 * <br><br>
 * To do so, use one of the methods returning an {@link RecipeTemplate}
 * to create a template from a list of {@link ItemSlot}s and optionally an
 * ItemStack representing the recipe type (generally the block/item used to
 * craft the recipe, if not provided, defaults to the workbench).
 * <br><br>
 * With the template, call addRecipe for each recipe, passing an ItemStack[]
 * corresponding to the ItemSlot[] provided when creating the template.
 */

public interface RecipeGenerator
{
	/**
	 * Creates a {@link RecipeTemplate} for the provided ISlot[],
	 * associated with the provided crafting type. Create a default
	 * background based on the size of the template.
	 * @param slots
	 * @param craftingType
	 * @return
	 */
	public RecipeTemplate createRecipeTemplate(Slot[] slots, ItemStack craftingType);

	/**
	 * Creates a {@link RecipeTemplate} for the provided ISlot[],
	 * associated with the provided crafting type. The background is
	 * a rectangle taken out of the supplied texture file, at the
	 * coordinate pairs given.
	 * @param slots
	 * @param craftingType
	 * @param backgroundTexture
	 * @param backgroundX
	 * @param backgroundY
	 * @param backgroundSelectedX
	 * @param backgroundSelectedY
	 * @return
	 */

	public RecipeTemplate createRecipeTemplate(Slot[] slots, ItemStack craftingType, String backgroundTexture, int backgroundX, int backgroundY, int backgroundSelectedX, int backgroundSelectedY);
	/**
	 * Creates a {@link RecipeTemplate} for the provided ISlot[],
	 * associated with the provided crafting type. The background is
	 * a rectangle taken out of the supplied texture file, at the
	 * coordinate pairs given.
	 * @param slots
	 * @param craftingType
	 * @param backgroundTexture
	 * @param backgroundX
	 * @param backgroundY
	 * @param backgroundSelectedTexture
	 * @param backgroundSelectedX
	 * @param backgroundSelectedY
	 * @return
	 */
	public RecipeTemplate createRecipeTemplate(Slot[] slots, ItemStack craftingType, String backgroundTexture, int backgroundX, int backgroundY, String backgroundSelectedTexture, int backgroundSelectedX, int backgroundSelectedY);

	/**
	 * Generates a recipe from the template and Object[] passed to
	 * this method, and then adds it to CraftGuide to display in
	 * game.
	 * @param template
	 * @param crafting
	 */
	public void addRecipe(RecipeTemplate template, Object[] crafting);

	/**
	 * If you have your own recipe implementation, you can use this
	 * method to add recipes without needing a RecipeTemplate.
	 * @param recipe
	 * @param craftingType
	 */
	public void addRecipe(CraftGuideRecipe recipe, ItemStack craftingType);

	/**
	 * Sets whether a certain type of recipe is initially visible.
	 * <br><br>
	 * Useful for hiding recipe types that have a lot of recipes, but
	 * few people need, so they would normally just add excessive
	 * clutter to the recipe list.
	 * @param type an ItemStack associated with the recipe type
	 * @param visible whether it is initially visible or not
	 */
	public void setDefaultTypeVisibility(ItemStack type, boolean visible);

	/**
	 * Takes an IRecipe, and returns an array representing a
	 * 3x3 crafting grid, plus a single output, for that recipe's
	 * contents. Each element is either null, an ItemStack, or a
	 * List containing zero or more ItemStacks (for Forge ore
	 * dictionary recipes).
	 * <br><br>
	 * May return null if given an IRecipe implementation that it
	 * cannot interpret.
	 * @see #getCraftingRecipe(IRecipe, boolean)
	 * @param recipe the IRecipe to be read
	 * @return an Object[10], where the first nine elements form
	 * the 3x3 input grid, and the last element is the recipe output.
	 */
	public Object[] getCraftingRecipe(IRecipe recipe);

	/**
	 * Takes an IRecipe, and returns an array representing a
	 * 3x3 or 2x2 crafting grid, plus a single output.
	 * <br><br>
	 * See {@link #getCraftingRecipe(IRecipe)} for details.
	 * @see #getCraftingRecipe(IRecipe)
	 * @param recipe
	 * @param allowSmallGrid	if true, may return an Object[5] if
	 * 		the recipe fits in a 2x2 grid.
	 * @return an Object[10] or an Object[5]
	 */
	public Object[] getCraftingRecipe(IRecipe recipe, boolean allowSmallGrid);
}
