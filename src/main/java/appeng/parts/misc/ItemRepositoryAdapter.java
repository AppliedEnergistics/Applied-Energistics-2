package appeng.parts.misc;

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
import com.jaquadro.minecraft.storagedrawers.api.capabilities.IItemRepository;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        ItemStack requestedItemStack = request.getDefinition();
        int remainingSize = (int) Math.min( Integer.MAX_VALUE, request.getStackSize() );

        final boolean simulate = ( mode == Actionable.SIMULATE );

        ItemStack extracted = this.itemRepository.extractItem( requestedItemStack, remainingSize, simulate );

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
        private IItemList<IAEItemStack> cachedAeStacks = AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createList();
        private final IItemRepository iItemRepository;

        public InventoryCache( IItemRepository iItemRepository )
        {
            this.iItemRepository = iItemRepository;
        }

        public IItemList<IAEItemStack> getAvailableItems( IItemList<IAEItemStack> out )
        {
            this.cachedAeStacks.forEach( out::add );
            return out;
        }

        public List<IAEItemStack> update()
        {
            final List<IAEItemStack> changes = new ArrayList<>();

            IItemList<IAEItemStack> storage = AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createList();
            this.iItemRepository.getAllItems().stream().map( s -> AEItemStack.fromItemStack( s.itemPrototype ).setStackSize( s.count ) ).forEach( storage::add );

            Iterator<IAEItemStack> cachedAeStacksIterator = cachedAeStacks.iterator();
            while ( cachedAeStacksIterator.hasNext() )
            {
                IAEItemStack cachedStack = cachedAeStacksIterator.next();
                IAEItemStack storedStack = storage.findPrecise( cachedStack );
                if( storedStack == null )
                {
                    changes.add( cachedStack.setStackSize( -cachedStack.getStackSize() ) );
                    cachedAeStacksIterator.remove();
                }
                else if( cachedStack.getStackSize() != storedStack.getStackSize() )
                {
                    handleStackSizeChanged( cachedStack, storedStack, changes );
                }
            }

            for( IAEItemStack storedStack : storage )
            {
                if( cachedAeStacks.findPrecise( storedStack ) == null )
                {
                    cachedAeStacks.add( storedStack );
                    changes.add( storedStack.copy() );
                }
            }

            return changes;
        }

        private void handleStackSizeChanged( IAEItemStack cachedStack, IAEItemStack storedStack, List<IAEItemStack> changes )
        {
            // Still the same item, but amount might have changed
            final long diff = storedStack.getStackSize() - cachedStack.getStackSize();

            if( diff != 0 )
            {
                cachedStack.setStackSize( storedStack.getStackSize() );

                final IAEItemStack diffStack = cachedStack.copy();
                diffStack.setStackSize( diff );
                changes.add( diffStack );
            }
        }

    }
}
