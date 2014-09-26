package uristqwerty.CraftGuide;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import uristqwerty.CraftGuide.api.CraftGuideRecipe;
import uristqwerty.CraftGuide.api.CraftGuideRecipeExtra1;
import uristqwerty.CraftGuide.api.ItemFilter;
import uristqwerty.CraftGuide.api.ItemSlot;
import uristqwerty.CraftGuide.api.Renderer;
import uristqwerty.CraftGuide.api.Slot;
import uristqwerty.CraftGuide.api.SlotType;
import uristqwerty.CraftGuide.api.Util;
import uristqwerty.CraftGuide.client.ui.GuiRenderer;
import uristqwerty.gui_craftguide.rendering.Renderable;

public class Recipe implements CraftGuideRecipe, CraftGuideRecipeExtra1
{
	protected Slot[] slots;
	protected Object[] recipe;
	private Renderable background;
	private Renderable backgroundSelected;

	private int width = 79, height = 58;

	public Recipe(Slot[] slots, Object[] items, Renderable background, Renderable backgroundSelected)
	{
		this.slots = slots;
		this.recipe = new Object[items.length];
		for(int i = 0; i < items.length; i++)
		{
			this.recipe[i] = items[i];
		}

		this.background = background;
		this.backgroundSelected = backgroundSelected;

		for(int i = 0; i < slots.length; i++)
		{
			ItemStack display = displayStack(recipe[i]);
			if(this.recipe[i] != null && slots[i] instanceof ItemSlot && !((ItemSlot)slots[i]).drawQuantity && display != null && display.stackSize > 1)
			{
				ItemStack old = display;

				this.recipe[i] = new ItemStack(old.getItem(), 1, CommonUtilities.getItemDamage(old));
			}
		}
	}

	private ItemStack displayStack(Object item)
	{
		if(item == null)
		{
			return null;
		}
		else if(item instanceof ItemStack)
		{
			return (ItemStack)item;
		}
		else if(item instanceof ArrayList)
		{
			if(((ArrayList)item).size() < 1)
			{
				return null;
			}
			else
			{
				return displayStack(((ArrayList)item).get(0));
			}
		}
		else
		{
			return null;
		}
	}

	@Override
	public void draw(Renderer renderer, int x, int y, boolean mouseOverRecipe, int mouseX, int mouseY)
	{
		if(mouseOverRecipe)
		{
			backgroundSelected.render((GuiRenderer)renderer, x, y);
		}
		else
		{
			background.render((GuiRenderer)renderer, x, y);
		}

		for(int i = 0; i < slots.length; i++)
		{
			if(mouseOverRecipe)
			{
				boolean mouseOverSlot = slots[i].isPointInBounds(mouseX, mouseY, recipe, i);
				slots[i].draw(renderer, x, y, recipe, i, mouseOverSlot);
			}
			else
			{
				slots[i].draw(renderer, x, y, recipe, i, false);
			}
		}
	}

	public int getSlotIndexUnderMouse(int x, int y)
	{
		for(int i = 0; i < slots.length; i++)
		{
			if(slots[i].isPointInBounds(x, y, recipe, i))
			{
				return i;
			}
		}

		return -1;
	}

	@Override
	public boolean containsItem(ItemStack stack)
	{
		ItemFilter filter = Util.instance.getCommonFilter(stack);

		return containsItem(filter);
	}

	@Override
	public boolean containsItem(ItemFilter filter)
	{
		return containsItem(filter, SlotType.ANY_SLOT);
	}

	@Override
	public boolean containsItem(ItemStack stack, SlotType type)
	{
		ItemFilter filter = Util.instance.getCommonFilter(stack);

		return containsItem(filter, type);
	}

	@Override
	public boolean containsItem(ItemFilter filter, SlotType type)
	{
		if(filter != null)
		{
			for(int i = 0; i < slots.length; i++)
			{
				if(slots[i].matches(filter, recipe, i, type))
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public Object[] getItems()
	{
		return recipe;
	}

	@Override
	public int width()
	{
		return width;
	}

	@Override
	public int height()
	{
		return height;
	}

	public Recipe setSize(int width, int height)
	{
		this.width = width;
		this.height = height;

		return this;
	}

	@Override
	public List<String> getItemText(int x, int y)
	{
		int slot = getSlotIndexUnderMouse(x, y);

		if(slot == -1 || slots[slot] == null)
		{
			return null;
		}
		else
		{
			return slots[slot].getTooltip(x, y, recipe, slot);
		}
	}

	@Override
	public ItemFilter getRecipeClickedResult(int x, int y)
	{
		int slot = getSlotIndexUnderMouse(x, y);

		if(slot == -1 || slots[slot] == null)
		{
			return null;
		}
		else
		{
			return slots[slot].getClickedFilter(x, y, recipe, slot);
		}
	}
}
