package appeng.util.inv;

import java.util.Iterator;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;

public class AdaptorIInventory extends InventoryAdaptor
{

	private IInventory i;
	private boolean wrapperEnabled;

	public AdaptorIInventory(IInventory s) {
		i = s;
		wrapperEnabled = s instanceof IInventoryWrapper;
	}

	boolean canRemoveStackFromSlot(int x, ItemStack is)
	{
		if ( wrapperEnabled )
			return ((IInventoryWrapper) i).canRemoveItemFromSlot( x, is );
		return true;
	}

	@Override
	public boolean containsItems()
	{
		int s = i.getSizeInventory();
		for (int x = 0; x < s; x++)
		{
			if ( i.getStackInSlot( x ) != null )
				return true;
		}
		return false;
	}

	@Override
	public ItemStack removeSimilarItems(int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination)
	{
		int s = i.getSizeInventory();
		for (int x = 0; x < s; x++)
		{
			ItemStack is = i.getStackInSlot( x );
			if ( is != null && canRemoveStackFromSlot( x, is ) && (filter == null || Platform.isSameItemFuzzy( is, filter, fuzzyMode )) )
			{
				int newAmount = amount;
				if ( newAmount > is.stackSize )
					newAmount = is.stackSize;
				if ( destination != null && !destination.canInsert( is ) )
					newAmount = 0;

				ItemStack rv = null;
				if ( newAmount > 0 )
				{
					rv = is.copy();
					rv.stackSize = newAmount;

					if ( is.stackSize == rv.stackSize )
					{
						i.setInventorySlotContents( x, null );
						i.markDirty();
					}
					else
					{
						ItemStack po = is.copy();
						po.stackSize -= rv.stackSize;
						i.setInventorySlotContents( x, po );
						i.markDirty();
					}
				}

				if ( rv != null )
				{
					// i.markDirty();
					return rv;
				}
			}
		}
		return null;
	}

	@Override
	public ItemStack simulateSimilarRemove(int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination)
	{
		int s = i.getSizeInventory();
		for (int x = 0; x < s; x++)
		{
			ItemStack is = i.getStackInSlot( x );

			if ( is != null && canRemoveStackFromSlot( x, is ) && (filter == null || Platform.isSameItemFuzzy( is, filter, fuzzyMode )) )
			{
				int boundAmount = amount;
				if ( boundAmount > is.stackSize )
					boundAmount = is.stackSize;
				if ( destination != null && !destination.canInsert( is ) )
					boundAmount = 0;

				if ( boundAmount > 0 )
				{
					ItemStack rv = is.copy();
					rv.stackSize = boundAmount;
					return rv;
				}
			}
		}
		return null;
	}

	@Override
	public ItemStack removeItems(int amount, ItemStack filter, IInventoryDestination destination)
	{
		int s = i.getSizeInventory();
		ItemStack rv = null;

		for (int x = 0; x < s && amount > 0; x++)
		{
			ItemStack is = i.getStackInSlot( x );
			if ( is != null && canRemoveStackFromSlot( x, is ) && (filter == null || Platform.isSameItemPrecise( is, filter )) )
			{
				int boundAmounts = amount;
				if ( boundAmounts > is.stackSize )
					boundAmounts = is.stackSize;
				if ( destination != null && !destination.canInsert( is ) )
					boundAmounts = 0;

				if ( boundAmounts > 0 )
				{
					if ( rv == null )
					{
						rv = is.copy();
						filter = rv;
						rv.stackSize = boundAmounts;
						amount -= boundAmounts;
					}
					else
					{
						rv.stackSize += boundAmounts;
						amount -= boundAmounts;
					}

					if ( is.stackSize == boundAmounts )
					{
						i.setInventorySlotContents( x, null );
						i.markDirty();
					}
					else
					{
						ItemStack po = is.copy();
						po.stackSize -= boundAmounts;
						i.setInventorySlotContents( x, po );
						i.markDirty();
					}
				}
			}
		}

		// if ( rv != null )
		// i.markDirty();

		return rv;
	}

