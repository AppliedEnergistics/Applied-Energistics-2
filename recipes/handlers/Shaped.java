package appeng.recipes.handlers;

import java.util.List;

import appeng.recipes.Ingredient;
import appeng.recipes.RecipeError;
import appeng.recipes.RegistrationError;

public class Shaped extends CraftHandler
{

	private int rows;
	private int cols;

	List<List<Ingredient>> inputs;
	Ingredient output;

	@Override
	public void setup(List<List<Ingredient>> input, List<List<Ingredient>> output) throws RecipeError
	{
		if ( output.size() == 1 && output.get( 0 ).size() == 1 )
		{
			rows = inputs.size();
			if ( rows > 0 && inputs.size() <= 3 )
			{
				cols = inputs.get( 0 ).size();
				for (int x = 0; x < inputs.size(); x++)
					if ( inputs.get( x ).size() != cols )
						throw new RecipeError( "all rows in a shaped crafting recipe must contain the same number of ingredients." );

				inputs = input;
				this.output = output.get( 0 ).get( 0 );
			}
			else
				throw new RecipeError( "shaped crafting recpies must have 1-3 rows." );
		}
		else
			throw new RecipeError( "Crafting must produce a single output." );
	}

	@Override
	public void register() throws RegistrationError
	{

	}
}
