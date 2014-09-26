package uristqwerty.CraftGuide.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

public class InfoSlot extends ItemSlot
{
	private final ItemStack displayed;
	private final String[] format;
	private boolean clickable;
	private boolean searchable;

	public InfoSlot(int x, int y, ItemStack displayedStack, String[] lines)
	{
		super(x, y, 16, 16, true);
		displayed = displayedStack;
		format = lines;
	}

	@Override
	public void draw(Renderer renderer, int x, int y, Object[] data, int dataIndex, boolean isMouseOver)
	{
		ItemSlot.implementation.draw(this, renderer, x, y, displayed, isMouseOver && clickable);
	}

	@Override
	public ItemFilter getClickedFilter(int x, int y, Object[] data, int dataIndex)
	{
		return clickable? null : ItemSlot.implementation.getClickedFilter(x, y, displayed);
	}

	@Override
	public List<String> getTooltip(int x, int y, Object[] data, int dataIndex)
	{
		List<String> tooltip = new ArrayList<String>();

		if(data[dataIndex] instanceof Object[])
		{
			for(int i = 0; i < format.length; i++)
			{
				tooltip.add(String.format(format[i], (Object[])data[dataIndex]));
			}
		}
		else
		{
			for(int i = 0; i < format.length; i++)
			{
				tooltip.add(String.format(format[i], data[dataIndex]));
			}
		}

		return tooltip;
	}

	@Override
	public boolean matches(ItemFilter filter, Object[] data, int dataIndex, SlotType type)
	{
		return searchable && ItemSlot.implementation.matches(this, filter, displayed, type);
	}

	public InfoSlot setSeachable(boolean searchable)
	{
		this.searchable = searchable;
		return this;
	}

	public InfoSlot setClickable(boolean clickable)
	{
		this.clickable = clickable;
		return this;
	}
}
