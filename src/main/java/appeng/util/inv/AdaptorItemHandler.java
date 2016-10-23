package appeng.util.inv;


import java.util.Iterator;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;


public class AdaptorItemHandler extends InventoryAdaptor
{
	private final IItemHandler itemHandler;

	public AdaptorItemHandler( IItemHandler itemHandler )
	{
		this.itemHandler = itemHandler;
	}

	@Override
	public ItemStack removeItems( int amount, ItemStack filter, IInventoryDestination destination )
	{
		int slots = itemHandler.getSlots();
		ItemStack rv = null;

		for( int slot = 0; slot < slots && amount > 0; slot++ )
		{
			final ItemStack is = itemHandler.getStackInSlot( slot );
			if( is == null || ( filter != null && !Platform.isSameItemPrecise( is, filter ) ) )
			{
				continue;
			}

			if( destination != null )
			{
				ItemStack extracted = itemHandler.extractItem( slot, amount, true );
				if( extracted == null )
				{
					continue;
				}

				if( !destination.canInsert( extracted ) )
				{
					continue;
				}
			}

			// Attempt extracting it
			ItemStack extracted = itemHandler.extractItem( slot, amount, false );

			if( extracted == null )
			{
				continue;
			}

			if( rv == null )
			{
				// Use the first stack as a template for the result
				rv = extracted;
				filter = extracted;
				amount -= extracted.stackSize;
			}
			else
			{
				// Subsequent stacks will just increase the extracted size
				rv.stackSize += extracted.stackSize;
				amount -= extracted.stackSize;
			}
		}

		return rv;
	}

	@Override
	public ItemStack simulateRemove( int amount, ItemStack filter, IInventoryDestination destination )
	{
		int slots = itemHandler.getSlots();
		ItemStack rv = null;

		for( int slot = 0; slot < slots && amount > 0; slot++ )
		{
			final ItemStack is = itemHandler.getStackInSlot( slot );
			if( is != null && ( filter == null || Platform.isSameItemPrecise( is, filter ) ) )
			{
				ItemStack extracted = itemHandler.extractItem( slot, amount, true );

				if( extracted == null )
				{
					continue;
				}

				if( destination != null )
				{
					if( !destination.canInsert( extracted ) )
					{
						continue;
					}
				}

				if( rv == null )
				{
					// Use the first stack as a template for the result
					rv = extracted.copy();
					filter = extracted;
					amount -= extracted.stackSize;
				}
				else
				{
					// Subsequent stacks will just increase the extracted size
					rv.stackSize += extracted.stackSize;
					amount -= extracted.stackSize;
				}
			}
		}

		return rv;
	}

	/**
	 * For fuzzy extract, we will only ever extract one slot, since we're afraid of merging two item stacks with different damage values.
	 */
	@Override
	public ItemStack removeSimilarItems( int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination )
	{
		int slots = itemHandler.getSlots();
		ItemStack extracted = null;

		for( int slot = 0; slot < slots && extracted == null; slot++ )
		{
			final ItemStack is = itemHandler.getStackInSlot( slot );
			if( is == null || ( filter != null && !Platform.isSameItemFuzzy( is, filter, fuzzyMode ) ) )
			{
				continue;
			}

			if( destination != null )
			{
				ItemStack simulated = itemHandler.extractItem( slot, amount, true );
				if( simulated == null )
				{
					continue;
				}

				if( !destination.canInsert( simulated ) )
				{
					continue;
				}
			}

			// Attempt extracting it
			extracted = itemHandler.extractItem( slot, amount, false );
		}

		return extracted;
	}

	@Override
	public ItemStack simulateSimilarRemove( int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination )
	{
		int slots = itemHandler.getSlots();
		ItemStack extracted = null;

		for( int slot = 0; slot < slots && extracted == null; slot++ )
		{
			final ItemStack is = itemHandler.getStackInSlot( slot );
			if( is == null || ( filter != null && !Platform.isSameItemFuzzy( is, filter, fuzzyMode ) ) )
			{
				continue;
			}

			// Attempt extracting it
			extracted = itemHandler.extractItem( slot, amount, true );

			if( extracted != null && destination != null )
			{
				if( !destination.canInsert( extracted ) )
				{
					extracted = null; // Keep on looking...
				}
			}
		}

		return extracted;
	}

	@Override
	public ItemStack addItems( ItemStack toBeAdded )
	{
		return addItems( toBeAdded, false );
	}

	@Override
	public ItemStack simulateAdd( ItemStack toBeSimulated )
	{
		return addItems( toBeSimulated, true );
	}

	private ItemStack addItems( final ItemStack itemsToAdd, final boolean simulate )
	{
		if( itemsToAdd == null || itemsToAdd.stackSize == 0 )
		{
			return null;
		}

		ItemStack left = itemsToAdd.copy();

		for( int slot = 0; slot < itemHandler.getSlots(); slot++ )
		{
			ItemStack is = itemHandler.getStackInSlot( slot );

			if( is == null || Platform.isSameItemPrecise( is, left ) )
			{
				left = itemHandler.insertItem( slot, left, simulate );

				if( left == null || left.stackSize <= 0 )
				{
					return null;
				}
			}
		}

		return left;
	}

	@Override
	public boolean containsItems()
	{
		int slots = itemHandler.getSlots();
		for( int slot = 0; slot < slots; slot++ )
		{
			if( itemHandler.getStackInSlot( slot ) != null )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<ItemSlot> iterator()
	{
		return new ItemHandlerIterator( itemHandler );
	}
}
