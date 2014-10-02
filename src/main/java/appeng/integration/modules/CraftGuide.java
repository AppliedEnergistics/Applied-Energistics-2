package appeng.integration.modules;

import java.util.Arrays;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import uristqwerty.CraftGuide.CraftGuideLog;
import uristqwerty.CraftGuide.DefaultRecipeTemplate;
import uristqwerty.CraftGuide.RecipeGeneratorImplementation;
import uristqwerty.CraftGuide.api.CraftGuideAPIObject;
import uristqwerty.CraftGuide.api.CraftGuideRecipe;
import uristqwerty.CraftGuide.api.ItemSlot;
import uristqwerty.CraftGuide.api.RecipeGenerator;
import uristqwerty.CraftGuide.api.RecipeProvider;
import uristqwerty.CraftGuide.api.RecipeTemplate;
import uristqwerty.CraftGuide.api.Slot;
import uristqwerty.CraftGuide.api.SlotType;
import uristqwerty.CraftGuide.api.StackInfo;
import uristqwerty.CraftGuide.api.StackInfoSource;
import uristqwerty.gui_craftguide.texture.DynamicTexture;
import uristqwerty.gui_craftguide.texture.TextureClip;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.IIngredient;
import appeng.integration.IIntegrationModule;
import appeng.recipes.game.ShapedRecipe;
import appeng.recipes.game.ShapelessRecipe;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class CraftGuide extends CraftGuideAPIObject implements IIntegrationModule, RecipeProvider, StackInfoSource, RecipeGenerator
{

	public static CraftGuide instance;

	private final Slot[] shapelessCraftingSlots = new ItemSlot[] { new ItemSlot( 3, 3, 16, 16 ), new ItemSlot( 21, 3, 16, 16 ), new ItemSlot( 39, 3, 16, 16 ),
			new ItemSlot( 3, 21, 16, 16 ), new ItemSlot( 21, 21, 16, 16 ), new ItemSlot( 39, 21, 16, 16 ), new ItemSlot( 3, 39, 16, 16 ),
			new ItemSlot( 21, 39, 16, 16 ), new ItemSlot( 39, 39, 16, 16 ), new ItemSlot( 59, 21, 16, 16, true ).setSlotType( SlotType.OUTPUT_SLOT ), };

	private final Slot[] craftingSlotsOwnBackground = new ItemSlot[] { new ItemSlot( 3, 3, 16, 16 ).drawOwnBackground(),
			new ItemSlot( 21, 3, 16, 16 ).drawOwnBackground(), new ItemSlot( 39, 3, 16, 16 ).drawOwnBackground(),
			new ItemSlot( 3, 21, 16, 16 ).drawOwnBackground(), new ItemSlot( 21, 21, 16, 16 ).drawOwnBackground(),
			new ItemSlot( 39, 21, 16, 16 ).drawOwnBackground(), new ItemSlot( 3, 39, 16, 16 ).drawOwnBackground(),
			new ItemSlot( 21, 39, 16, 16 ).drawOwnBackground(), new ItemSlot( 39, 39, 16, 16 ).drawOwnBackground(),
			new ItemSlot( 59, 21, 16, 16, true ).setSlotType( SlotType.OUTPUT_SLOT ).drawOwnBackground(), };

	private final Slot[] smallCraftingSlotsOwnBackground = new ItemSlot[] { new ItemSlot( 12, 12, 16, 16 ).drawOwnBackground(),
			new ItemSlot( 30, 12, 16, 16 ).drawOwnBackground(), new ItemSlot( 12, 30, 16, 16 ).drawOwnBackground(),
			new ItemSlot( 30, 30, 16, 16 ).drawOwnBackground(), new ItemSlot( 59, 21, 16, 16, true ).setSlotType( SlotType.OUTPUT_SLOT ).drawOwnBackground(), };

	private final Slot[] craftingSlots = new ItemSlot[] { new ItemSlot( 3, 3, 16, 16 ), new ItemSlot( 21, 3, 16, 16 ), new ItemSlot( 39, 3, 16, 16 ),
			new ItemSlot( 3, 21, 16, 16 ), new ItemSlot( 21, 21, 16, 16 ), new ItemSlot( 39, 21, 16, 16 ), new ItemSlot( 3, 39, 16, 16 ),
			new ItemSlot( 21, 39, 16, 16 ), new ItemSlot( 39, 39, 16, 16 ), new ItemSlot( 59, 21, 16, 16, true ).setSlotType( SlotType.OUTPUT_SLOT ), };

	private final Slot[] smallCraftingSlots = new ItemSlot[] { new ItemSlot( 12, 12, 16, 16 ), new ItemSlot( 30, 12, 16, 16 ), new ItemSlot( 12, 30, 16, 16 ),
			new ItemSlot( 30, 30, 16, 16 ), new ItemSlot( 59, 21, 16, 16, true ).setSlotType( SlotType.OUTPUT_SLOT ), };

	private final Slot[] furnaceSlots = new ItemSlot[] { new ItemSlot( 13, 21, 16, 16 ),
			new ItemSlot( 50, 21, 16, 16, true ).setSlotType( SlotType.OUTPUT_SLOT ), };

	@Override
	public String getInfo(ItemStack itemStack)
	{
		// :P
		return null;
	}

	RecipeGenerator parent;

	@Override
	public void generateRecipes(RecipeGenerator generator)
	{
		parent = generator;

		RecipeTemplate craftingTemplate;
		RecipeTemplate smallCraftingTemplate;

		if ( uristqwerty.CraftGuide.CraftGuide.newerBackgroundStyle )
		{
			craftingTemplate = generator.createRecipeTemplate( craftingSlotsOwnBackground, null );
			smallCraftingTemplate = generator.createRecipeTemplate( smallCraftingSlotsOwnBackground, null );
		}
		else
		{
			craftingTemplate = new DefaultRecipeTemplate( craftingSlots, RecipeGeneratorImplementation.workbench, new TextureClip(
					DynamicTexture.instance( "recipe_backgrounds" ), 1, 1, 79, 58 ), new TextureClip( DynamicTexture.instance( "recipe_backgrounds" ), 82, 1,
					79, 58 ) );

			smallCraftingTemplate = new DefaultRecipeTemplate( smallCraftingSlots, RecipeGeneratorImplementation.workbench, new TextureClip(
					DynamicTexture.instance( "recipe_backgrounds" ), 1, 61, 79, 58 ), new TextureClip( DynamicTexture.instance( "recipe_backgrounds" ), 82, 61,
					79, 58 ) );
		}

		RecipeTemplate shapelessTemplate = new DefaultRecipeTemplate( shapelessCraftingSlots, RecipeGeneratorImplementation.workbench, new TextureClip(
				DynamicTexture.instance( "recipe_backgrounds" ), 1, 121, 79, 58 ), new TextureClip( DynamicTexture.instance( "recipe_backgrounds" ), 82, 121,
				79, 58 ) );

		RecipeTemplate furnaceTemplate = new DefaultRecipeTemplate( furnaceSlots, new ItemStack( Blocks.furnace ), new TextureClip(
				DynamicTexture.instance( "recipe_backgrounds" ), 1, 181, 79, 58 ), new TextureClip( DynamicTexture.instance( "recipe_backgrounds" ), 82, 181,
				79, 58 ) );

		addCraftingRecipes( craftingTemplate, smallCraftingTemplate, shapelessTemplate, this );
		addGrinderRecipes( furnaceTemplate, this );
		addInscriberRecipes( furnaceTemplate, this );
	}

	private void addCraftingRecipes(RecipeTemplate template, RecipeTemplate templateSmall, RecipeTemplate templateShapeless, RecipeGenerator generator)
	{
		List recipes = CraftingManager.getInstance().getRecipeList();

		int errCount = 0;

		for (Object o : recipes)
		{
			try
			{
				IRecipe recipe = (IRecipe) o;

				Object[] items = generator.getCraftingRecipe( recipe, true );

				if ( items == null )
				{
					continue;
				}
				else if ( items.length == 5 )
				{
					generator.addRecipe( templateSmall, items );
				}
				else if ( recipe instanceof ShapelessRecipe )
				{
					generator.addRecipe( templateShapeless, items );
				}
				else
				{
					generator.addRecipe( template, items );
				}
			}
			catch (Exception e)
			{
				if ( errCount == -1 )
				{
				}
				else if ( errCount++ >= 5 )
				{
					CraftGuideLog
							.log( "CraftGuide DefaultRecipeProvider: Stack trace limit reached, further stack traces from this invocation will not be logged to the console. They will still be logged to (.minecraft)/config/CraftGuide/CraftGuide.log",
									true );
					errCount = -1;
				}
				else
				{
					e.printStackTrace();
				}

				CraftGuideLog.log( e );
			}
		}
	}

	private void addGrinderRecipes(RecipeTemplate template, RecipeGenerator generator)
	{

	}

	private void addInscriberRecipes(RecipeTemplate template, RecipeGenerator generator)
	{

	}

	@Override
	public RecipeTemplate createRecipeTemplate(Slot[] slots, ItemStack craftingType)
	{
		return parent.createRecipeTemplate( slots, craftingType );
	}

	@Override
	public RecipeTemplate createRecipeTemplate(Slot[] slots, ItemStack craftingType, String backgroundTexture, int backgroundX, int backgroundY,
			int backgroundSelectedX, int backgroundSelectedY)
	{
		return parent.createRecipeTemplate( slots, craftingType, backgroundTexture, backgroundX, backgroundY, backgroundSelectedX, backgroundSelectedY );
	}

	@Override
	public RecipeTemplate createRecipeTemplate(Slot[] slots, ItemStack craftingType, String backgroundTexture, int backgroundX, int backgroundY,
			String backgroundSelectedTexture, int backgroundSelectedX, int backgroundSelectedY)
	{
		return parent.createRecipeTemplate( slots, craftingType, backgroundTexture, backgroundX, backgroundY, backgroundSelectedTexture, backgroundSelectedX,
				backgroundSelectedY );
	}

	@Override
	public void addRecipe(RecipeTemplate template, Object[] crafting)
	{
		parent.addRecipe( template, crafting );
	}

	@Override
	public void addRecipe(CraftGuideRecipe recipe, ItemStack craftingType)
	{
		parent.addRecipe( recipe, craftingType );
	}

	@Override
	public void setDefaultTypeVisibility(ItemStack type, boolean visible)
	{
		parent.setDefaultTypeVisibility( type, visible );
	}

	@Override
	public Object[] getCraftingRecipe(IRecipe recipe)
	{
		return getCraftingRecipe( recipe, true );
	}

	Object[] getCraftingShapelessRecipe(List items, ItemStack recipeOutput)
	{
		Object[] output = new Object[10];

		for (int i = 0; i < items.size(); i++)
		{
			output[i] = items.get( i );

			if ( output[i] instanceof ItemStack[] )
				output[i] = Arrays.asList( (ItemStack[]) output[i] );

			if ( output[i] instanceof IIngredient )
			{
				try
				{
					output[i] = toCG( ((IIngredient) output[i]).getItemStackSet() );
				}
				catch (RegistrationError ignored)
				{

				}
				catch (MissingIngredientError ignored)
				{

				}
			}
		}

		output[9] = recipeOutput;
		return output;
	}

	Object[] getCraftingShapedRecipe(int width, int height, Object[] items, ItemStack recipeOutput)
	{
		Object[] output = new Object[10];

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int i = y * 3 + x;
				output[i] = items[y * width + x];

				if ( output[i] instanceof ItemStack[] )
					output[i] = Arrays.asList( (ItemStack[]) output[i] );

				if ( output[i] instanceof IIngredient )
				{
					try
					{
						output[i] = toCG( ((IIngredient) output[i]).getItemStackSet() );
					}
					catch (RegistrationError ignored)
					{

					}
					catch (MissingIngredientError ignored)
					{

					}
				}
			}
		}

		output[9] = recipeOutput;
		return output;
	}

	Object[] getSmallShapedRecipe(int width, int height, Object[] items, ItemStack recipeOutput)
	{
		Object[] output = new Object[5];

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int i = y * 2 + x;
				output[i] = items[y * width + x];

				if ( output[i] instanceof ItemStack[] )
					output[i] = Arrays.asList( (ItemStack[]) output[i] );

				if ( output[i] instanceof IIngredient )
				{
					try
					{
						output[i] = toCG( ((IIngredient) output[i]).getItemStackSet() );
					}
					catch (RegistrationError ignored)
					{

					}
					catch (MissingIngredientError ignored)
					{

					}
				}
			}
		}

		output[4] = recipeOutput;
		return output;
	}

	private Object toCG(ItemStack[] itemStackSet)
	{
		List<ItemStack> list = Arrays.asList( itemStackSet );

		for (int x = 0; x < list.size(); x++)
		{
			list.set( x, list.get( x ).copy() );
			if ( list.get( x ).stackSize == 0 )
				list.get( x ).stackSize = 1;
		}

		return list;
	}

	@Override
	public Object[] getCraftingRecipe(IRecipe recipe, boolean allowSmallGrid)
	{
		if ( recipe instanceof ShapelessRecipe )
		{
			List items = ReflectionHelper.getPrivateValue( ShapelessRecipe.class, (ShapelessRecipe) recipe, "input" );
			return getCraftingShapelessRecipe( items, recipe.getRecipeOutput() );
		}
		else if ( recipe instanceof ShapedRecipe )
		{
			int width = ReflectionHelper.getPrivateValue( ShapedRecipe.class, (ShapedRecipe) recipe, "width" );
			int height = ReflectionHelper.getPrivateValue( ShapedRecipe.class, (ShapedRecipe) recipe, "height" );
			Object[] items = ReflectionHelper.getPrivateValue( ShapedRecipe.class, (ShapedRecipe) recipe, "input" );

			if ( allowSmallGrid && width < 3 && height < 3 )
			{
				return getSmallShapedRecipe( width, height, items, recipe.getRecipeOutput() );
			}
			else
			{
				return getCraftingShapedRecipe( width, height, items, recipe.getRecipeOutput() );
			}

		}

		return null;
	}

	@Override
	public void Init() throws Throwable
	{
		StackInfo.addSource( this );
	}

	@Override
	public void PostInit() throws Throwable
	{

	}

}
