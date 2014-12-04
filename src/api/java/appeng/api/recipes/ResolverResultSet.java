package appeng.api.recipes;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

public class ResolverResultSet
{

	public final String name;
	public final List<ItemStack> results;

	public ResolverResultSet(String myName, ItemStack... set) {
		results = Arrays.asList( set );
		name = myName;
	}

}
