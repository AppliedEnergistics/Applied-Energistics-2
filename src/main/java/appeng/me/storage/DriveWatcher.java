package appeng.me.storage;

import net.minecraft.item.ItemStack;
import appeng.api.config.Actionable;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEStack;

public class DriveWatcher<T extends IAEStack<T>> extends MEInventoryHandler<T>
{

	final int oldStatus = 0;
	final ItemStack is;
	final ICellHandler handler;
	final IChestOrDrive cord;

	public DriveWatcher(IMEInventory<T> i, ItemStack is, ICellHandler han, IChestOrDrive cod) {
		super( i, i.getChannel() );
		this.is = is;
		handler = han;
		cord = cod;
	}

	@Override
	public T injectItems(T input, Actionable type, BaseActionSource src)
	{
		long size = input.getStackSize();

		T a = super.injectItems( input, type, src );

		if ( a == null || a.getStackSize() != size )
		{
			int newStatus = handler.getStatusForCell( is, getInternal() );

			if ( newStatus != oldStatus )
			{
				cord.blinkCell( getSlot() );
			}
		}

		return a;
	}

	@Override
	public T extractItems(T request, Actionable type, BaseActionSource src)
	{
		T a = super.extractItems( request, type, src );

		if ( a != null )
		{
			int newStatus = handler.getStatusForCell( is, getInternal() );

			if ( newStatus != oldStatus )
			{
				cord.blinkCell( getSlot() );
			}
		}

		return a;
	}
}
