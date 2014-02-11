package appeng.recipes.handlers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import appeng.core.AELog;
import appeng.recipes.Ingredient;
import appeng.recipes.MissingIngredientError;
import appeng.recipes.RecipeError;
import appeng.recipes.RegistrationError;
import appeng.recipes.Recipes.ShapedRecipe;
import cpw.mods.fml.common.registry.GameRegistry;

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
			rows = input.size();
			if ( rows > 0 && input.size() <= 3 )
			{
				cols = input.get( 0 ).size();
				for (int x = 0; x < input.size(); x++)
					if ( input.get( x ).size() != cols )
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
	public void register() throws RegistrationError, MissingIngredientError
	{
		char first = 'A';
		List<Object> args = new ArrayList();

		for (int y = 0; y < rows; y++)
		{
			String row = "";
			for (int x = 0; x < cols; x++)
			{
				if ( inputs.get( y ).get( x ).isAir )
					row = row + " ";
				else
				{
					row = row + first;
					args.add( first );
					args.add( inputs.get( y ).get( x ).getSet() );

					first++;
				}
			}
			args.add( y, new String( row ) );
		}

		ItemStack outIS = output.getItemStack();

		try
		{
			GameRegistry.addRecipe( new ShapedRecipe( outIS, args.toArray( new Object[args.size()] ) ) );
		}
		catch (Throwable e)
		{
			AELog.error( e );
			throw new RegistrationError( "Error while adding shaped recipe." );
		}
	}
}
