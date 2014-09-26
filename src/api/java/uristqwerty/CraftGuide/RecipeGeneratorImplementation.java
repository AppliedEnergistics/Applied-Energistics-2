package uristqwerty.CraftGuide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import uristqwerty.CraftGuide.api.CraftGuideRecipe;
import uristqwerty.CraftGuide.api.RecipeGenerator;
import uristqwerty.CraftGuide.api.RecipeTemplate;
import uristqwerty.CraftGuide.api.Slot;
import uristqwerty.gui_craftguide.minecraft.Image;
import uristqwerty.gui_craftguide.texture.BlankTexture;
import uristqwerty.gui_craftguide.texture.BorderedTexture;
import uristqwerty.gui_craftguide.texture.DynamicTexture;
import uristqwerty.gui_craftguide.texture.SubTexture;
import uristqwerty.gui_craftguide.texture.Texture;
import uristqwerty.gui_craftguide.texture.TextureClip;

public class RecipeGeneratorImplementation implements RecipeGenerator
{
	public static interface RecipeGeneratorForgeExtension
	{
		boolean matchesType(IRecipe recipe);
		boolean isShapelessRecipe(IRecipe recipe);
		Object[] getCraftingRecipe(RecipeGeneratorImplementation recipeGeneratorImplementation, IRecipe recipe, boolean allowSmallGrid);
		List<String> emptyOreDictEntryText(List oreDictionaryList);
	}

	private Map<ItemStack, List<CraftGuideRecipe>> recipes = new HashMap<ItemStack, List<CraftGuideRecipe>>();
	public List<ItemStack> disabledTypes = new LinkedList<ItemStack>();
	private Texture defaultBackground = new BlankTexture();
	private Texture defaultBackgroundSelected;
	public static ItemStack workbench = new ItemStack(Blocks.crafting_table);

	public static RecipeGeneratorForgeExtension forgeExt;

	public RecipeGeneratorImplementation()
	{
		Texture source = DynamicTexture.instance("base_image");
		defaultBackgroundSelected = new BorderedTexture(
				new Texture[]{
						new TextureClip(source, 117,  1,  2, 2),
						new SubTexture (source, 120,  1, 32, 2),
						new TextureClip(source, 153,  1,  2, 2),
						new SubTexture (source, 117,  4,  2, 32),
						new SubTexture (source, 120,  4, 32, 32),
						new SubTexture (source, 153,  4,  2, 32),
						new TextureClip(source, 117, 37,  2, 2),
						new SubTexture (source, 120, 37, 32, 2),
						new TextureClip(source, 153, 37,  2, 2),
				}, 2);
	}

	@Override
	public RecipeTemplate createRecipeTemplate(Slot[] slots,
			ItemStack craftingType, String backgroundTexture, int backgroundX,
			int backgroundY, int backgroundSelectedX, int backgroundSelectedY)
	{
		return createRecipeTemplate(slots, craftingType,
				backgroundTexture, backgroundX, backgroundY,
				backgroundTexture, backgroundSelectedX, backgroundSelectedY);
	}

	@Override
	public RecipeTemplate createRecipeTemplate(Slot[] slots, ItemStack craftingType,
			String backgroundTexture, int backgroundX, int backgroundY,
			String backgroundSelectedTexture, int backgroundSelectedX, int backgroundSelectedY)
	{
		if(craftingType == null)
		{
			craftingType = workbench;
		}

		if(backgroundTexture.equals("/gui/BrewGuide.png"))
		{
			backgroundTexture = "craftguide:textures/gui/BrewGuide.png";
		}
		else if(backgroundTexture.equals("/gui/CraftGuide.png"))
		{
			backgroundTexture = "craftguide:textures/gui/CraftGuide.png";
		}
		else if(backgroundTexture.equals("/gui/CraftGuideRecipe.png"))
		{
			backgroundTexture = "craftguide:textures/gui/CraftGuideRecipe.png";
		}

		if(backgroundSelectedTexture.equals("/gui/BrewGuide.png"))
		{
			backgroundSelectedTexture = "craftguide:textures/gui/BrewGuide.png";
		}
		else if(backgroundSelectedTexture.equals("/gui/CraftGuide.png"))
		{
			backgroundSelectedTexture = "craftguide:textures/gui/CraftGuide.png";
		}
		else if(backgroundSelectedTexture.equals("/gui/CraftGuideRecipe.png"))
		{
			backgroundSelectedTexture = "craftguide:textures/gui/CraftGuideRecipe.png";
		}

		for(ItemStack stack: recipes.keySet())
		{
			if(ItemStack.areItemStacksEqual(stack, craftingType))
			{
				craftingType = stack;
				break;
			}
		}

		return new DefaultRecipeTemplate(
				slots,
				craftingType,
				new TextureClip(
						Image.fromJar(backgroundTexture),
						backgroundX, backgroundY, 79, 58),
				new TextureClip(
						Image.fromJar(backgroundSelectedTexture),
						backgroundSelectedX, backgroundSelectedY, 79, 58));
	}

