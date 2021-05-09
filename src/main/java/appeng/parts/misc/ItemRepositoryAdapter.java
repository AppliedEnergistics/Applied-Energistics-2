package appeng.parts.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import appeng.api.storage.IStorageChannel;

import appeng.core.AELog;
import com.jaquadro.minecraft.storagedrawers.api.capabilities.IItemRepository;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.me.GridAccessException;
import appeng.me.helpers.IGridProxyable;
import appeng.me.storage.ITickingMonitor;
import appeng.util.item.AEItemStack;

import javax.annotation.Nonnull;

/**
 * Wraps an Item Repository in such a way that it can be used as an IMEInventory for items.
 * Used by the Storage Bus
 */

class ItemRepositoryAdapter implements IMEInventory<IAEItemStack>, IBaseMonitor<IAEItemStack>, ITickingMonitor
{
    private final Map<IMEMonitorHandlerReceiver<IAEItemStack>, Object> listeners = new HashMap<>();
    private IActionSource mySource;
    private final IItemRepository itemRepository;
    private final IGridProxyable proxyable;
    private final InventoryCache cache;


    ItemRepositoryAdapter( IItemRepository itemRepository, IGridProxyable proxy )
    {
        this.itemRepository = itemRepository;
        this.proxyable = proxy;
        this.cache = new InventoryCache( this.itemRepository );
    }

    @Override
    public IAEItemStack injectItems( IAEItemStack iox, Actionable type, IActionSource src )
    {
        ItemStack orgInput = iox.createItemStack();
        ItemStack remaining = orgInput;

        boolean simulate = ( type == Actionable.SIMULATE );

        remaining = this.itemRepository.insertItem( remaining, simulate );

        // At this point, we still have some items left...
        if( remaining == orgInput )
        {
            // The stack remained unmodified, target inventory is full
            return iox;
        }

        if( type == Actionable.MODULATE )
        {
            if (this.cache.cachedAeStacks.length == 0) this.cache.update();
            boolean found = false;
            for (IAEItemStack iaeItemStack : this.cache.cachedAeStacks)
            {
                if( iaeItemStack == null )
                {
                    continue;
                }
                if( iaeItemStack.equals( iox ) )
                {
                    found = true;
                    iaeItemStack.incStackSize( iox.getStackSize() );
                }
            }
            if (!found)
            {
                this.cache.cachedAeStacks = Arrays.copyOf( this.cache.cachedAeStacks, this.cache.cachedAeStacks.length + 1 );
                this.cache.cachedAeStacks[this.cache.cachedAeStacks.length - 1] = iox.copy();
            }
        }

        return AEItemStack.fromItemStack( remaining );

    }

    @Override
    public IAEItemStack extractItems( IAEItemStack request, Actionable mode, IActionSource src )
    {
        ItemStack requestedItemStack = request.createItemStack();
        int remainingSize = requestedItemStack.getCount();

        final boolean simulate = ( mode == Actionable.SIMULATE );

        ItemStack extracted;
        extracted = this.itemRepository.extractItem( requestedItemStack, remainingSize, simulate );

        if( extracted.getCount() > remainingSize )
        {
            // Something broke. It should never return more than we requested...
            // We're going to silently eat the remainder
            AELog.warn( "Mod that provided item handler %s is broken. Returned %s items while only requesting %d.",
                    this.itemRepository.getClass().getName(), extracted.toString(), remainingSize );
            extracted.setCount( remainingSize );
        }

        if( !extracted.isEmpty() )
        {
            if (this.cache.cachedAeStacks.length == 0) this.cache.update();
            IAEItemStack iaeExtracted = AEItemStack.fromItemStack( extracted );

            if( mode == Actionable.MODULATE )
            {
                for ( int i = 0; i < this.cache.cachedAeStacks.length; i++ )
                {
                    IAEItemStack iaeItemStack = this.cache.cachedAeStacks[i];
                    if( iaeExtracted.equals( iaeItemStack ) )
                    {
                        if( iaeExtracted.getStackSize() >= iaeItemStack.getStackSize() )
                        {
                            iaeExtracted.decStackSize( iaeItemStack.getStackSize() );
                            this.cache.cachedAeStacks[i] = null;
                        }
                        else
                        {
                            iaeItemStack.decStackSize( iaeExtracted.getStackSize() );
                            break;
                        }
                    }
                }
            }

            return AEItemStack.fromItemStack( extracted );
        }

        return null;
    }

    @Override
    public IItemList<IAEItemStack> getAvailableItems( IItemList<IAEItemStack> out )
    {
        return this.cache.getAvailableItems( out );
    }

