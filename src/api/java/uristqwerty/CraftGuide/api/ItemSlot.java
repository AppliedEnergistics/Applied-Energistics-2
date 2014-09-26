package uristqwerty.CraftGuide.api;

import java.util.List;


/**
 * When a recipe is rendered, the ItemSlots provided to the template are
 * used to determine the layout of the recipe's items.
 */
public class ItemSlot implements Slot
{
	/**
	 * This slot's size and location relative to the containing
	 * recipe's top left corner
	 */
	public int x, y, width, height;
	
	/**
	 * Used by {@link ItemSlotImplementation#draw} to decide
	 * whether to draw a background before drawing the actual
	 * slot contents.
	 */
	public boolean drawBackground;
	
	/**
	 * A sneaky field that is read by the default recipe implementation
	 * when an instance is created. If an ItemStack with a quantity
	 * greater than one would match up to an ItemSlot where
	 * drawQuantity equals false, it replaces the stack with one
	 * with a size of 1. This is only provided for convenience, it
	 * is entirely possibly to implement the same logic if it
	 * is needed but unavailable, such as with a custom recipe,
	 * or an implementation of Slot that is not an ItemSlot.
	 * <br><br>
	 * The actual effect of this field may change in the future
	 * (for example, copying only part of the method that Minecraft
	 * uses to draw the item overlay, making the part that draws the
	 * stack size conditional), but for the moment, I'm assuming that
	 * doing it this way will be more compatible if Minecraft changes
	 * its implementation in the future, or if another mod edits
	 * {@link net.minecraft.src.RenderItem#renderItemOverlayIntoGUI}.
	 */
	public boolean drawQuantity;
	
	/**
	 * Used by {@link ItemSlotImplementation#matches} to
	 * restrict what sort of searches can match this slot
	 */
	public SlotType slotType = SlotType.INPUT_SLOT;
	
	/**
	 * Implementation of ItemSlot functionality, allowing it
	 * to change if needed, without altering the API. This
	 * field is set during CraftGuide's {@literal @PreInit}.
	 */
	public static ItemSlotImplementation implementation;

	/**
	 * Creates a new ItemSlot. Same as
	 * {@link #ItemSlot(int, int, int, int, boolean)}, with
	 * false as the last parameter.
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public ItemSlot(int x, int y, int width, int height)
	{
		this(x, y, width, height, false);
	}
	
	/**
	 * Creates a new ItemSlot
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param drawQuantity
	 */
	public ItemSlot(int x, int y, int width, int height, boolean drawQuantity)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.drawQuantity = drawQuantity;
	}

	@Override
	public void draw(Renderer renderer, int x, int y, Object[] data, int dataIndex, boolean isMouseOver)
	{
		implementation.draw(this, renderer, x, y, data[dataIndex], isMouseOver);
	}

	@Override
	public List<String> getTooltip(int x, int y, Object[] data, int dataIndex)
	{
		return implementation.getTooltip(this, data[dataIndex]);
	}

	@Override
	public boolean matches(ItemFilter search, Object[] data, int dataIndex, SlotType type)
	{
		return implementation.matches(this, search, data[dataIndex], type);
	}

	@Override
	public boolean isPointInBounds(int x, int y, Object[] data, int dataIndex)
	{
		return implementation.isPointInBounds(this, x, y);
	}

	@Override
	public ItemFilter getClickedFilter(int x, int y, Object[] data, int dataIndex)
	{
		return implementation.getClickedFilter(x, y, data[dataIndex]);
	}
	
	/**
	 * Sets whether or not this ItemSlot draws a background square
	 * during its draw method.
	 * @param draw
	 * @return this, to permit method chaining
	 */
	public ItemSlot drawOwnBackground(boolean draw)
	{
		drawBackground = draw;
		return this;
	}

	/**
	 * Sets whether or not this ItemSlot draws a background square
	 * during its draw method. Same as {@link #drawOwnBackground()},
	 * but uses the default value of true.
	 * @return this, to permit method chaining
	 */
	public ItemSlot drawOwnBackground()
	{
		return drawOwnBackground(true);
	}
	
	/**
	 * Sets the {@link SlotType} associated with this ItemSlot.
	 * The SlotType is used for searches, both from in-game, and
	 * from the use of the API
	 * @param type
	 * @return this ItemSlot, to permit method chaining
	 */
	public ItemSlot setSlotType(SlotType type)
	{
		slotType = type;
		return this;
	}
}