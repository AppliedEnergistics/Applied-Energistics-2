package appeng.recipes.handlers;

import java.util.List;

import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RecipeError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IIngredient;

public class Grind implements ICraftHandler
{

	@Override
	public void setup(List<List<IIngredient>> input,
			List<List<IIngredient>> output) throws RecipeError {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void register() throws RegistrationError, MissingIngredientError {
		// TODO Auto-generated method stub
		
	}

}
