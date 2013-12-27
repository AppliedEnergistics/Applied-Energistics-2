package appeng.recipes;

import java.util.Iterator;

public class Recipe
{

	final int size;
	final RecipeType type;
	final RecipeItem items[];

	public Recipe(RecipeType rt, Iterator<RecipeItem> ilist) throws RecipeError {
		type = rt;
		items = new RecipeItem[rt.getSize()];

		int itemCount = 0;
		while (itemCount < rt.getSize() && ilist.hasNext())
		{
			items[itemCount++] = ilist.next();
		}

		if ( ilist.hasNext() )
			throw new RecipeError( "Invalid Number of Items in recipe." );

		if ( rt.isShaped() )
		{
			if ( itemCount != rt.getSize() )
				throw new RecipeError( "Invalid Number of Items in recipe." );

			size = rt.getSize();
		}
		else
			size = itemCount;
	}

}
