package appeng.core.features.registries;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.ICellRegistry;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;

public class CellRegistry implements ICellRegistry
{

	List<ICellHandler> handlers;

	public CellRegistry() {
		handlers = new ArrayList<ICellHandler>();
	}

	@Override
	public void addCellHandler(ICellHandler h)
	{
		if ( h != null )
			handlers.add( h );
	}

	@Override
	public boolean isCellHandled(ItemStack is)
	{
		if ( is == null )
			return false;
		for (ICellHandler ch : handlers)
			if ( ch.isCell( is ) )
				return true;
		return false;
	}

	@Override
	public ICellHandler getHandler(ItemStack is)
	{
		if ( is == null )
			return null;
		for (ICellHandler ch : handlers)
		{
			if ( ch.isCell( is ) )
			{
				return ch;
			}
		}
		return null;
	}

	@Override
	public IMEInventoryHandler getCellInventory(ItemStack is, ISaveProvider container, StorageChannel chan)
	{
		if ( is == null )
			return null;
		for (ICellHandler ch : handlers)
		{
			if ( ch.isCell( is ) )
			{
				return ch.getCellInventory( is, container, chan );
			}
		}
		return null;
	}
}
