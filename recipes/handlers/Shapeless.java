package appeng.recipes.handlers;

import java.util.List;

import appeng.recipes.Ingredient;
import appeng.recipes.RecipeError;
import appeng.recipes.RegistrationError;

public class Shapeless extends CraftHandler
{

	List<Ingredient> inputs;
	Ingredient output;

	@Override
	public void setup(List<List<Ingredient>> input, List<List<Ingredient>> output) throws RecipeError
	{
		if ( output.size() == 1 && output.get( 0 ).size() == 1 )
		{
			if ( inputs.size() == 1 )
			{
				inputs = input.get( 0 );
				this.output = output.get( 0 ).get( 0 );
			}
			else
				throw new RecipeError( "Shapeless crafting recipes cannot have rows." );
		}
		else
			throw new RecipeError( "Crafting must produce a single output." );
	}

	@Override
	public void register() throws RegistrationError
	{

	}
}
