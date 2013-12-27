package appeng.core.features.registries;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import appeng.api.features.IItemComparisionProvider;
import appeng.api.features.IItemComparison;
import appeng.api.features.ISpecialComparisonRegistry;

public class SpecialComparisonRegistry implements ISpecialComparisonRegistry
{

	private List<IItemComparisionProvider> CompRegistry;

	public SpecialComparisonRegistry() {
		CompRegistry = new ArrayList<IItemComparisionProvider>();
	}

	@Override
	public IItemComparison getSpecialComparion(ItemStack stack)
	{
		for (IItemComparisionProvider i : CompRegistry)
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
	public void addComparisonProvider(IItemComparisionProvider prov)
	{
		CompRegistry.add( prov );
	}

}
