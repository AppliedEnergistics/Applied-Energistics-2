package appeng.api.storage;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Common implementation of a simple class that monitors injection/extraction of a inventory to send events to a list of
 * listeners.
 * 
 * @param <StackType>
 */
public class MEMonitorHandler<StackType extends IAEStack> implements IMEMonitor<StackType>
{

	private final IMEInventoryHandler<StackType> internalHandler;
	private final IItemList<StackType> cachedList;
	private final HashMap<IMEMonitorHandlerReceiver<StackType>, Object> listeners = new HashMap<IMEMonitorHandlerReceiver<StackType>, Object>();

	protected boolean hasChanged = true;

	protected IMEInventoryHandler<StackType> getHandler()
	{
		return internalHandler;
	}

	protected Iterator<Entry<IMEMonitorHandlerReceiver<StackType>, Object>> getListeners()
	{
		return listeners.entrySet().iterator();
	}

	protected void postChangesToListeners( Iterable<StackType> changes, BaseActionSource src)
	{
		notifyListenersOfChange( changes, src );
	}
	
	protected void notifyListenersOfChange(Iterable<StackType> diff, BaseActionSource src)
	{
		hasChanged = true;// need to update the cache.
		Iterator<Entry<IMEMonitorHandlerReceiver<StackType>, Object>> i = getListeners();
		while (i.hasNext())
		{
			Entry<IMEMonitorHandlerReceiver<StackType>, Object> o = i.next();
			IMEMonitorHandlerReceiver<StackType> receiver = o.getKey();
			if ( receiver.isValid( o.getValue() ) )
				receiver.postChange( this, diff, src );
			else
				i.remove();
		}
	}

	private StackType monitorDifference(IAEStack original, StackType leftOvers, boolean extraction, BaseActionSource src)
	{
		StackType diff = (StackType) original.copy();

		if ( extraction )
			diff.setStackSize( leftOvers == null ? 0 : -leftOvers.getStackSize() );
		else if ( leftOvers != null )
			diff.decStackSize( leftOvers.getStackSize() );

		if ( diff.getStackSize() != 0 )
			postChangesToListeners( ImmutableList.of( diff ), src );

		return leftOvers;
	}

	public MEMonitorHandler(IMEInventoryHandler<StackType> t) {
		internalHandler = t;
		cachedList = (IItemList<StackType>) t.getChannel().createList();
	}

	public MEMonitorHandler(IMEInventoryHandler<StackType> t, StorageChannel chan) {
		internalHandler = t;
		cachedList = (IItemList<StackType>) chan.createList();
	}

	@Override
	public void addListener(IMEMonitorHandlerReceiver<StackType> l, Object verificationToken)
	{
		listeners.put( l, verificationToken );
	}

	@Override
	public void removeListener(IMEMonitorHandlerReceiver<StackType> l)
	{
		listeners.remove( l );
	}

	@Override
	public StackType injectItems(StackType input, Actionable mode, BaseActionSource src)
	{
		if ( mode == Actionable.SIMULATE )
			return getHandler().injectItems( input, mode, src );
		return monitorDifference(input.copy(), getHandler().injectItems(input, mode, src), false, src);
	}

	@Override
	public StackType extractItems(StackType request, Actionable mode, BaseActionSource src)
	{
		if ( mode == Actionable.SIMULATE )
			return getHandler().extractItems( request, mode, src );
		return monitorDifference(request.copy(), getHandler().extractItems(request, mode, src), true, src);
	}

	@Override
	public IItemList<StackType> getStorageList()
	{
		if ( hasChanged )
		{
			hasChanged = false;
			cachedList.resetStatus();
			return getAvailableItems( cachedList );
		}

		return cachedList;
	}

	@Override
	public IItemList<StackType> getAvailableItems(IItemList out)
	{
		return getHandler().getAvailableItems( out );
	}

	@Override
	public StorageChannel getChannel()
	{
		return getHandler().getChannel();
	}

	@Override
	public AccessRestriction getAccess()
	{
		return getHandler().getAccess();
	}

	@Override
	public boolean isPrioritized(StackType input)
	{
		return getHandler().isPrioritized( input );
	}

	@Override
	public boolean canAccept(StackType input)
	{
		return getHandler().canAccept( input );
	}

	@Override
	public int getPriority()
	{
		return getHandler().getPriority();
	}

	@Override
	public int getSlot()
	{
		return getHandler().getSlot();
	}

	@Override
	public boolean validForPass(int i)
	{
		return getHandler().validForPass( i );
	}

}
