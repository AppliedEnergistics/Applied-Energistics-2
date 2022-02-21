package appeng.parts.misc;

import javax.annotation.Nullable;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.Settings;
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
	private AccessRestriction access;

	ItemRepositoryAdapter( IItemRepository itemRepository, IGridProxyable proxy )
	{
		this.itemRepository = itemRepository;
		this.proxyable = proxy;
		this.cache = new InventoryCache( this.itemRepository );
		if( this.proxyable instanceof PartStorageBus )
		{
			PartStorageBus partStorageBus = (PartStorageBus) this.proxyable;
			this.access = ( (AccessRestriction) partStorageBus.getConfigManager().getSetting( Settings.ACCESS ) );
		}
		this.cache.update();
	}

	@Override
	public IAEItemStack injectItems( IAEItemStack iox, Actionable type, IActionSource src )
	{
		// Try to reuse the cached stack
		ItemStack inputStack = iox.getCachedItemStack( iox.getStackSize() );

		ItemStack remaining;

		remaining = this.itemRepository.insertItem( inputStack, type == Actionable.SIMULATE );

		// Store the stack in the cache for next time.
		if( type == Actionable.SIMULATE )
		{
			iox.setCachedItemStack( inputStack );
		}
		else
		{
			if( !remaining.isEmpty() )
			{
				iox.setCachedItemStack( remaining );
			}
		}

		// At this point, we still have some items left...
		if( remaining == inputStack )
		{
			// The stack remained unmodified, target inventory is full
			return iox;
		}

		if( type == Actionable.MODULATE )
		{
			IAEItemStack added = iox.copy().setStackSize( iox.getStackSize() - remaining.getCount() );
			this.cache.currentlyCached.add( added );
			this.postDifference( Collections.singletonList( added ) );
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
		int remainingSize = Ints.saturatedCast( request.getStackSize() );

		final boolean simulate = ( mode == Actionable.SIMULATE );

		ItemStack extracted = this.itemRepository.extractItem( request.getDefinition(), remainingSize, simulate );

		if( extracted.getCount() > remainingSize )
		{
			// Something broke. It should never return more than we requested...
			// We're going to silently eat the remainder
			AELog.warn( "Mod that provided item handler %s is broken. Returned %s items while only requesting %d.", this.itemRepository.getClass().getName(), extracted.toString(), remainingSize );
			extracted.setCount( remainingSize );
		}

		if( !extracted.isEmpty() )
		{
			IAEItemStack extractedAEItemStack = AEItemStack.fromItemStack( extracted );
			if( mode == Actionable.MODULATE )
			{
				IAEItemStack cachedStack = this.cache.currentlyCached.findPrecise( request );
				if( cachedStack != null )
				{
					cachedStack.decStackSize( extractedAEItemStack.getStackSize() );
					this.postDifference( Collections.singletonList( extractedAEItemStack.copy().setStackSize( -extractedAEItemStack.getStackSize() ) ) );
				}
				try
				{
					this.proxyable.getProxy().getTick().alertDevice( this.proxyable.getProxy().getNode() );
				}
				catch( GridAccessException ex )
				{
					// meh
				}
			}
			return extractedAEItemStack;
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
		List<IAEItemStack> changes = this.cache.update();
		if( !changes.isEmpty() && access.hasPermission( AccessRestriction.READ ) )
		{
			this.postDifference( changes );
			return TickRateModulation.URGENT;
		}
		else
		{
			return TickRateModulation.SLOWER;
		}
	}

	@Override
	public void setActionSource( final IActionSource mySource )
	{
		this.mySource = mySource;
	}

	private static class InventoryCache
	{
		private IItemList<IAEItemStack> currentlyCached = AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createList();
		private final IItemRepository iItemRepository;

		public InventoryCache( IItemRepository iItemRepository )
		{
			this.iItemRepository = iItemRepository;
		}

		public IItemList<IAEItemStack> getAvailableItems( IItemList<IAEItemStack> out )
		{
			currentlyCached.iterator().forEachRemaining( out::add );
			return out;
		}

		public List<IAEItemStack> update()
		{
			final List<IAEItemStack> changes = new ArrayList<>();

			IItemList<IAEItemStack> currentlyOnStorage = AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createList();
			this.iItemRepository.getAllItems().stream().map( s -> AEItemStack.fromItemStack( s.itemPrototype ).setStackSize( s.count ) ).forEach( currentlyOnStorage::add );

			for( final IAEItemStack is : currentlyCached )
			{
				is.setStackSize( -is.getStackSize() );
			}

			for( final IAEItemStack is : currentlyOnStorage )
			{
				currentlyCached.add( is );
			}

			for( final IAEItemStack is : currentlyCached )
			{
				if( is.getStackSize() != 0 )
				{
					changes.add( is );
				}
			}

			currentlyCached = currentlyOnStorage;

			return changes;
		}

	}
}
