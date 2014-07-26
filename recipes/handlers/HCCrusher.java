package appeng.recipes.handlers;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RecipeError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IIngredient;
import appeng.core.AELog;
import appeng.recipes.RecipeHandler;
import appeng.util.Platform;
import cpw.mods.fml.common.event.FMLInterModComms;

public class HCCrusher implements ICraftHandler, IWebsiteSeralizer
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
		new RecipeError( "Crusher must have a single input, and single output." );
	}

	@Override
	public void register() throws RegistrationError, MissingIngredientError
	{
		for (ItemStack is : pro_input.getItemStackSet())
		{
			try
			{
				NBTTagCompound toRegister = new NBTTagCompound();
				
				ItemStack beginStack = is;
				ItemStack endStack = pro_output[0].getItemStack();
				
				NBTTagCompound itemFrom = new NBTTagCompound();
				NBTTagCompound itemTo = new NBTTagCompound();

				beginStack.writeToNBT(itemFrom);  
				endStack.writeToNBT(itemTo);

				toRegister.setTag("itemFrom", itemFrom);
				toRegister.setTag("itemTo", itemTo);
				toRegister.setFloat("pressureRatio", 1.0F);
				
				FMLInterModComms.sendMessage("HydCraft", "registerCrushingRecipe", toRegister);
			}
			catch (java.lang.RuntimeException err)
			{
				AELog.info( "Hydraulicraft not happy - " + err.getMessage() );
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
