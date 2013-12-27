package appeng.util.inv;

import java.util.Iterator;
import java.util.List;

import net.minecraft.item.ItemStack;
import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.iterators.StackToSlotIterator;

public class AdaptorList extends InventoryAdaptor
{

	private List<ItemStack> i;

	public AdaptorList(List<ItemStack> s) {
		i = s;
	}

	@Override
	public ItemStack removeSimilarItems(int how_many, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination dest)
	{
		int s = i.size();
		for (int x = 0; x < s; x++)
		{
			ItemStack is = i.get( x );
			if ( is != null && (filter == null || Platform.isSameItemFuzzy( is, filter, fuzzyMode )) )
			{
				if ( how_many > is.stackSize )
					how_many = is.stackSize;
				if ( dest != null && !dest.canInsert( is ) )
					how_many = 0;

				if ( how_many > 0 )
				{
					ItemStack rv = is.copy();
					rv.stackSize = how_many;
					is.stackSize -= how_many;

					// remove it..
					if ( is.stackSize <= 0 )
						i.remove( x );

					return rv;
				}
			}
		}
		return null;
	}

	@Override
	public ItemStack simulateSimilarRemove(int how_many, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination dest)
	{
		int s = i.size();
		for (int x = 0; x < s; x++)
		{
			ItemStack is = i.get( x );
			if ( is != null && (filter == null || Platform.isSameItemFuzzy( is, filter, fuzzyMode )) )
			{
				if ( how_many > is.stackSize )
					how_many = is.stackSize;
				if ( dest != null && !dest.canInsert( is ) )
					how_many = 0;

				if ( how_many > 0 )
				{
					ItemStack rv = is.copy();
					rv.stackSize = how_many;
					return rv;
				}
			}
		}
		return null;

	}

	@Override
	public ItemStack removeItems(int how_many, ItemStack filter, IInventoryDestination dest)
	{
		int s = i.size();
		for (int x = 0; x < s; x++)
		{
			ItemStack is = i.get( x );
			if ( is != null && (filter == null || Platform.isSameItemPrecise( is, filter )) )
			{
				if ( how_many > is.stackSize )
					how_many = is.stackSize;
				if ( dest != null && !dest.canInsert( is ) )
					how_many = 0;

				if ( how_many > 0 )
				{
					ItemStack rv = is.copy();
					rv.stackSize = how_many;
					is.stackSize -= how_many;

					// remove it..
					if ( is.stackSize <= 0 )
						i.remove( x );

					return rv;
				}
			}
		}
		return null;
	}

	@Override
	public ItemStack simulateRemove(int how_many, ItemStack filter, IInventoryDestination dest)
	{
		int s = i.size();
		for (int x = 0; x < s; x++)
		{
			ItemStack is = i.get( x );
			if ( is != null && (filter == null || Platform.isSameItemPrecise( is, filter )) )
			{
				if ( how_many > is.stackSize )
					how_many = is.stackSize;
				if ( dest != null && !dest.canInsert( is ) )
					how_many = 0;

				if ( how_many > 0 )
				{
					ItemStack rv = is.copy();
					rv.stackSize = how_many;
					return rv;
				}
			}
		}
		return null;

	}

	@Override
	public ItemStack addItems(ItemStack A)
	{
		if ( A == null )
			return null;
		if ( A.stackSize == 0 )
			return null;

		ItemStack left = A.copy();

		int s = i.size();
		for (int x = 0; x < s; x++)
		{
			ItemStack is = i.get( x );
			if ( Platform.isSameItem( is, left ) )
			{
				is.stackSize += left.stackSize;
				return null;
			}
		}

		i.add( left );
		return null;
	}

	@Override
	public ItemStack simulateAdd(ItemStack A)
	{
		return null;
	}

	@Override
	public boolean containsItems()
	{

		int s = i.size();
		for (int x = 0; x < s; x++)
		{
			ItemStack is = i.get( x );
			if ( is != null )
				return true;
		}
		return false;
	}

	@Override
	public Iterator<ItemSlot> iterator()
	{
		return new StackToSlotIterator( i.iterator() );
	}

}
