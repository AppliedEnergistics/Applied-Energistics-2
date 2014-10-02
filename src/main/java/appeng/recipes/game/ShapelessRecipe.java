package appeng.recipes.game;

import java.util.ArrayList;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.IIngredient;

public class ShapelessRecipe implements IRecipe, IRecipeBakeable
{

	private ItemStack output = null;
	private final ArrayList<Object> input = new ArrayList<Object>();
	private boolean disable = false;

	public boolean isEnabled()
	{
		return !disable;
	}

	public ShapelessRecipe(ItemStack result, Object... recipe)
	{
		output = result.copy();
		for (Object in : recipe)
		{
			if ( in instanceof IIngredient )
			{
				input.add( in );
			}
			else
			{
				StringBuilder ret = new StringBuilder( "Invalid shapeless ore recipe: " );
				for (Object tmp : recipe)
				{
					ret.append( tmp + ", " );
				}
				ret.append( output );
				throw new RuntimeException( ret.toString() );
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
		if ( disable )
			return false;

		ArrayList<Object> required = new ArrayList<Object>( input );

		for (int x = 0; x < var1.getSizeInventory(); x++)
		{
			ItemStack slot = var1.getStackInSlot( x );

			if ( slot != null )
			{
				boolean inRecipe = false;

				for (Object next : required)
				{
					boolean match = false;

					if ( next instanceof IIngredient )
					{
						try
						{
							for (ItemStack item : ((IIngredient) next).getItemStackSet())
							{
								match = match || checkItemEquals( item, slot );
							}
						}
						catch (RegistrationError e)
						{
							// :P
						}
						catch (MissingIngredientError e)
						{
							// :P
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

	@Override
	public void bake() throws RegistrationError, MissingIngredientError
	{
		try
		{
			disable = false;
			for (Object o : getInput())
			{
				if ( o instanceof IIngredient )
					((IIngredient) o).bake();
			}
		}
		catch (MissingIngredientError e)
		{
			disable = true;
		}
	}
}