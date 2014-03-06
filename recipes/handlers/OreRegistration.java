package appeng.recipes.handlers;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RecipeError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IIngredient;

public class OreRegistration implements ICraftHandler
{

	List<IIngredient> inputs;
	String name;

	public OreRegistration(List<IIngredient> in, String out) {
		inputs = in;
		name = out;
	}

	@Override
	public void register() throws RegistrationError, MissingIngredientError
	{
		for (IIngredient i : inputs)
		{
			for (ItemStack is : i.getItemStackSet())
			{
				OreDictionary.registerOre( name, is );
			}
		}
	}

	@Override
	public void setup(List<List<IIngredient>> input,
			List<List<IIngredient>> output) throws RecipeError {
		
	}
	
	@Override
	public boolean canCraft(ItemStack output) throws RegistrationError, MissingIngredientError {
		return false;
	}
	
}
