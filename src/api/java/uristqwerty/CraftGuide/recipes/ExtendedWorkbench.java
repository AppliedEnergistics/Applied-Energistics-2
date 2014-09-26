package uristqwerty.CraftGuide.recipes;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import uristqwerty.CraftGuide.api.CraftGuideAPIObject;
import uristqwerty.CraftGuide.api.ItemSlot;
import uristqwerty.CraftGuide.api.RecipeGenerator;
import uristqwerty.CraftGuide.api.RecipeProvider;
import uristqwerty.CraftGuide.api.RecipeTemplate;
import uristqwerty.CraftGuide.api.Slot;
import uristqwerty.CraftGuide.api.SlotType;

public class ExtendedWorkbench extends CraftGuideAPIObject implements RecipeProvider
{
	private static ItemStack workbenchStack = new ItemStack(Blocks.crafting_table, 1, 1);

	@Override
	public void generateRecipes(RecipeGenerator generator)
	{
		try
		{
			Class craftingManagerClass = Class.forName("naruto1310.extendedWorkbench.ExtendedCraftingManager");
			Object craftingManager = craftingManagerClass.getMethod("getInstance").invoke(null);
			addRecipes(generator, (List<IRecipe>)craftingManagerClass.getMethod("getRecipeList").invoke(craftingManager));
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch(SecurityException e)
		{
			e.printStackTrace();
		}
		catch(IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch(InvocationTargetException e)
		{
			e.printStackTrace();
		}
		catch(NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(NoSuchFieldException e)
		{
			e.printStackTrace();
		}
	}

	private void addRecipes(RecipeGenerator generator, List<IRecipe> recipes) throws ClassNotFoundException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
	{
		Class extendedShapedRecipe = Class.forName("naruto1310.extendedWorkbench.ExtendedShapedRecipes");
		Field shapedWidth = extendedShapedRecipe.getDeclaredField("recipeWidth");
		Field shapedHeight = extendedShapedRecipe.getDeclaredField("recipeHeight");
		Field shapedItems = extendedShapedRecipe.getDeclaredField("recipeItems");
		shapedWidth.setAccessible(true);
		shapedHeight.setAccessible(true);
		shapedItems.setAccessible(true);

		Class extendedShapelessRecipe = Class.forName("naruto1310.extendedWorkbench.ExtendedShapelessRecipes");
		Field shapelessItems = extendedShapelessRecipe.getDeclaredField("recipeItems");
		shapelessItems.setAccessible(true);

		Slot[] slots = new Slot[] {
				new ItemSlot( 3,  3, 16, 16).drawOwnBackground(),
				new ItemSlot(21,  3, 16, 16).drawOwnBackground(),
				new ItemSlot(39,  3, 16, 16).drawOwnBackground(),
				new ItemSlot( 3, 21, 16, 16).drawOwnBackground(),
				new ItemSlot(21, 21, 16, 16).drawOwnBackground(),
				new ItemSlot(39, 21, 16, 16).drawOwnBackground(),
				new ItemSlot( 3, 39, 16, 16).drawOwnBackground(),
				new ItemSlot(21, 39, 16, 16).drawOwnBackground(),
				new ItemSlot(39, 39, 16, 16).drawOwnBackground(),
				new ItemSlot( 3, 57, 16, 16).drawOwnBackground(),
				new ItemSlot(21, 57, 16, 16).drawOwnBackground(),
				new ItemSlot(39, 57, 16, 16).drawOwnBackground(),
				new ItemSlot( 3, 75, 16, 16).drawOwnBackground(),
				new ItemSlot(21, 75, 16, 16).drawOwnBackground(),
				new ItemSlot(39, 75, 16, 16).drawOwnBackground(),
				new ItemSlot( 3, 93, 16, 16).drawOwnBackground(),
				new ItemSlot(21, 93, 16, 16).drawOwnBackground(),
				new ItemSlot(39, 93, 16, 16).drawOwnBackground(),
				new ItemSlot(59, 48, 16, 16, true).setSlotType(SlotType.OUTPUT_SLOT).drawOwnBackground(),
		};

		RecipeTemplate template = generator.createRecipeTemplate(slots, workbenchStack);
		template.setSize(79, 112);

		for(IRecipe recipe: recipes)
		{
			Object[] recipeContents = new Object[19];

			if(extendedShapedRecipe.isInstance(recipe))
			{
				ItemStack[] items = (ItemStack[])shapedItems.get(recipe);
				int height = Math.min(6, shapedHeight.getInt(recipe));
				int width = Math.min(3, shapedWidth.getInt(recipe));

				for(int i = 0; i < height; i++)
				{
					for(int j = 0; j < width; j++)
					{
						recipeContents[i * 3 + j] = items[i * width + j];
					}
				}
			}
			else if(extendedShapelessRecipe.isInstance(recipe))
			{
				List<ItemStack> items = (List<ItemStack>)shapelessItems.get(recipe);

				for(int i = 0; i < Math.min(18, items.size()); i++)
				{
					recipeContents[i] = items.get(i);
				}
			}
			else
			{
				continue;
			}

			recipeContents[18] = recipe.getRecipeOutput();

			generator.addRecipe(template, recipeContents);
		}
	}
}
