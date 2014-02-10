package appeng.recipes.handlers;

import java.util.List;

import appeng.recipes.Ingredient;
import appeng.recipes.RecipeError;
import appeng.recipes.RegistrationError;
import cpw.mods.fml.common.registry.GameRegistry;

public class Smelt extends CraftHandler
{

	Ingredient in;
	Ingredient out;

	@Override
	public void setup(List<List<Ingredient>> input, List<List<Ingredient>> output) throws RecipeError
	{
		if ( input.size() == 1 && output.size() == 1 )
		{
			List<Ingredient> inputList = input.get( 0 );
			List<Ingredient> outputList = output.get( 0 );
			if ( inputList.size() == 1 && outputList.size() == 1 )
			{
				in = inputList.get( 0 );
				out = outputList.get( 0 );
				return;
			}
		}
		throw new RecipeError( "Smelting recipe can only have a single input and output." );
	}

	@Override
	public void register() throws RegistrationError
	{
		if ( in.getItemStack().getItem() == null )
			throw new RegistrationError( in.toString() + ": Smelting Input is not a valid item." );

		if ( out.getItemStack().getItem() == null )
			throw new RegistrationError( out.toString() + ": Smelting Output is not a valid item." );

		GameRegistry.addSmelting( in.getItemStack(), out.getItemStack(), 0 );
	}

}
