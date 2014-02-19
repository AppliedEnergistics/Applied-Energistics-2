package appeng.recipes.handlers;

import java.util.List;

import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RecipeError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IIngredient;

public class Pureify implements ICraftHandler
{

	IIngredient in;
	IIngredient out;

	@Override
	public void setup(List<List<IIngredient>> input, List<List<IIngredient>> output) throws RecipeError
	{
		if ( input.size() == 1 && output.size() == 1 )
		{
			List<IIngredient> inputList = input.get( 0 );
			List<IIngredient> outputList = output.get( 0 );
			if ( inputList.size() == 1 && outputList.size() == 1 )
			{
				in = inputList.get( 0 );
				out = outputList.get( 0 );
				return;
			}
		}
		throw new RecipeError( "Pureify recipe can only have a single input and output." );
	}

	@Override
	public void register() throws RegistrationError, MissingIngredientError
	{
		if ( in.getItemStackSet() == null )
			throw new RegistrationError( in.toString() + ": Pureify Input is not a valid item." );

		if ( out.getItemStack().getItem() == null )
			throw new RegistrationError( out.toString() + ": Pureify Output is not a valid item." );

	}

}
