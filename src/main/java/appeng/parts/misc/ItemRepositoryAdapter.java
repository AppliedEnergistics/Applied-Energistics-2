package appeng.parts.misc;

import javax.annotation.Nullable;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.me.GridAccessException;
import appeng.me.helpers.IGridProxyable;
import appeng.me.storage.ITickingMonitor;
import appeng.util.item.AEItemStack;
import com.google.common.primitives.Ints;
import com.jaquadro.minecraft.storagedrawers.api.capabilities.IItemRepository;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Wraps an Item Repository in such a way that it can be used as an IMEInventory for items.
 * Used by the Storage Bus
 */

class ItemRepositoryAdapter implements IMEInventory<IAEItemStack>, IBaseMonitor<IAEItemStack>, ITickingMonitor
{
    private final Object2ObjectMap<IMEMonitorHandlerReceiver<IAEItemStack>, Object> listeners = new Object2ObjectOpenHashMap<>();
    private IActionSource mySource;
    private final IItemRepository itemRepository;
    private final IGridProxyable proxyable;
    private final InventoryCache cache;

    private ItemStack stackCache;

    ItemRepositoryAdapter( IItemRepository itemRepository, IGridProxyable proxy )
    {
        this.itemRepository = itemRepository;
        this.proxyable = proxy;
        this.cache = new InventoryCache( this.itemRepository );
    }

    @Override
    public IAEItemStack injectItems( IAEItemStack iox, Actionable type, IActionSource src )
    {
        // Try to reuse the cached stack
        @Nullable ItemStack currentCached = stackCache;
        stackCache = null;

        ItemStack orgInput;
        if( currentCached != null && iox.isSameType( currentCached ) )
        {
            // Cache is suitable, just update the count
            orgInput = currentCached;
            currentCached.setCount( Ints.saturatedCast( iox.getStackSize() ) );
        }
        else
        {
            // We need a new stack :-(
            orgInput = iox.createItemStack();
        }
        ItemStack remaining = this.itemRepository.insertItem( orgInput, type == Actionable.SIMULATE );

        // Store the stack in the cache for next time.
        if (!remaining.isEmpty() && remaining != orgInput)
        {
            stackCache = remaining;
        }

        // At this point, we still have some items left...
        if( remaining == orgInput )
        {
            // The stack remained unmodified, target inventory is full
            return iox;
        }

        if( type == Actionable.MODULATE )
        {
            try
            {
                this.proxyable.getProxy().getTick().alertDevice( this.proxyable.getProxy().getNode() );
            }
            catch( GridAccessException ex )
            {
                // meh
            }
        }

        return AEItemStack.fromItemStack( remaining );

    }

    @Override
    public IAEItemStack extractItems( IAEItemStack request, Actionable mode, IActionSource src )
    {
        int remainingSize = Ints.saturatedCast(request.getStackSize());

        final boolean simulate = ( mode == Actionable.SIMULATE );

        ItemStack extracted = this.itemRepository.extractItem( request.getDefinition(), remainingSize, simulate );

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
            if( mode == Actionable.MODULATE )
            {
                try
                {
                    this.proxyable.getProxy().getTick().alertDevice( this.proxyable.getProxy().getNode() );
                }
                catch( GridAccessException ex )
                {
                    // meh
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
        while ( i.hasNext() )
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

            List<IAEItemStack> out = this.iItemRepository.getAllItems().stream().map( s -> AEItemStack.fromItemStack( s.itemPrototype ).setStackSize( s.count ) ).collect( Collectors.toList() );

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
