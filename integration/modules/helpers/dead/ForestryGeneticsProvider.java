package appeng.integration.modules.helpers.dead;

import net.minecraft.item.ItemStack;
import appeng.api.features.IItemComparisionProvider;
import appeng.api.features.IItemComparison;
import forestry.api.genetics.IIndividual;

public class ForestryGeneticsProvider implements IItemComparisionProvider
{

	@Override
	public IItemComparison getComparison(ItemStack is)
	{
		if ( forestry.api.genetics.AlleleManager.alleleRegistry != null )
		{
			IIndividual idiv = forestry.api.genetics.AlleleManager.alleleRegistry.getIndividual( is );
			if ( idiv == null )
				return null;
			return new ForestryGeneticsComparison( idiv );
		}
		return null;
	}

	@Override
	public boolean canHandle(ItemStack stack)
	{
		if ( forestry.api.genetics.AlleleManager.alleleRegistry != null )
			return forestry.api.genetics.AlleleManager.alleleRegistry.isIndividual( stack );
		return false;
	}

}
