package uristqwerty.CraftGuide;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import uristqwerty.CraftGuide.api.ItemFilter;
import uristqwerty.CraftGuide.api.NamedTexture;
import uristqwerty.CraftGuide.api.Renderer;
import uristqwerty.CraftGuide.api.Util;

public class StringItemFilter implements ItemFilter
{
	private String comparison;
	private NamedTexture textImage = Util.instance.getTexture("TextFilter");

	public StringItemFilter(String string)
	{
		comparison = string.toLowerCase();
	}

	@Override
	public boolean matches(Object item)
	{
		if(item instanceof ItemStack)
		{
			return CommonUtilities.searchExtendedItemStackText(item, comparison);
		}
		else if(item instanceof String)
		{
			return ((String)item).toLowerCase().contains(comparison);
		}
		else if(item instanceof List)
		{
			for(Object o: (List)item)
			{
				if(matches(o))
				{
					return true;
				}
			}

			return false;
		}
		else
		{
			return false;
		}
	}

	@Override
	public void draw(Renderer renderer, int x, int y)
	{
		renderer.renderRect(x, y, 16, 16, textImage);
	}

	@Override
	public List<String> getTooltip()
	{
		List<String> text = new ArrayList<String>(1);
		text.add("\u00a77Text search: '" + comparison + "'");
		return text;
	}
}
