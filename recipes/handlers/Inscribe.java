package appeng.recipes.handlers;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RecipeError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IIngredient;
import appeng.recipes.RecipeHandler;
import appeng.util.Platform;

public class Inscribe implements ICraftHandler, IWebsiteSeralizer
{

	public static class InscriberRecipe
	{

		public InscriberRecipe(ItemStack[] imprintable, ItemStack plateA, ItemStack plateB, ItemStack out, boolean usePlates) {
			this.imprintable = imprintable;
			this.usePlates = usePlates;
			this.plateA = plateA;
			this.plateB = plateB;
			output = out;
		}

		final public boolean usePlates;

		final public ItemStack plateA;
		final public ItemStack[] imprintable;
		final public ItemStack plateB;
		final public ItemStack output;

	};

	public boolean usePlates = false;

	public static HashSet<ItemStack> plates = new HashSet();
	public static HashSet<ItemStack> inputs = new HashSet();
	public static LinkedList<InscriberRecipe> recipes = new LinkedList();

	IIngredient imprintable;

	IIngredient plateA;
	IIngredient plateB;

	IIngredient output;

	@Override
	public void setup(List<List<IIngredient>> input, List<List<IIngredient>> output) throws RecipeError
	{
		if ( output.size() == 1 && output.get( 0 ).size() == 1 )
		{
			if ( input.size() == 1 && input.get( 0 ).size() > 1 )
			{
				imprintable = input.get( 0 ).get( 0 );

				plateA = input.get( 0 ).get( 1 );

				if ( input.get( 0 ).size() > 2 )
					plateB = input.get( 0 ).get( 2 );

				this.output = output.get( 0 ).get( 0 );
			}
			else
				throw new RecipeError( "Inscriber recipes cannot have rows, and must have more then one input." );
		}
		else
			throw new RecipeError( "Inscriber recipes must produce a single output." );
	}

	@Override
	public void register() throws RegistrationError, MissingIngredientError
	{
		if ( imprintable != null )
			for (ItemStack s : imprintable.getItemStackSet())
				inputs.add( s );

		if ( plateA != null )
			for (ItemStack s : plateA.getItemStackSet())
				plates.add( s );

		if ( plateB != null )
			for (ItemStack s : plateB.getItemStackSet())
				plates.add( s );

		InscriberRecipe ir = new InscriberRecipe( imprintable.getItemStackSet(), plateA == null ? null : plateA.getItemStack(), plateB == null ? null
				: plateB.getItemStack(), output.getItemStack(), usePlates );
		recipes.add( ir );
	}

	@Override
	public boolean canCraft(ItemStack reqOutput) throws RegistrationError, MissingIngredientError
	{
		return Platform.isSameItemPrecise( output.getItemStack(), reqOutput );
	}

	@Override
	public String getPattern(RecipeHandler h)
	{
		String o = "inscriber " + output.getQty() + "\n";

		o += h.getName( output ) + "\n";

		if ( plateB != null )
			o += h.getName( plateB ) + "\n";

		o += h.getName( plateA );

		return o;
	}

}
