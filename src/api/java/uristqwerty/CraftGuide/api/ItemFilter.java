package uristqwerty.CraftGuide.api;

import java.util.List;


/**
 * 
 */
public interface ItemFilter
{
	/**
	 * Compares this filter with an Object. Common types include an
	 * ItemStack, a List of ItemStacks, or a String.
	 * <br><br>
	 * If the object type is unknown, return false.
	 * @param item
	 * @return
	 */
	public boolean matches(Object item);
	
	/**
	 * Draw a visual representation of this filter at (x, y), within a
	 * 16 by 16 rectangle. May extend out of that rectangle, although it
	 * might not look very good.
	 * @param renderer
	 * @param x
	 * @param y
	 */
	public void draw(Renderer renderer, int x, int y);
	
	/**
	 * Gets a description of the filter, displayed when the user moves
	 * their cursor over the filter display.
	 * @return a List of lines of text
	 */
	public List<String> getTooltip();
}
