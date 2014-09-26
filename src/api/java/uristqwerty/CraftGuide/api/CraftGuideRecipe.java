package uristqwerty.CraftGuide.api;

import java.util.List;

import net.minecraft.item.ItemStack;

/**
 * An instance of a single recipe to be displayed by CraftGuide.
 * <br><br>
 * Contains a number of methods related to searching, drawing,
 * and interaction with the user.
 * <br><br>
 * It is possible to implement your own recipe, if the default
 * functionality is not adequate, although you won't have access
 * to any of the default implementation, so it may require a bit
 * of work.
 */
public interface CraftGuideRecipe
{
	/**
	 * Checks whether a certain item is contained in this recipe.
	 * <br><br>
	 * This method is provided as a convenience for anyone writing a
	 * RecipeFilter; {@link #containsItem(ItemFilter)} will generally
	 * be used elsewhere.
	 * @param filter
	 * @return
	 */
	public boolean containsItem(ItemStack filter);

	/**
	 * Checks whether this recipe contains an item that matches the
	 * supplied ItemFilter. Implementations of RecipeFilter may either
	 * use this or {@link #containsItem(ItemFilter)}; CraftGuide itself
	 * always uses this.
	 * @param filter
	 * @return
	 */
	public boolean containsItem(ItemFilter filter);

	/**
	 * When a mouse click occurs within the rectangle occupied by this recipe,
	 * this method is used to determine if the current ItemFilter should be
	 * changed as a result. Generally involves iterating over each Slot within
	 * the recipe, and, if any of them is at the coordinates, returning
	 * {@link Slot#getClickedFilter}.
	 * @param x coordinate, relative to the top left of this recipe
	 * @param y coordinate, relative to the top left of this recipe
	 * @return a new ItemFilter, or null to keep the current one
	 */
	public ItemFilter getRecipeClickedResult(int x, int y);

	/**
	 * Draws this recipe at the specified screen coordinates. If isMouseOver
	 * is true, mouseX and moyseY are the coordinates of the user's curosr,
	 * relative to the top left of this recipe; otherwise they are undefined,
	 * and may be anything, depending on the implementation of CraftGuide,
	 * and may change between CraftGuide versions.
	 * @param renderer
	 * @param x
	 * @param y
	 * @param isMouseOver
	 * @param mouseX
	 * @param mouseY
	 */
	public void draw(Renderer renderer, int x, int y, boolean isMouseOver, int mouseX, int mouseY);

	/**
	 * Called to get the text to display when the user moves their cursor
	 * over this recipe. Generally, and in the default recipe implementation,
	 * this means checking if the cursor is over a Slot, and if so,
	 * returning whatever the slot returns for {@link Slot#getTooltip}
	 * @param mouseX
	 * @param mouseY
	 * @return a List of Strings, or null
	 */
	public List<String> getItemText(int mouseX, int mouseY);

	/**
	 * Gets an array of Objects, representing the contents of this recipe.
	 * <br><br>
	 * Currently used by craftGuide to create a list of every type of item
	 * that is used (CraftGuide does not have any logic for finding items
	 * on its own). Some recipe filters may use this, although
	 * {@link containsItem} is preferred wherever possible.
	 * @return
	 */
	public Object[] getItems();

	/**
	 * Gets the width of this recipe; used to determine grid spacing and
	 * if the cursor is currently positioned over this recipe
	 * @return
	 */
	public int width();

	/**
	 * Gets the height of this recipe; used to determine grid spacing and
	 * if the cursor is currently positioned over this recipe
	 * @return
	 */
	public int height();
}
