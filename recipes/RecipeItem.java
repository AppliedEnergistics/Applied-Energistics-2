package appeng.recipes;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class RecipeItem
{

	final private ItemStack is[];
	final private int oreID;

	RecipeItem(ItemStack in) {
		oreID = -1;
		is = new ItemStack[1];
		is[0] = in;
	}

	RecipeItem(String oreName) {
		oreID = OreDictionary.getOreID( oreName );
		List<ItemStack> ores = OreDictionary.getOres( oreID );
		is = ores.toArray( new ItemStack[ores.size()] );
	}

	int getOreID()
	{
		return oreID;
	}

	ItemStack[] getValidItems()
	{
		return is;
	}

}
