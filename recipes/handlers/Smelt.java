package appeng.recipes.handlers;

import java.util.List;

import net.minecraft.item.ItemStack;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RecipeError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IIngredient;
import appeng.recipes.RecipeHandler;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;

public class Smelt implements ICraftHandler, IWebsiteSeralizer
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
		throw new RecipeError( "Smelting recipe can only have a single input and output." );
	}

	@Override
	public void register() throws RegistrationError, MissingIngredientError
	{
		if ( in.getItemStack().getItem() == null )
			throw new RegistrationError( in.toString() + ": Smelting Input is not a valid item." );

		if ( out.getItemStack().getItem() == null )
			throw new RegistrationError( out.toString() + ": Smelting Output is not a valid item." );

		GameRegistry.addSmelting( in.getItemStack(), out.getItemStack(), 0 );
	}

	@Override
	public boolean canCraft(ItemStack reqOutput) throws RegistrationError, MissingIngredientError {
		return Platform.isSameItemPrecise( out.getItemStack(),reqOutput );
	}

	@Override
	public String getPattern( RecipeHandler h ) {
		return "smelt "+out.getQty()+"\n"+
				h.getName(out)+"\n"+
				h.getName(in);
	}
}
