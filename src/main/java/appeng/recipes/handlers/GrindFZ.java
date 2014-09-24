package appeng.recipes.handlers;

import java.util.List;

import net.minecraft.item.ItemStack;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RecipeError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IIngredient;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IFZ;
import appeng.recipes.RecipeHandler;
import appeng.util.Platform;

public class GrindFZ implements ICraftHandler, IWebsiteSerializer
{

	IIngredient pro_input;
	IIngredient pro_output[];

	@Override
	public void setup(List<List<IIngredient>> input, List<List<IIngredient>> output) throws RecipeError
	{
		if ( input.size() == 1 && output.size() == 1 )
		{
			int outs = output.get( 0 ).size();
			if ( input.get( 0 ).size() == 1 && outs == 1 )
			{
				pro_input = input.get( 0 ).get( 0 );
				pro_output = output.get( 0 ).toArray( new IIngredient[outs] );
				return;
			}
		}
		new RecipeError( "Grind must have a single input, and single output." );
	}

	@Override
	public void register() throws RegistrationError, MissingIngredientError
	{
		if ( AppEng.instance.isIntegrationEnabled( IntegrationType.FZ ) )
		{
			IFZ fz = (IFZ) AppEng.instance.getIntegration( IntegrationType.FZ );
			for (ItemStack is : pro_input.getItemStackSet())
			{
				try
				{
					fz.grinderRecipe( is, pro_output[0].getItemStack() );
				}
				catch (java.lang.RuntimeException err)
				{
					AELog.info( "FZ not happy - " + err.getMessage() );
				}
			}
		}
	}

	@Override
	public boolean canCraft(ItemStack output) throws RegistrationError, MissingIngredientError
	{
		return Platform.isSameItemPrecise( pro_output[0].getItemStack(), output );
	}

	@Override
	public String getPattern(RecipeHandler h)
	{
		return null;
	}

}
