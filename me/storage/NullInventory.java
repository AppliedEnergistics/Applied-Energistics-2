package appeng.me.storage;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

public class NullInventory<T extends IAEStack<T>> implements IMEInventoryHandler<T>
{

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	@Override
	public T injectItems(T input, Actionable mode, BaseActionSource src)
	{
		return input;
	}

	@Override
	public T extractItems(T request, Actionable mode, BaseActionSource src)
	{
		return null;
	}

	@Override
	public IItemList<T> getAvailableItems(IItemList out)
	{
		return out;
	}

	@Override
	public AccessRestriction getAccess()
	{
		return AccessRestriction.READ;
	}

	@Override
	public boolean isPrioritized(T input)
	{
		return false;
	}

	@Override
	public boolean canAccept(T input)
	{
		return false;
	}

	@Override
	public int getPriority()
	{
		return 0;
	}

	@Override
	public int getSlot()
	{
		return 0;
	}

}
