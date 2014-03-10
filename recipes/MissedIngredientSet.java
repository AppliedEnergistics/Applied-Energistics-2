package appeng.recipes;

import appeng.api.recipes.ResolverResultSet;

public class MissedIngredientSet extends Throwable
{

	private static final long serialVersionUID = 2672951714376345807L;
	final ResolverResultSet rrs;

	public MissedIngredientSet(ResolverResultSet ro) {
		rrs = ro;
	}

}