	@Override
	public ItemStack simulateRemove(int amount, ItemStack filter, IInventoryDestination destination)
	{
		int s = i.getSizeInventory();
		ItemStack rv = null;

		for (int x = 0; x < s && amount > 0; x++)
		{
			ItemStack is = i.getStackInSlot( x );
			if ( is != null && canRemoveStackFromSlot( x, is ) && (filter == null || Platform.isSameItemPrecise( is, filter )) )
			{
				int boundAount = amount;
				if ( boundAount > is.stackSize )
					boundAount = is.stackSize;
				if ( destination != null && !destination.canInsert( is ) )
					boundAount = 0;

				if ( boundAount > 0 )
				{
					if ( rv == null )
					{
						rv = is.copy();
						rv.stackSize = boundAount;
						amount -= boundAount;
					}
					else
					{
						rv.stackSize += boundAount;
						amount -= boundAount;
					}
				}
			}
		}

		return rv;
	}

	@Override
	public ItemStack addItems(ItemStack A)
	{
		if ( A == null )
			return null;
		if ( A.stackSize == 0 )
			return null;

		ItemStack left = A.copy();

		int stack_limit = A.getMaxStackSize();
		if ( stack_limit > i.getInventoryStackLimit() )
			stack_limit = i.getInventoryStackLimit();

		int s = i.getSizeInventory();
		for (int pass = 0; pass < 2; pass++)
		{
			for (int x = 0; x < s; x++)
			{
				if ( i.isItemValidForSlot( x, A ) )
				{
					ItemStack is = i.getStackInSlot( x );
					if ( is == null && pass != 0 )
					{
						ItemStack thisSlot = left.copy();
						if ( thisSlot.stackSize > stack_limit )
							thisSlot.stackSize = stack_limit;
						left.stackSize -= thisSlot.stackSize;

						i.setInventorySlotContents( x, thisSlot );

						if ( left.stackSize <= 0 )
						{
							// i.markDirty();
							return null;
						}
					}
					else if ( is != null )
					{
						if ( Platform.isSameItemPrecise( is, left ) )
						{
							if ( is.stackSize < stack_limit )
							{
								int room = stack_limit - is.stackSize;
								int used = left.stackSize;
								if ( used > room )
									used = room;

								is.stackSize += used;
								i.setInventorySlotContents( x, is );
								i.markDirty();

								left.stackSize -= used;
								if ( left.stackSize <= 0 )
								{
									// i.markDirty();
									return null;
								}
							}
						}
					}
				}
			}
		}

		// if ( left.stackSize != A.stackSize )
		// i.markDirty();

		return left;
	}

	@Override
	public ItemStack simulateAdd(ItemStack A)
	{
		if ( A == null )
			return A;
		ItemStack left = A.copy();

		int stack_limit = A.getMaxStackSize();
		if ( stack_limit > i.getInventoryStackLimit() )
			stack_limit = i.getInventoryStackLimit();

		int s = i.getSizeInventory();
		for (int x = 0; x < s; x++)
		{
			if ( i.isItemValidForSlot( x, A ) )
			{
				ItemStack is = i.getStackInSlot( x );
				if ( is == null )
				{
					ItemStack thisSlot = left.copy();
					if ( thisSlot.stackSize > stack_limit )
						thisSlot.stackSize = stack_limit;
					left.stackSize -= thisSlot.stackSize;

					if ( left.stackSize <= 0 )
					{
						return null;
					}
				}
				else
				{
					if ( Platform.isSameItemPrecise( is, left ) )
					{
						if ( is.stackSize < stack_limit )
						{
							int room = stack_limit - is.stackSize;
							int used = left.stackSize;
							if ( used > room )
								used = room;

							left.stackSize -= used;
							if ( left.stackSize <= 0 )
							{
								return null;
							}
						}
					}
				}
			}
		}

		return left;
	}

	class InvIterator implements Iterator<ItemSlot>
	{

		final ItemSlot is = new ItemSlot();
		int x = 0;

		@Override
		public boolean hasNext()
		{
			return x < i.getSizeInventory();
		}

		@Override
		public ItemSlot next()
		{
			ItemStack iss = i.getStackInSlot( x );

			is.isExtractable = canRemoveStackFromSlot( x, iss );
			is.setItemStack( iss );

			is.slot = x++;
			return is;
		}

		@Override
		public void remove()
		{
			// nothing!
		}

	}

	@Override
	public Iterator<ItemSlot> iterator()
	{
		return new InvIterator();
	}

}
