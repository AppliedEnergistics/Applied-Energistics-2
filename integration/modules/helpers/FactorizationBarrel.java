package appeng.integration.modules.helpers;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import appeng.api.config.Actionable;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.integration.abstraction.IFZ;
import appeng.util.item.AEItemStack;

public class FactorizationBarrel implements IMEInventory<IAEItemStack>
{

	private final TileEntity te;
	IFZ fProxy;

	public FactorizationBarrel(IFZ proxy, TileEntity tile) {
		te = tile;
		fProxy = proxy;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	public long remainingItemTypes()
	{
		return fProxy.barrelGetItem( te ) == null ? 1 : 0;
	}

	public long remainingItemCount()
	{
		return fProxy.barrelGetMaxItemCount( te ) - fProxy.barrelGetItemCount( te );
	}

	public boolean containsItemType(IAEItemStack i)
	{
		return i.equals( fProxy.barrelGetItem( te ) );
	}

	public long storedItemCount()
	{
		return fProxy.barrelGetItemCount( te );
	}

	@Override
	public IAEItemStack injectItems(IAEItemStack input, Actionable mode)
	{
		if ( input == null )
			return null;
		if ( input.getStackSize() == 0 )
			return null;

		ItemStack shared = input.getItemStack();
		if ( shared.isItemDamaged() )
			return input;

		if ( remainingItemTypes() > 0 )
		{
			if ( mode == Actionable.MODULATE )
				fProxy.setItemType( te, input.getItemStack() );
		}

		if ( containsItemType( input ) )
		{
			int max = fProxy.barrelGetMaxItemCount( te );
			int newTotal = (int) storedItemCount() + (int) input.getStackSize();
			if ( newTotal > max )
			{
				if ( mode == Actionable.MODULATE )
					fProxy.barrelSetCount( te, max );
				IAEItemStack result = input.copy();
				result.setStackSize( newTotal - max );
				return result;
			}
			else
			{
				if ( mode == Actionable.MODULATE )
					fProxy.barrelSetCount( te, newTotal );
				return null;
			}
		}

		return input;
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request, Actionable mode)
	{
		if ( containsItemType( request ) )
		{
			int howMany = (int) storedItemCount();
			if ( request.getStackSize() >= howMany )
			{
				if ( mode == Actionable.MODULATE )
				{
					fProxy.setItemType( te, null );
					fProxy.barrelSetCount( te, 0 );
				}

				IAEItemStack r = request.copy();
				r.setStackSize( howMany );
				return r;
			}
			else
			{
				if ( mode == Actionable.MODULATE )
					fProxy.barrelSetCount( te, (int) (howMany - request.getStackSize()) );
				return request.copy();
			}
		}
		return null;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems(IItemList out)
	{
		ItemStack i = fProxy.barrelGetItem( te );
		if ( i != null )
		{
			i.stackSize = fProxy.barrelGetItemCount( te );
			out.addStorage( AEItemStack.create( i ) );
		}

		return out;
	}

}