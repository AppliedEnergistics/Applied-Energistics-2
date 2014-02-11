package appeng.recipes.handlers;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import appeng.recipes.Ingredient;
import appeng.recipes.MissingIngredientError;
import appeng.recipes.RegistrationError;

public class OreRegistration extends CraftHandler
{

	List<Ingredient> inputs;
	String name;

	public OreRegistration(List<Ingredient> in, String out) {
		inputs = in;
		name = out;
	}

	@Override
	public void register() throws RegistrationError, MissingIngredientError
	{
		for (Ingredient i : inputs)
		{
			for (ItemStack is : i.getSet())
			{
				OreDictionary.registerOre( name, is );
			}
		}
	}
}
