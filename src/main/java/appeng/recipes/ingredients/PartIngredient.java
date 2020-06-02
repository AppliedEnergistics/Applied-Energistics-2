package appeng.recipes.ingredients;


import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.stream.Stream;


public class PartIngredient extends Ingredient
{
	private final String partName;

	protected PartIngredient( String partName, Stream<? extends IItemList> itemLists )
	{
		super( itemLists );
		this.partName = partName;
	}

	public static PartIngredient fromStacks(String partName, ItemStack... stacks) {
		return new PartIngredient( partName, Arrays.stream( stacks ).map( SingleItemList::new ) );
	}

	public static PartIngredient empty( String partName )
	{
		return new PartIngredient( partName, Stream.empty() );
	}

	public String getPartName()
	{
		return partName;
	}

}