	@Override
	public RecipeTemplate createRecipeTemplate(Slot[] slots, ItemStack craftingType)
	{
		if(craftingType == null)
		{
			craftingType = workbench;
		}

		return new DefaultRecipeTemplate(slots, craftingType, defaultBackground, defaultBackgroundSelected);
	}

	@Override
	public void addRecipe(RecipeTemplate template, Object[] items)
	{
		addRecipe(template.generate(items), template.getCraftingType());
	}

	@Override
	public void addRecipe(CraftGuideRecipe recipe, ItemStack craftingType)
	{
		List<CraftGuideRecipe> recipeList = recipes.get(craftingType);

		if(recipeList == null)
		{
			recipeList = new ArrayList<CraftGuideRecipe>();
			recipes.put(craftingType, recipeList);
		}

		recipeList.add(recipe);
	}

	public Map<ItemStack, List<CraftGuideRecipe>> getRecipes()
	{
		return recipes;
	}

	public void clearRecipes()
	{
		recipes.clear();
	}

	@Override
	public void setDefaultTypeVisibility(ItemStack type, boolean visible)
	{
		if(visible)
		{
			disabledTypes.remove(type);
		}
		else if(!disabledTypes.contains(type))
		{
			disabledTypes.add(type);
		}
	}

	@Override
	public Object[] getCraftingRecipe(IRecipe recipe)
	{
		return getCraftingRecipe(recipe, false);
	}

	@Override
	public Object[] getCraftingRecipe(IRecipe recipe, boolean allowSmallGrid)
	{
		try
		{
			if(recipe instanceof ShapedRecipes)
			{
				int width = (Integer)CommonUtilities.getPrivateValue(ShapedRecipes.class, (ShapedRecipes)recipe, "b", "recipeWidth", "field_77576_b");
				int height = (Integer)CommonUtilities.getPrivateValue(ShapedRecipes.class, (ShapedRecipes)recipe, "c", "recipeHeight", "field_77577_c");
				Object[] items = (Object[])CommonUtilities.getPrivateValue(ShapedRecipes.class, (ShapedRecipes)recipe, "d", "recipeItems", "field_77574_d");

				if(allowSmallGrid && width < 3 && height < 3)
				{
					return getSmallShapedRecipe(width, height, items, ((ShapedRecipes)recipe).getRecipeOutput());
				}
				else
				{
					return getCraftingShapedRecipe(width, height, items, ((ShapedRecipes)recipe).getRecipeOutput());
				}
			}
			else if(recipe instanceof ShapelessRecipes)
			{
				List items = (List)CommonUtilities.getPrivateValue(ShapelessRecipes.class, (ShapelessRecipes)recipe, "b", "recipeItems", "field_77579_b");
				return getCraftingShapelessRecipe(items, ((ShapelessRecipes)recipe).getRecipeOutput());
			}
			else if(forgeExt != null && forgeExt.matchesType(recipe))
			{
				return forgeExt.getCraftingRecipe(this, recipe, allowSmallGrid);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			CraftGuideLog.log("Exception while trying to parse an ItemStack[10] from an IRecipe:");
			CraftGuideLog.log(e);
		}

		return null;
	}

	Object[] getSmallShapedRecipe(int width, int height, Object[] items, ItemStack recipeOutput)
	{
		Object[] output = new Object[5];

		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				output[y * 2 + x] = items[y * width + x];
			}
		}

		output[4] = recipeOutput;
		return output;
	}

	Object[] getCraftingShapelessRecipe(List items, ItemStack recipeOutput)
	{
		Object[] output = new Object[10];

		for(int i = 0; i < items.size(); i++)
		{
			output[i] = items.get(i);
		}

		output[9] = recipeOutput;
		return output;
	}

	Object[] getCraftingShapedRecipe(int width, int height, Object[] items, ItemStack recipeOutput)
	{
		Object[] output = new Object[10];

		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				output[y * 3 + x] = items[y * width + x];
			}
		}

		output[9] = recipeOutput;
		return output;
	}
}
