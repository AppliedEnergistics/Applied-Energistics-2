package appeng.integration.modules.helpers;

import java.util.Iterator;

import net.mcft.copy.betterstorage.api.crate.ICrateStorage;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.ItemSlot;
import appeng.util.iterators.StackToSlotIterator;

public class BSCrateStorageAdaptor extends InventoryAdaptor
{

	ICrateStorage cs;
	ForgeDirection side;

	public BSCrateStorageAdaptor(Object te, ForgeDirection d) {
		cs = (ICrateStorage) te;
		side = d;
	}

	@Override
	public ItemStack removeItems(int how_many, ItemStack Filter, IInventoryDestination dest)
	{
		ItemStack target = null;

		for (ItemStack is : cs.getContents())
		{
			if ( is != null )
			{
				if ( is.stackSize > 0 && (Filter == null || Platform.isSameItem( Filter, is )) )
				{
					if ( dest == null || dest.canInsert( is ) )
					{
						target = is;
						break;
					}
				}
			}
		}

		if ( target != null )
		{
			ItemStack f = Platform.cloneItemStack( target );
			f.stackSize = how_many;
			return cs.extractItems( f, how_many );
		}

		return null;
	}

	@Override
	public ItemStack simulateRemove(int how_many, ItemStack Filter, IInventoryDestination dest)
	{
		ItemStack target = null;

		for (ItemStack is : cs.getContents())
		{
			if ( is != null )
			{
				if ( is.stackSize > 0 && (Filter == null || Platform.isSameItem( Filter, is )) )
				{
					if ( dest == null || dest.canInsert( is ) )
					{
						target = is;
						break;
					}
				}
			}
		}

		if ( target != null )
		{
			int cnt = cs.getItemCount( target );
			if ( cnt == 0 )
				return null;
			if ( cnt > how_many )
				cnt = how_many;
			ItemStack c = target.copy();
			c.stackSize = cnt;
			return c;
		}

		return null;
	}

	@Override
	public ItemStack removeSimilarItems(int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination dest)
	{
		ItemStack target = null;

		for (ItemStack is : cs.getContents())
		{
			if ( is != null )
			{
				if ( is.stackSize > 0 && (filter == null || Platform.isSameItemFuzzy( filter, is, fuzzyMode )) )
				{
					if ( dest == null || dest.canInsert( is ) )
					{
						target = is;
						break;
					}
				}
			}
		}

		if ( target != null )
		{
			ItemStack f = Platform.cloneItemStack( target );
			f.stackSize = amount;
			return cs.extractItems( f, amount );
		}

		return null;
	}

	@Override
	public ItemStack simulateSimilarRemove(int how_many, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination dest)
	{
		ItemStack target = null;

		for (ItemStack is : cs.getContents())
		{
			if ( is != null )
			{
				if ( is.stackSize > 0 && (filter == null || Platform.isSameItemFuzzy( filter, is, fuzzyMode )) )
				{
					if ( dest == null || dest.canInsert( is ) )
					{
						target = is;
						break;
					}
				}
			}
		}

		if ( target != null )
		{
			int cnt = cs.getItemCount( target );
			if ( cnt == 0 )
				return null;
			if ( cnt > how_many )
				cnt = how_many;
			ItemStack c = target.copy();
			c.stackSize = cnt;
			return c;
		}

		return null;
	}

	@Override
	public ItemStack addItems(ItemStack A)
	{
		return cs.insertItems( A );
	}

	@Override
	public ItemStack simulateAdd(ItemStack A)
	{
		int items = cs.getSpaceForItem( A );
		ItemStack B = Platform.cloneItemStack( A );
		if ( A.stackSize <= items )
			return null;
		B.stackSize -= items;
		return B;
	}

	@Override
	public boolean containsItems()
	{
		return cs.getUniqueItems() > 0;
	}

	@Override
	public Iterator<ItemSlot> iterator()
	{
		return new StackToSlotIterator( cs.getContents().iterator() );
	}

}
