package uristqwerty.CraftGuide;

import java.util.List;

import net.minecraft.item.ItemStack;
import uristqwerty.CraftGuide.api.ItemFilter;
import uristqwerty.CraftGuide.api.ItemSlot;
import uristqwerty.CraftGuide.api.ItemSlotImplementation;
import uristqwerty.CraftGuide.api.NamedTexture;
import uristqwerty.CraftGuide.api.Renderer;
import uristqwerty.CraftGuide.api.SlotType;
import uristqwerty.CraftGuide.api.Util;

/**
 * It's a rather silly name, but since it's only directly used in one other class...
 */
public class ItemSlotImplementationImplementation implements ItemSlotImplementation
{
	private NamedTexture overlayAny;
	private NamedTexture overlayForge;
	private NamedTexture overlayForgeSingle;
	private NamedTexture background;

	public ItemSlotImplementationImplementation()
	{
		overlayAny = Util.instance.getTexture("ItemStack-Any");
		overlayForge = Util.instance.getTexture("ItemStack-OreDict");
		overlayForgeSingle = Util.instance.getTexture("ItemStack-OreDict-Single");
		background = Util.instance.getTexture("ItemStack-Background");
	}

	@Override
	public List<String> getTooltip(ItemSlot itemSlot, Object data)
	{
		ItemStack stack = item(data);

		if(stack == null)
		{
			if(data instanceof List && ((List)data).size() < 1)
			{
				return emptyOreDictEntryText((List)data);
			}
			else
			{
				return null;
			}
		}
		else
		{
			return CommonUtilities.getExtendedItemStackText(data);
		}
	}

	@Override
	public void draw(ItemSlot itemSlot, Renderer renderer, int recipeX, int recipeY, Object data, boolean isMouseOver)
	{
		int x = recipeX + itemSlot.x;
		int y = recipeY + itemSlot.y;
		ItemStack stack = item(data);

		if(itemSlot.drawBackground)
		{
			renderer.renderRect(x - 1, y - 1, 18, 18, background);
		}

		if(stack != null)
		{
			renderer.renderItemStack(x, y, stack);

			if(isMouseOver)
			{
				renderer.renderRect(x, y, 16, 16, 0xff, 0xff, 0xff, 0x80);
			}

			if(CommonUtilities.getItemDamage(stack) == CraftGuide.DAMAGE_WILDCARD)
			{
				renderer.renderRect(x - 1, y - 1, 18, 18, overlayAny);
			}

			if(data instanceof List)
			{
				if(((List)data).size() > 1)
				{
					renderer.renderRect(x - 1, y - 1, 18, 18, overlayForge);
				}
				else
				{
					renderer.renderRect(x - 1, y - 1, 18, 18, overlayForgeSingle);
				}
			}
		}
		else if(data instanceof List && ((List)data).size() < 1)
		{
			renderer.renderRect(x - 1, y - 1, 18, 18, overlayForge);
		}
	}

	private static ItemStack item(Object data)
	{
		if(data == null)
		{
			return null;
		}
		else if(data instanceof ItemStack)
		{
			return (ItemStack)data;
		}
		else if(data instanceof List && ((List)data).size() > 0)
		{
			return item(((List)data).get(0));
		}

		return null;
	}

	@Override
	public boolean matches(ItemSlot itemSlot, ItemFilter search, Object data, SlotType type)
	{
		if(type != itemSlot.slotType && (
				type != SlotType.ANY_SLOT ||
				itemSlot.slotType == SlotType.DISPLAY_SLOT ||
				itemSlot.slotType == SlotType.HIDDEN_SLOT))
		{
			return false;
		}

		if(search == null)
		{
			return false;
		}
		else if(data == null || data instanceof ItemStack)
		{
			return search.matches(data);
		}
		else if(data instanceof List)
		{
			for(Object content: (List)data)
			{
				if(search.matches(content))
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean isPointInBounds(ItemSlot itemSlot, int x, int y)
	{
		return x >= itemSlot.x
			&& x < itemSlot.x + itemSlot.width
			&& y >= itemSlot.y
			&& y < itemSlot.y + itemSlot.height;
	}

	@Override
	public ItemFilter getClickedFilter(int x, int y, Object object)
	{
		return Util.instance.getCommonFilter(object);
	}

	private List<String> emptyOreDictEntryText(List oreDictionaryList)
	{
		if(RecipeGeneratorImplementation.forgeExt != null)
		{
			return RecipeGeneratorImplementation.forgeExt.emptyOreDictEntryText(oreDictionaryList);
		}
		else
		{
			return null;
		}
	}
}
