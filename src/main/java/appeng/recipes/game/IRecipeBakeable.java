package appeng.recipes.game;

import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RegistrationError;


public interface IRecipeBakeable
{

	void bake() throws RegistrationError, MissingIngredientError;
	
}
