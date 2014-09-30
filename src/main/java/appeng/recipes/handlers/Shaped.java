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
import appeng.recipes.game.ShapedRecipe;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;

public class Shaped implements ICraftHandler, IWebsiteSerializer
{

	private int rows;
	private int cols;

	List<List<IIngredient>> inputs;
	IIngredient output;

	@Override
	public void setup(List<List<IIngredient>> input, List<List<IIngredient>> output) throws RecipeError
	{
		if ( output.size() == 1 && output.get( 0 ).size() == 1 )
		{
			rows = input.size();
			if ( rows > 0 && input.size() <= 3 )
			{
				cols = input.get( 0 ).size();
				if ( cols <= 3 && cols >= 1 )
				{
					for (List<IIngredient> anInput : input)
					{
						if ( anInput.size() != cols )
						{
							throw new RecipeError( "all rows in a shaped crafting recipe must contain the same number of ingredients." );
						}
					}

					inputs = input;
					this.output = output.get( 0 ).get( 0 );
				}
				else
					throw new RecipeError( "Crafting recipes must have 1-3 columns." );
			}
			else
				throw new RecipeError( "shaped crafting recipes must have 1-3 rows." );
		}
		else
			throw new RecipeError( "Crafting must produce a single output." );
	}

	@Override
	public void register() throws RegistrationError, MissingIngredientError
	{
		char first = 'A';
		List<Object> args = new ArrayList<Object>();

		for (int y = 0; y < rows; y++)
		{
			StringBuilder row = new StringBuilder();
			for (int x = 0; x < cols; x++)
			{
				if ( inputs.get( y ).get( x ).isAir() )
					row.append( " " );
				else
				{
					row.append( first );
					args.add( first );
					args.add( inputs.get( y ).get( x ) );

					first++;
				}
			}
			args.add( y, row.toString() );
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

	@Override
	public boolean canCraft(ItemStack reqOutput) throws RegistrationError, MissingIngredientError
	{
		for (int y = 0; y < rows; y++)
			for (int x = 0; x < cols; x++)
			{
				IIngredient i = inputs.get( y ).get( x );

				if ( !i.isAir() )
				{
					for ( ItemStack r : i.getItemStackSet() )
					{
						if ( Platform.isSameItemPrecise( r, reqOutput) )
							return false;
					}
				}
			}
		
		return Platform.isSameItemPrecise( output.getItemStack(), reqOutput );
	}

	@Override
	public String getPattern(RecipeHandler h)
	{
		String o = "shaped " + output.getQty() + " " + cols + "x" + rows + "\n";

		o += h.getName( output ) + "\n";

		for (int y = 0; y < rows; y++)
			for (int x = 0; x < cols; x++)
			{
				IIngredient i = inputs.get( y ).get( x );

				if ( i.isAir() )
					o += "air" + (x + 1 == cols ? "\n" : " ");
				else
					o += h.getName( i ) + (x + 1 == cols ? "\n" : " ");
			}

		return o.trim();
	}
}
