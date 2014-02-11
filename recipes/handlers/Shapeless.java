package appeng.recipes.handlers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import appeng.core.AELog;
import appeng.recipes.Ingredient;
import appeng.recipes.MissingIngredientError;
import appeng.recipes.RecipeError;
import appeng.recipes.RegistrationError;
import appeng.recipes.Recipes.ShapelessRecipe;
import cpw.mods.fml.common.registry.GameRegistry;

public class Shapeless extends CraftHandler
{

	List<Ingredient> inputs;
	Ingredient output;

	@Override
	public void setup(List<List<Ingredient>> input, List<List<Ingredient>> output) throws RecipeError
	{
		if ( output.size() == 1 && output.get( 0 ).size() == 1 )
		{
			if ( input.size() == 1 )
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
	public void register() throws RegistrationError, MissingIngredientError
	{
		List<Object> args = new ArrayList();
		for (Ingredient i : inputs)
			args.add( i.getSet() );

		ItemStack outIS = output.getItemStack();

		try
		{
			GameRegistry.addRecipe( new ShapelessRecipe( outIS, args.toArray( new Object[args.size()] ) ) );
		}
		catch (Throwable e)
		{
			AELog.error( e );
			throw new RegistrationError( "Erro while adding shapeless recipe." );
		}
	}
}
