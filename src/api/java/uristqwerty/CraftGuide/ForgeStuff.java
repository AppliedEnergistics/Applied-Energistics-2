package uristqwerty.CraftGuide;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import uristqwerty.CraftGuide.RecipeGeneratorImplementation.RecipeGeneratorForgeExtension;

public class ForgeStuff implements RecipeGeneratorForgeExtension
{
	@Override
	public boolean matchesType(IRecipe recipe)
	{
		return recipe instanceof ShapedOreRecipe || recipe instanceof ShapelessOreRecipe;
	}

	@Override
	public boolean isShapelessRecipe(IRecipe recipe)
	{
		return recipe instanceof ShapelessOreRecipe;
	}

	@Override
	public Object[] getCraftingRecipe(RecipeGeneratorImplementation gen, IRecipe recipe, boolean allowSmallGrid)
	{
		try
		{
			if(recipe instanceof ShapedOreRecipe)
			{
				int width = (Integer)CommonUtilities.getPrivateValue(ShapedOreRecipe.class, (ShapedOreRecipe)recipe, "width");
				int height = (Integer)CommonUtilities.getPrivateValue(ShapedOreRecipe.class, (ShapedOreRecipe)recipe, "height");
				Object[] items = (Object[])CommonUtilities.getPrivateValue(ShapedOreRecipe.class, (ShapedOreRecipe)recipe, "input");

				if(allowSmallGrid && width < 3 && height < 3)
				{
					return gen.getSmallShapedRecipe(width, height, items, ((ShapedOreRecipe)recipe).getRecipeOutput());
				}
				else
				{
					return gen.getCraftingShapedRecipe(width, height, items, ((ShapedOreRecipe)recipe).getRecipeOutput());
				}
			}
			else if(recipe instanceof ShapelessOreRecipe)
			{
				List items = (List)CommonUtilities.getPrivateValue(ShapelessOreRecipe.class, (ShapelessOreRecipe)recipe, "input");
				return gen.getCraftingShapelessRecipe(items, ((ShapelessOreRecipe)recipe).getRecipeOutput());
			}
		}
		catch(SecurityException e)
		{
			CraftGuideLog.log(e);
		}
		catch(NoSuchFieldException e)
		{
			CraftGuideLog.log(e);
		}
		catch(IllegalArgumentException e)
		{
			CraftGuideLog.log(e);
		}
		catch(IllegalAccessException e)
		{
			CraftGuideLog.log(e);
		}

		return null;
	}

	private IdentityHashMap<List, String> mappingCache = new IdentityHashMap<List, String>();

	@Override
	public List<String> emptyOreDictEntryText(List oreDictionaryList)
	{
		if(!mappingCache.containsKey(oreDictionaryList))
		{
			mappingCache.put(oreDictionaryList, getOreDictName(oreDictionaryList));
		}

		String name = mappingCache.get(oreDictionaryList);

		if(name == null)
		{
			return null;
		}
		else
		{
			List<String> text = new ArrayList<String>(1);
			text.add("0 items for Ore Dictionary name '" + name + "'");
			return text;
		}
	}

	private String getOreDictName(List list)
	{
		try
		{
			Field oreStacks = OreDictionary.class.getDeclaredField("oreStacks");
			oreStacks.setAccessible(true);
			HashMap<Integer, ArrayList<ItemStack>> map = (HashMap<Integer, ArrayList<ItemStack>>)oreStacks.get(null);
			Integer integer = null;

			for(Entry<Integer, ArrayList<ItemStack>> entry: map.entrySet())
			{
				if(entry.getValue() == list)
				{
					integer = entry.getKey();
					break;
				}
			}

			if(integer != null)
			{
				return OreDictionary.getOreName(integer);
			}
		}
		catch(NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		catch(SecurityException e)
		{
			e.printStackTrace();
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch(IllegalAccessException e)
		{
			e.printStackTrace();
		}

		return null;
	}
}
