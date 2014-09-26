package uristqwerty.CraftGuide.api;

import java.util.List;


/**
 * A collections of methods implementing the functionality required
 * by ItemStack, without needing the code to be included as part of
 * the API.
 */
public interface ItemSlotImplementation
{
	/**
	 * Gets the tooltip for an ItemSlot
	 * @param itemSlot
	 * @param data
	 * @return
	 */
	public List<String> getTooltip(ItemSlot itemSlot, Object data);
	
	/**
	 * Draws an ItemSlot.
	 * @param itemSlot
	 * @param renderer
	 * @param x
	 * @param y
	 * @param data
	 * @param isMouseOver
	 */
	public void draw(ItemSlot itemSlot, Renderer renderer, int x, int y, Object data, boolean isMouseOver);
	
	/**
	 * Return an ItemFilter representing the ItemSlot contents.
	 * @param x
	 * @param y
	 * @param object
	 * @return
	 */
	public ItemFilter getClickedFilter(int x, int y, Object object);
	
	/**
	 * Compare the contents of an ItemSlot with an ItemFilter.
	 * @param itemSlot
	 * @param search
	 * @param object
	 * @param type
	 * @return
	 */
	public boolean matches(ItemSlot itemSlot, ItemFilter search, Object object, SlotType type);
	
	/**
	 * Does some simple math to test if a point, relative to the containing
	 * recipe's top left corner, is within the boundary rectangle of an
	 * ItemSlot.
	 * @param itemSlot
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isPointInBounds(ItemSlot itemSlot, int x, int y);
}
