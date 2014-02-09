package appeng.recipes;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.AEApi;

/*
 * Basically the ore dictionary crafting recipe, with a slight twist 
 * of adding quartz to the lists even tho they are not in the ore dictionary.
 * 
 * Yes this is silly, but it seemed simpler then re-writing half the class to make a simple alteration.
 */

public class AEShapedQuartzRecipe implements IRecipe
{

	// Added in for future ease of change, but hard coded for now.
	private static final int MAX_CRAFT_GRID_WIDTH = 3;
	private static final int MAX_CRAFT_GRID_HEIGHT = 3;

	private ItemStack output = null;
	private Object[] input = null;
	private int width = 0;
	private int height = 0;
	private boolean mirrored = true;

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public Object[] getIngredients()
	{
		return input;
	}

	public AEShapedQuartzRecipe(ItemStack result, boolean dust, Object... recipe) {
		output = result.copy();

		String shape = "";
		int idx = 0;

		if ( recipe[idx] instanceof Boolean )
		{
			mirrored = (Boolean) recipe[idx];
			if ( recipe[idx + 1] instanceof Object[] )
			{
				recipe = (Object[]) recipe[idx + 1];
			}
			else
			{
				idx = 1;
			}
		}

		if ( recipe[idx] instanceof String[] )
		{
			String[] parts = ((String[]) recipe[idx++]);

			for (String s : parts)
			{
				width = s.length();
				shape += s;
			}

			height = parts.length;
		}
		else
		{
			while (recipe[idx] instanceof String)
			{
				String s = (String) recipe[idx++];
				shape += s;
				width = s.length();
				height++;
			}
		}

		if ( width * height != shape.length() )
		{
			String ret = "Invalid shaped ore recipe: ";
			for (Object tmp : recipe)
			{
				ret += tmp + ", ";
			}
			ret += output;
			throw new RuntimeException( ret );
		}

		HashMap<Character, Object> itemMap = new HashMap<Character, Object>();

		for (; idx < recipe.length; idx += 2)
		{
			Character chr = (Character) recipe[idx];
			Object in = recipe[idx + 1];

			if ( in instanceof ItemStack )
			{
				itemMap.put( chr, ((ItemStack) in).copy() );
			}
			else if ( in instanceof Item )
			{
				itemMap.put( chr, new ItemStack( (Item) in ) );
			}
			else if ( in instanceof Block )
			{
				itemMap.put( chr, new ItemStack( (Block) in, 1, OreDictionary.WILDCARD_VALUE ) );
			}
			else if ( in instanceof String )
			{
				ArrayList<ItemStack> is = OreDictionary.getOres( (String) in );
				ArrayList copy = new ArrayList();
				copy.addAll( is );

				if ( in.equals( "crystalQuartz" ) || in.equals( "dustQuartz" ) )
				{
					if ( dust )
					{
						copy.add( AEApi.instance().materials().materialCertusQuartzDust.stack( 1 ) );
						copy.add( AEApi.instance().materials().materialNetherQuartzDust.stack( 1 ) );
					}
					else
					{
						copy.add( AEApi.instance().materials().materialCertusQuartzCrystal.stack( 1 ) );
						copy.add( new ItemStack( Items.quartz ) );
					}
				}

				itemMap.put( chr, copy );
			}
			else
			{
				String ret = "Invalid shaped ore recipe: ";
				for (Object tmp : recipe)
				{
					ret += tmp + ", ";
				}
				ret += output;
				throw new RuntimeException( ret );
			}
		}

		input = new Object[width * height];
		int x = 0;
		for (char chr : shape.toCharArray())
		{
			input[x++] = itemMap.get( chr );
		}
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting var1)
	{
		return output.copy();
	}

	@Override
	public int getRecipeSize()
	{
		return input.length;
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return output;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World world)
	{
		for (int x = 0; x <= MAX_CRAFT_GRID_WIDTH - width; x++)
		{
			for (int y = 0; y <= MAX_CRAFT_GRID_HEIGHT - height; ++y)
			{
				if ( checkMatch( inv, x, y, false ) )
				{
					return true;
				}

				if ( mirrored && checkMatch( inv, x, y, true ) )
				{
					return true;
				}
			}
		}

		return false;
	}

	private boolean checkMatch(InventoryCrafting inv, int startX, int startY, boolean mirror)
	{
		for (int x = 0; x < MAX_CRAFT_GRID_WIDTH; x++)
		{
			for (int y = 0; y < MAX_CRAFT_GRID_HEIGHT; y++)
			{
				int subX = x - startX;
				int subY = y - startY;
				Object target = null;

				if ( subX >= 0 && subY >= 0 && subX < width && subY < height )
				{
					if ( mirror )
					{
						target = input[width - subX - 1 + subY * width];
					}
					else
					{
						target = input[subX + subY * width];
					}
				}

				ItemStack slot = inv.getStackInRowAndColumn( x, y );

				if ( target instanceof ItemStack )
				{
					if ( !checkItemEquals( (ItemStack) target, slot ) )
					{
						return false;
					}
				}
				else if ( target instanceof ArrayList )
				{
					boolean matched = false;

					for (ItemStack item : (ArrayList<ItemStack>) target)
					{
						matched = matched || checkItemEquals( item, slot );
					}

					if ( !matched )
					{
						return false;
					}
				}
				else if ( target == null && slot != null )
				{
					return false;
				}
			}
		}

		return true;
	}

	private boolean checkItemEquals(ItemStack target, ItemStack input)
	{
		if ( input == null && target != null || input != null && target == null )
		{
			return false;
		}
		return (target.getItem() == input.getItem() && (target.getItemDamage() == OreDictionary.WILDCARD_VALUE || target.getItemDamage() == input
				.getItemDamage()));
	}

	public AEShapedQuartzRecipe setMirrored(boolean mirror)
	{
		mirrored = mirror;
		return this;
	}

	/**
	 * Returns the input for this recipe, any mod accessing this value should never manipulate the values in this array
	 * as it will effect the recipe itself.
	 * 
	 * @return The recipes input vales.
	 */
	public Object[] getInput()
	{
		return this.input;
	}
}
