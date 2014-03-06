package appeng.recipes.game;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class ShapelessRecipe implements IRecipe
{

	private ItemStack output = null;
	private ArrayList<Object> input = new ArrayList<Object>();

	public ShapelessRecipe(ItemStack result, Object... recipe) {
		output = result.copy();
		for (Object in : recipe)
		{
			if ( in instanceof ItemStack )
			{
				input.add( ((ItemStack) in).copy() );
			}
			else if ( in instanceof ItemStack[] )
			{
				ItemStack[] a = (ItemStack[]) in;
				if ( a.length == 1 )
					input.add( a[0] );
				else
					input.add( a );
			}
			else if ( in instanceof String )
			{
				input.add( OreDictionary.getOres( (String) in ) );
			}
			else
			{
				String ret = "Invalid shapeless ore recipe: ";
				for (Object tmp : recipe)
				{
					ret += tmp + ", ";
				}
				ret += output;
				throw new RuntimeException( ret );
			}
		}
	}

	@Override
	public int getRecipeSize()
	{
		return input.size();
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return output;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting var1)
	{
		return output.copy();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(InventoryCrafting var1, World world)
	{
		ArrayList<Object> required = new ArrayList<Object>( input );

		for (int x = 0; x < var1.getSizeInventory(); x++)
		{
			ItemStack slot = var1.getStackInSlot( x );

			if ( slot != null )
			{
				boolean inRecipe = false;
				Iterator<Object> req = required.iterator();

				while (req.hasNext())
				{
					boolean match = false;

					Object next = req.next();

					if ( next instanceof ItemStack )
					{
						match = checkItemEquals( (ItemStack) next, slot );
					}
					else if ( next instanceof ItemStack[] )
					{
						for (ItemStack item : (ItemStack[]) next)
						{
							match = match || checkItemEquals( item, slot );
						}
					}
					else if ( next instanceof ArrayList )
					{
						for (ItemStack item : (ArrayList<ItemStack>) next)
						{
							match = match || checkItemEquals( item, slot );
						}
					}

					if ( match )
					{
						inRecipe = true;
						required.remove( next );
						break;
					}
				}

				if ( !inRecipe )
				{
					return false;
				}
			}
		}

		return required.isEmpty();
	}

	private boolean checkItemEquals(ItemStack target, ItemStack input)
	{
		return (target.getItem() == input.getItem() && (target.getItemDamage() == OreDictionary.WILDCARD_VALUE || target.getItemDamage() == input
				.getItemDamage()));
	}

	/**
	 * Returns the input for this recipe, any mod accessing this value should never manipulate the values in this array
	 * as it will effect the recipe itself.
	 * 
	 * @return The recipes input vales.
	 */
	public ArrayList<Object> getInput()
	{
		return this.input;
	}
}