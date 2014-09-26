package uristqwerty.CraftGuide.api;

import java.util.List;


/**
 * An extension of ItemSlot that allows certain interactions
 * to be disabled, and uses fixed Object provided during
 * initialisation rather than the object matching this slot
 * in the recipe.
 * <br><br>
 * Unlike in the old API, there is no "can filter" option,
 * as that can be accomplished through {@link #setSlotType}
 * which is already present in ItemSlot.
 */
public class ExtraSlot extends ItemSlot
{
	public Object displayed;
	public boolean showName = false;
	public boolean canClick = false;
	
	public ExtraSlot(int x, int y, int width, int height, Object displayedItem)
	{
		super(x, y, width, height);
		displayed = displayedItem;
	}
	
	@Override
	public void draw(Renderer renderer, int x, int y, Object[] data, int dataIndex, boolean isMouseOver)
	{
		implementation.draw(this, renderer, x, y, displayed, canClick && isMouseOver);
	}
	
	@Override
	public List<String> getTooltip(int x, int y, Object[] data, int dataIndex)
	{
		if(showName)
		{
			return implementation.getTooltip(this, displayed);
		}
		else
		{
			return null;
		}
	}
	
	@Override
	public ItemFilter getClickedFilter(int x, int y, Object[] data, int dataIndex)
	{
		if(canClick)
		{
			return implementation.getClickedFilter(x, y, displayed);
		}
		else
		{
			return null;
		}
	}

	/**
	 * Sets whether this slot, if clicked, sets the current
	 * filter to the displayed Object, or doesn't do anything
	 * 
	 * @param clickable
	 * @return this ExtraSlot, to permit method chaining
	 */
	public ExtraSlot clickable(boolean clickable)
	{
		canClick = clickable;
		return this;
	}

	/**
	 * Sets whether this slot shows a tooltip on mouseover
	 * 
	 * @param showName
	 * @return this ExtraSlot, to permit method chaining
	 */
	public ExtraSlot showName(boolean showName)
	{
		this.showName = showName;
		return this;
	}

	/**
	 * A shortened version of {@link #clickable(boolean)}
	 * that passes true
	 * 
	 * @return this ExtraSlot, to permit method chaining
	 */
	public ExtraSlot clickable()
	{
		return clickable(true);
	}

	/**
	 * A shortened version of {@link #showName(boolean)}
	 * that passes true
	 * 
	 * @return this ExtraSlot, to permit method chaining
	 */
	public ExtraSlot showName()
	{
		return showName(true);
	}
}
