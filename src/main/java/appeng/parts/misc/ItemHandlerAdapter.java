package appeng.parts.misc;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.me.storage.ITickingMonitor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;


/**
 * Wraps an Item Handler in such a way that it can be used as an IMEInventory for items.
 */
class ItemHandlerAdapter implements IMEInventory<IAEItemStack>, IBaseMonitor<IAEItemStack>, ITickingMonitor
{

	private final Map<IMEMonitorHandlerReceiver<IAEItemStack>, Object> listeners = new HashMap<IMEMonitorHandlerReceiver<IAEItemStack>, Object>();

	private BaseActionSource mySource;

	private final IItemHandler itemHandler;

	private ItemStack[] cachedStacks = new ItemStack[0];

	private IAEItemStack[] cachedAeStacks = new IAEItemStack[0];

	ItemHandlerAdapter( IItemHandler itemHandler )
	{
		this.itemHandler = itemHandler;
	}

	@Override
	public IAEItemStack injectItems( IAEItemStack iox, Actionable type, BaseActionSource src )
	{

		ItemStack orgInput = iox.getItemStack();
		ItemStack remaining = orgInput;

		// In simulation mode, we don't need to do 2 passes, since if we find an empty slot, we would
		// eventually insert into it in the second phase. So for the sake of a simulation, we can consider it
		// immediately.
		if( type == Actionable.SIMULATE )
		{
			for( int i = 0; i < itemHandler.getSlots(); i++ )
			{
				// We have a chance to use this slot for injection
				remaining = itemHandler.insertItem( i, remaining, true );
				if( remaining == null )
				{
					break; // Awesome, full stack consumed
				}
			}
		}
		else
		{

			// For actually inserting the stack, we try to fill up existing stacks in the inventory, and then move onto free slots
			// Think about this: In a storage drawer or barrel setup, we'd want to attempt to fill up containers that already have the
			// item we're trying to insert first. The second phase should not cost a considerable amount of time, since it will just search
			// for empty slots.
			int slotCount = itemHandler.getSlots();

			// This array is used to remember which slots were viable but skipped in the first phase
			// This avoids calling getStackInSlot for each slot a second time. Hopefully the JVM will decide
			// to allocate this array on the stack
			boolean[] retry = new boolean[slotCount];

			for( int i = 0; i < slotCount; i++ )
			{
				ItemStack stackInSlot = itemHandler.getStackInSlot( i );

				if( stackInSlot == null )
				{
					retry[i] = true;
					continue; // In the first phase, we try to top up existing item stacks
				}

				remaining = itemHandler.insertItem( i, remaining, false );
				if( remaining == null )
				{
					break; // Awesome, full stack consumed
				}
			}

			// If we reached this point, we still have items to insert, our first pass failed.
			// Now we try to insert into empty slots
			for( int i = 0; i < slotCount; i++ )
			{
				if( retry[i] )
				{
					remaining = itemHandler.insertItem( i, remaining, false );
					if( remaining == null )
					{
						break; // Awesome, full stack consumed
					}
				}
			}
		}

		// At this point, we still have some items left...
		if ( remaining == orgInput ) {
			// The stack remained unmodified, target inventory is full
			return iox;
		}

		return AEItemStack.create( remaining );

	}

	@Override
	public IAEItemStack extractItems( IAEItemStack request, Actionable mode, BaseActionSource src )
	{

		ItemStack req = request.getItemStack();
		int remainingSize = req.stackSize;

		// Use this to gather the requested items
		ItemStack gathered = null;

		final boolean simulate = ( mode == Actionable.SIMULATE );

		for( int i = 0; i < itemHandler.getSlots(); i++ )
		{
			ItemStack sub = itemHandler.getStackInSlot( i );

			if( !Platform.isSameItem( sub, req ) )
			{
				continue;
			}

			ItemStack extracted;

			// We have to loop here because according to the docs, the handler shouldn't return a stack with size > maxSize, even if we
			// request more. So even if it returns a valid stack, it might have more stuff.
			do
			{
				extracted = itemHandler.extractItem( i, remainingSize, simulate );
				if( extracted != null )
				{
					if( extracted.stackSize > remainingSize )
					{
						// Something broke. It should never return more than we requested... We're going to silently eat the remainder
						AELog.warn( "Mod that provided item handler {} is broken. Returned {} items, even though we requested {}.",
								itemHandler.getClass().getSimpleName(), extracted.stackSize, remainingSize );
						extracted.stackSize = remainingSize;
					}

					// We're just gonna use the first stack we get our hands on as the template for the rest
					if( gathered == null )
					{
						gathered = extracted;
					}
					else
					{
						gathered.stackSize += extracted.stackSize;
					}
					remainingSize -= gathered.stackSize;
				}
			}
			while( extracted != null && remainingSize > 0 );

			// Done?
			if( remainingSize <= 0 )
			{
				break;
			}
		}

		if( gathered != null )
		{
			return AEItemStack.create( gathered );
		}

		return null;

	}