    @Override
    public IStorageChannel<IAEItemStack> getChannel()
    {
        return AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class );
    }

    @Override
    public void addListener( IMEMonitorHandlerReceiver<IAEItemStack> l, Object verificationToken )
    {
        this.listeners.put( l, verificationToken );
    }

    @Override
    public void removeListener( IMEMonitorHandlerReceiver<IAEItemStack> l )
    {
        this.listeners.remove( l );
    }

    private void postDifference( Iterable<IAEItemStack> a )
    {
        final Iterator<Map.Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object>> i = this.listeners.entrySet().iterator();
        while( i.hasNext() )
        {
            final Map.Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object> l = i.next();
            final IMEMonitorHandlerReceiver<IAEItemStack> key = l.getKey();
            if( key.isValid( l.getValue() ) )
            {
                key.postChange( this, a, this.mySource );
            }
            else
            {
                i.remove();
            }
        }
    }

    @Override
    public TickRateModulation onTick()
    {
        {
            List<IAEItemStack> changes = this.cache.update();
            if( !changes.isEmpty() )
            {
                this.postDifference( changes );
                return TickRateModulation.URGENT;
            }
            else
            {
                return TickRateModulation.SLOWER;
            }
        }
    }

    @Override
    public void setActionSource( final IActionSource mySource )
    {
        this.mySource = mySource;
    }

    private static class InventoryCache
    {
        private IAEItemStack[] cachedAeStacks = new IAEItemStack[0];
        private final IItemRepository iItemRepository;

        public InventoryCache( IItemRepository iItemRepository )
        {
            this.iItemRepository = iItemRepository;
        }

        public IItemList<IAEItemStack> getAvailableItems( IItemList<IAEItemStack> out )
        {
            Arrays.stream( this.cachedAeStacks ).forEach( out::add );
            return out;
        }

        public List<IAEItemStack> update()
        {
            final List<IAEItemStack> changes = new ArrayList<>();

            List<IAEItemStack> out = this.iItemRepository.getAllItems().stream().map( s -> AEItemStack.fromItemStack( s.itemPrototype ).setStackSize( s.count ) ).collect(Collectors.toList() );

            final int size = out.size();

            // Make room for new slots
            if( size > this.cachedAeStacks.length )
            {
                this.cachedAeStacks = Arrays.copyOf( this.cachedAeStacks, size );
            }

            for( int x = 0; x < size; x++ )
            {
                // Save the old stuff
                final IAEItemStack oldAeIS = this.cachedAeStacks[x];
                final IAEItemStack newIS = out.get( x );

                this.handlePossibleSlotChanges( x, oldAeIS, newIS, changes );
            }

            // Handle cases where the number of slots actually is lower now than before
            if( size < this.cachedAeStacks.length )
            {
                for( int x = 0; x < this.cachedAeStacks.length; x++ )
                {
                    final IAEItemStack aeStack = this.cachedAeStacks[x];

                    if( aeStack != null )
                    {
                        final IAEItemStack a = aeStack.copy();
                        a.setStackSize( -a.getStackSize() );
                        changes.add( a );
                    }
                }

                // Reduce the cache size
                this.cachedAeStacks = Arrays.copyOf( this.cachedAeStacks, size );
            }

            return changes;
        }

        private void handlePossibleSlotChanges( int slot, IAEItemStack oldAeIS, IAEItemStack newIS, List<IAEItemStack> changes )
        {
            if( oldAeIS != null && oldAeIS.isSameType( newIS ) )
            {
                this.handleStackSizeChanged( slot, oldAeIS, newIS, changes );
            }
            else
            {
                this.handleItemChanged( slot, oldAeIS, newIS, changes );
            }
        }

        private void handleStackSizeChanged( int slot, IAEItemStack oldAeIS, IAEItemStack newIS, List<IAEItemStack> changes )
        {
            // Still the same item, but amount might have changed
            final long diff = newIS.getStackSize() - oldAeIS.getStackSize();

            if( diff != 0 )
            {
                final IAEItemStack stack = oldAeIS.copy();
                stack.setStackSize( newIS.getStackSize() );

                this.cachedAeStacks[slot] = stack;

                final IAEItemStack a = stack.copy();
                a.setStackSize( diff );
                changes.add( a );
            }
        }

        private void handleItemChanged( int slot, IAEItemStack oldAeIS, IAEItemStack newIS, List<IAEItemStack> changes )
        {
            // Completely different item
            this.cachedAeStacks[slot] =  newIS ;

            // If we had a stack previously in this slot, notify the network about its disappearance
            if( oldAeIS != null )
            {
                oldAeIS.setStackSize( -oldAeIS.getStackSize() );
                changes.add( oldAeIS );
            }

            // Notify the network about the new stack. Note that this is null if newIS was null
            if( this.cachedAeStacks[slot] != null )
            {
                changes.add( this.cachedAeStacks[slot] );
            }
        }
    }
}
