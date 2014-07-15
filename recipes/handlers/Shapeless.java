package appeng.recipes.handlers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RecipeError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IIngredient;
import appeng.core.AELog;
import appeng.recipes.RecipeHandler;
import appeng.recipes.game.ShapelessRecipe;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;

public class Shapeless implements ICraftHandler, IWebsiteSeralizer
{

	List<IIngredient> inputs;
	IIngredient output;

	@Override
	public void setup(List<List<IIngredient>> input, List<List<IIngredient>> output) throws RecipeError
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
		List<Object> args = new ArrayList<Object>();
		for (IIngredient i : inputs)
			args.add( i.getItemStackSet() );

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
	
	@Override
	public boolean canCraft(ItemStack reqOutput) throws RegistrationError, MissingIngredientError {

		for ( int y = 0; y < inputs.size(); y++ )
		{
			IIngredient i = inputs.get(y);
				if ( !i.isAir() )
				{
					for ( ItemStack r : i.getItemStackSet() )
					{
						if ( Platform.isSameItemPrecise( r, reqOutput) )
							return false;
					}
				}
			}
		
		return Platform.isSameItemPrecise( output.getItemStack(),reqOutput );
	}

	@Override
	public String getPattern( RecipeHandler h) {
		String o = "shapeless "+output.getQty()+"\n";

		o += h.getName(output)+"\n";
		
		for ( int y = 0; y < inputs.size(); y++ )
		{
			IIngredient i = inputs.get(y);
			if ( i.isAir() )
				o += "air"+( y +1 == inputs.size() ? "\n" : " " );
			else
				o += h.getName(i)+( y +1 == inputs.size() ? "\n" : " " );
		}
		
		return o.trim();
	}
	
}
