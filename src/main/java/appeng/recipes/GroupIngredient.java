package appeng.recipes;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RecipeError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.IIngredient;

public class GroupIngredient implements IIngredient
{

	int qty = 0;
	final String name;
	final List<IIngredient> ingredients;
	ItemStack[] baked;

	boolean isInside = false;

	public GroupIngredient(String myName, List<IIngredient> ingredients) throws RecipeError {
		name = myName;

		for (IIngredient I : ingredients)
			if ( I.isAir() )
				throw new RecipeError( "Cannot include air in a group." );

		this.ingredients = ingredients;
	}

	public IIngredient copy(int qty) throws RecipeError
	{
		GroupIngredient gi = new GroupIngredient( name, ingredients );
		gi.qty = qty;
		return gi;
	}

	public int getDamageValue()
	{
		return OreDictionary.WILDCARD_VALUE;
	}

	@Override
	public String getItemName()
	{
		return name;
	}

	@Override
	public ItemStack getItemStack() throws RegistrationError, MissingIngredientError
	{
		throw new RegistrationError( "Cannot pass group of items to a recipe which desires a single recipe item." );
	}

	@Override
	public ItemStack[] getItemStackSet() throws RegistrationError, MissingIngredientError
	{
		if ( baked != null )
			return baked;

		if ( isInside )
			return new ItemStack[0];

		List<ItemStack> out = new LinkedList();
		isInside = true;
		try
		{
			for (IIngredient i : ingredients)
			{
				try
				{
					out.addAll( Arrays.asList( i.getItemStackSet() ) );
				}
				catch (MissingIngredientError mir)
				{
					// oh well this is a group!
				}
			}
		}
		finally
		{
			isInside = false;
		}

		if ( out.size() == 0 )
			throw new MissingIngredientError( toString() + " - group could not be resolved to any items." );

		for (ItemStack is : out)
			is.stackSize = qty;

		return out.toArray( new ItemStack[out.size()] );
	}

	public String getNameSpace()
	{
		return "";
	}

	@Override
	public int getQty()
	{
		return 0;
	}

	@Override
	public boolean isAir()
	{
		return false;
	}

	@Override
	public void bake() throws RegistrationError, MissingIngredientError
	{
		baked = null;
		baked = getItemStackSet();
	}

}
