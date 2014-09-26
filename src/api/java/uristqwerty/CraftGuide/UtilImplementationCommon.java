package uristqwerty.CraftGuide;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import uristqwerty.CraftGuide.api.ItemFilter;
import uristqwerty.CraftGuide.api.Util;
import uristqwerty.CraftGuide.client.ui.GuiRenderer;

public abstract class UtilImplementationCommon extends Util
{
	public float partialTicks;

	@Override
	public ItemFilter getCommonFilter(Object stack)
	{
		if(stack == null)
		{
			return null;
		}
		else if(stack instanceof ItemStack)
		{
			return new SingleItemFilter((ItemStack)stack);
		}
		else if(stack instanceof List && ((List)stack).size() > 0)
		{
			return new MultipleItemFilter((List)stack);
		}
		else if(stack instanceof String)
		{
			return new StringItemFilter((String)stack);
		}
		else
		{
			return null;
		}
	}

	@Override
	public List<String> getItemStackText(ItemStack stack)
	{
		try
		{
			List list = ((GuiRenderer)GuiRenderer.instance).getItemNameandInformation(stack);

			if(CommonUtilities.getItemDamage(stack) == CraftGuide.DAMAGE_WILDCARD && (list.size() < 1 || (list.size() == 1 && (list.get(0) == null || (list.get(0) instanceof String && ((String)list.get(0)).isEmpty())))))
			{
				list = ((GuiRenderer)GuiRenderer.instance).getItemNameandInformation(GuiRenderer.fixedItemStack(stack));
			}

			List<String> text = new ArrayList<String>(list.size());
			boolean first = true;

			for(Object o: list)
			{
				if(o instanceof String)
				{
					if(first)
					{
						EnumRarity rarity = null;

						try
						{
							rarity = stack.getRarity();
						}
						catch(NullPointerException e)
						{
						}

						if(rarity == null)
						{
							rarity = EnumRarity.common;
						}

						text.add(rarity.rarityColor + (String)o);

						if(CraftGuide.alwaysShowID)
						{
							text.add(EnumChatFormatting.DARK_GRAY + "ID: " + Item.itemRegistry.getNameForObject(stack.getItem()) + "; data: " + CommonUtilities.getItemDamage(stack));
						}

						first = false;
					}
					else
					{
						text.add(EnumChatFormatting.DARK_GRAY + (String)o);
					}
				}
			}

			return text;
		}
		catch(Exception e)
		{
			CraftGuideLog.log(e);

			List<String> text = new ArrayList<String>(1);
			text.add(EnumChatFormatting.YELLOW + "Item " + Item.itemRegistry.getNameForObject(stack.getItem()) + " data " + Integer.toString(CommonUtilities.getItemDamage(stack)));
			return text;
		}
	}

	@Override
	public void reloadRecipes()
	{
		CraftGuide.side.reloadRecipes();
	}

	@Override
	public float getPartialTicks()
	{
		return partialTicks;
	}
}
