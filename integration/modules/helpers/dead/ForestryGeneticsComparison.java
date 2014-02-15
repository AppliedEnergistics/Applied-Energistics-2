package appeng.integration.modules.helpers.dead;

import appeng.api.features.IItemComparison;
import appeng.api.integration.IBeeComparison;
import forestry.api.arboriculture.EnumTreeChromosome;
import forestry.api.genetics.IIndividual;

public class ForestryGeneticsComparison implements IItemComparison, IBeeComparison
{

	IIndividual idiv;
	String Species;

	@Override
	public IIndividual getIndividual()
	{
		return idiv;
	}

	public ForestryGeneticsComparison(IIndividual _idiv) {
		idiv = _idiv;
		Species = _idiv.getGenome().getActiveAllele( EnumTreeChromosome.SPECIES.ordinal() ).getUID();
	}

	@Override
	public boolean sameAsPrecise(IItemComparison comp)
	{
		if ( comp instanceof ForestryGeneticsComparison )
		{
			IIndividual op = ((ForestryGeneticsComparison) comp).idiv;
			if ( idiv.isAnalyzed() == op.isAnalyzed() )
				return idiv.isGeneticEqual( op );
		}

		return false;
	}

	@Override
	public boolean sameAsFuzzy(IItemComparison comp)
	{
		if ( comp instanceof ForestryGeneticsComparison )
			return Species.equals( ((ForestryGeneticsComparison) comp).Species );

		return false;
	}

}