	public TickRateModulation onTick()
	{
		LinkedList<IAEItemStack> changes = new LinkedList<IAEItemStack>();

		int slots = itemHandler.getSlots();

		// Make room for new slots
		if ( slots > cachedStacks.length ) {
			cachedStacks = Arrays.copyOf( cachedStacks, slots );
			cachedAeStacks = Arrays.copyOf( cachedAeStacks, slots );
		}

		for( int slot = 0; slot < slots; slot++ )
		{
			// Save the old stuff
			ItemStack oldIS = cachedStacks[slot];
			IAEItemStack oldAeIS = cachedAeStacks[slot];

			ItemStack newIS = itemHandler.getStackInSlot( slot );

			if( this.isDifferent( newIS, oldIS ) )
			{
				// Completely different item
				cachedStacks[slot] = newIS;
				cachedAeStacks[slot] = AEItemStack.create( newIS );

				// If we had a stack previously in this slot, notify the newtork about its disappearance
				if( oldAeIS != null )
				{
					oldAeIS.setStackSize( -oldAeIS.getStackSize() );
					changes.add( oldAeIS );
				}

				// Notify the network about the new stack. Note that this is null if newIS was null
				if( cachedAeStacks[slot] != null )
				{
					changes.add( cachedAeStacks[slot] );
				}
			}
			else
			{
				// Still the same item, but amount might have changed
				int newSize = ( newIS == null ? 0 : newIS.stackSize );
				int diff = newSize - ( oldIS == null ? 0 : oldIS.stackSize );

				IAEItemStack stack = ( oldAeIS == null ? AEItemStack.create( newIS ) : oldAeIS.copy() );
				if( stack != null )
				{
					stack.setStackSize( newSize );
				}

				if( diff != 0 && stack != null )
				{
					cachedStacks[slot] = newIS;
					cachedAeStacks[slot] = stack;

					final IAEItemStack a = stack.copy();
					a.setStackSize( diff );
					changes.add( a );
				}
			}
		}

		// Handle cases where the number of slots actually is lower now than before
		if( slots < cachedStacks.length )
		{
			for (int slot = slots; slot < cachedStacks.length; slot++ )
			{
				IAEItemStack aeStack = cachedAeStacks[slot];
				if ( aeStack != null ) {
					IAEItemStack a = aeStack.copy();
					a.setStackSize( -a.getStackSize() );
					changes.add( a );
				}
			}

			// Reduce the cache size
			cachedStacks = Arrays.copyOf( cachedStacks, slots );
			cachedAeStacks = Arrays.copyOf( cachedAeStacks, slots );
		}

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

	private boolean isDifferent( final ItemStack a, final ItemStack b )
	{
		if( a == b && b == null )
		{
			return false;
		}

		if( ( a == null && b != null ) || ( a != null && b == null ) )
		{
			return true;
		}

		return !Platform.isSameItemPrecise( a, b );
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
				key.postChange( this, a, mySource );
			}
			else
			{
				i.remove();
			}
		}
	}

	private BaseActionSource getActionSource()
	{
		return this.mySource;
	}

	public void setActionSource( final BaseActionSource mySource )
	{
		this.mySource = mySource;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems( IItemList<IAEItemStack> out )
	{

		for( int i = 0; i < itemHandler.getSlots(); i++ )
		{
			out.addStorage( AEItemStack.create( itemHandler.getStackInSlot( i ) ) );
		}

		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	@Override
	public void addListener( final IMEMonitorHandlerReceiver<IAEItemStack> l, final Object verificationToken )
	{
		this.listeners.put( l, verificationToken );
	}

	@Override
	public void removeListener( final IMEMonitorHandlerReceiver<IAEItemStack> l )
	{
		this.listeners.remove( l );
	}

}
