package appeng.me.storage;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.IncludeExclude;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.util.prioitylist.DefaultPriorityList;
import appeng.util.prioitylist.IPartitionList;

public class MEInventoryHandler<T extends IAEStack<T>> implements IMEInventoryHandler<T>
{

	final StorageChannel channel;
	final protected IMEMonitor<T> monitor;
	final protected IMEInventoryHandler<T> internal;

	public int myPriority = 0;
	public IncludeExclude myWhitelist = IncludeExclude.WHITELIST;
	public AccessRestriction myAccess = AccessRestriction.READ_WRITE;
	public IPartitionList<T> myPartitionList = new DefaultPriorityList<T>();

	public MEInventoryHandler(IMEInventory<T> i, StorageChannel channel) {
		this.channel = channel;

		if ( i instanceof IMEInventoryHandler )
			internal = (IMEInventoryHandler<T>) i;
		else
			internal = new MEPassthru<T>( i, channel );

		monitor = internal instanceof IMEMonitor ? (IMEMonitor<T>) internal : null;
	}

	@Override
	public T injectItems(T input, Actionable type, BaseActionSource src)
	{
		if ( !this.canAccept( input ) )
			return input;

		return internal.injectItems( input, type, src );
	}

	@Override
	public T extractItems(T request, Actionable type, BaseActionSource src)
	{
		if ( !getAccess().hasPermission( AccessRestriction.READ ) )
			return null;

		return internal.extractItems( request, type, src );
	}

	@Override
	public IItemList<T> getAvailableItems(IItemList<T> out)
	{
		if ( !getAccess().hasPermission( AccessRestriction.READ ) )
			return out;

		return internal.getAvailableItems( out );
	}

	@Override
	public StorageChannel getChannel()
	{
		return internal.getChannel();
	}

	@Override
	public AccessRestriction getAccess()
	{
		return myAccess.restrictPermissions( internal.getAccess() );
	}

	@Override
	public boolean isPrioritized(T input)
	{
		if ( myWhitelist == IncludeExclude.WHITELIST )
			return myPartitionList.isListed( input ) || internal.isPrioritized( input );
		return false;
	}

	@Override
	public boolean canAccept(T input)
	{
		if ( !getAccess().hasPermission( AccessRestriction.WRITE ) )
			return false;

		if ( myWhitelist == IncludeExclude.BLACKLIST && myPartitionList.isListed( input ) )
			return false;
		if ( myPartitionList.isEmpty() || myWhitelist == IncludeExclude.BLACKLIST )
			return internal.canAccept( input );
		return myPartitionList.isListed( input ) && internal.canAccept( input );
	}

	@Override
	public int getPriority()
	{
		return myPriority;
	}

	@Override
	public int getSlot()
	{
		return internal.getSlot();
	}

	public IMEInventory<T> getInternal()
	{
		return internal;
	}

	@Override
	public boolean validForPass(int i)
	{
		return true;
	}

}
