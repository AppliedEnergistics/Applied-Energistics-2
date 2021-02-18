package appeng.util.inv;

import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;

import appeng.util.Platform;
import com.jaquadro.minecraft.storagedrawers.api.capabilities.IItemRepository;
import net.minecraft.item.ItemStack;

import java.util.Iterator;


public class AdaptorItemRepository extends InventoryAdaptor
{
    protected final IItemRepository itemRepository;

    public AdaptorItemRepository( IItemRepository itemRepository )
    {
        this.itemRepository = itemRepository;
    }

    @Override
    public ItemStack removeItems( int amount, ItemStack filter, IInventoryDestination destination )
    {
        ItemStack rv = ItemStack.EMPTY;

        ItemStack extracted = this.itemRepository.extractItem( filter, amount, true );

        if( destination != null )
        {

            if( extracted.isEmpty() || !destination.canInsert( extracted ) )
            {
                return rv;
            }

        }

        extracted = this.itemRepository.extractItem( filter, amount, false );
        return extracted;
    }

    @Override
    public ItemStack simulateRemove( int amount, ItemStack filter, IInventoryDestination destination )
    {
        ItemStack rv = ItemStack.EMPTY;

        ItemStack extracted = this.itemRepository.extractItem( filter, amount, true );

        if( destination != null )
        {
            if( extracted.isEmpty() || !destination.canInsert( extracted ) )
            {
                return rv;
            }
        }

        return extracted;
    }

    @Override
    public ItemStack removeSimilarItems( int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination )
    {
        ItemStack extracted = ItemStack.EMPTY;

        ItemStack simulated = this.itemRepository.extractItem( filter, amount, true );

        if (simulated.isEmpty() && !filter.isEmpty()) {
            simulated = this.itemRepository.extractItem( filter, amount, true, s -> Platform.itemComparisons().isFuzzyEqualItem( s, filter, fuzzyMode ) );
        }

        if( destination != null )
        {
            if( simulated.isEmpty() || !destination.canInsert( simulated ) )
            {
                return extracted;
            }
        }

        extracted = this.itemRepository.extractItem( simulated, amount, false );
        return extracted;
    }

    @Override
    public ItemStack simulateSimilarRemove( int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination )
    {
        ItemStack extracted = ItemStack.EMPTY;

        ItemStack simulated = this.itemRepository.extractItem( filter, amount, true );

        if (simulated.isEmpty() && !filter.isEmpty()) {
            simulated = this.itemRepository.extractItem( filter, amount, true, s -> Platform.itemComparisons().isFuzzyEqualItem( s, filter, fuzzyMode ) );
        }

        if( destination != null )
        {
            if( simulated.isEmpty() || !destination.canInsert( simulated ) )
            {
                return extracted;
            }
        }

        return simulated;
    }

    @Override
    public ItemStack addItems( ItemStack toBeAdded )
    {
        return this.addItems( toBeAdded, false );
    }

    protected ItemStack addItems( final ItemStack itemsToAdd, final boolean simulate )
    {
        if( itemsToAdd.isEmpty() )
        {
            return ItemStack.EMPTY;
        }

        ItemStack left = itemsToAdd.copy();

        left = this.itemRepository.insertItem( left, simulate );

        if( left.isEmpty() )
        {
            return ItemStack.EMPTY;
        }

        return left;
    }

    @Override
    public ItemStack simulateAdd( ItemStack toBeSimulated )
    {
        return this.addItems( toBeSimulated, true );
    }

    @Override
    public boolean containsItems()
    {
        return !this.itemRepository.getAllItems().isEmpty();
    }

    @Override
    public boolean hasSlots()
    {
        return false;
    }

    @Override
    public Iterator<ItemSlot> iterator()
    {
        return null;
    }
}
