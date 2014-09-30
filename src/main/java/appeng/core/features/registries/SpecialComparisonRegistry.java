package appeng.core.features.registries;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import appeng.api.features.IItemComparisonProvider;
import appeng.api.features.IItemComparison;
import appeng.api.features.ISpecialComparisonRegistry;

public class SpecialComparisonRegistry implements ISpecialComparisonRegistry
{

	private final List<IItemComparisonProvider> CompRegistry;

	public SpecialComparisonRegistry() {
		CompRegistry = new ArrayList<IItemComparisonProvider>();
	}

	@Override
	public IItemComparison getSpecialComparison(ItemStack stack)
	{
		for (IItemComparisonProvider i : CompRegistry)
		{
			IItemComparison comp = i.getComparison( stack );
			if ( comp != null )
			{
				return comp;
			}
		}

		return null;
	}

	@Override
	public void addComparisonProvider(IItemComparisonProvider prov)
	{
		CompRegistry.add( prov );
	}

}
