package appeng.me.storage;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class MEIInventoryWrapper implements IMEInventory<IAEItemStack>
{

	protected IInventory target;
	protected InventoryAdaptor adaptor;

	public MEIInventoryWrapper(IInventory m, InventoryAdaptor ia) {
		target = m;
		adaptor = ia;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	@Override
	public IAEItemStack injectItems(IAEItemStack iox, Actionable mode, BaseActionSource src)
	{
		ItemStack input = iox.getItemStack();

		if ( adaptor != null )
		{
			ItemStack is = mode == Actionable.SIMULATE ? adaptor.simulateAdd( input ) : adaptor.addItems( input );
			if ( is == null )
				return null;
			return AEItemStack.create( is );
		}

		ItemStack out = Platform.cloneItemStack( input );

		if ( mode == Actionable.MODULATE ) // absolutely no need for a first run in simulate mode.
		{
			for (int x = 0; x < target.getSizeInventory(); x++)
			{
				ItemStack t = target.getStackInSlot( x );

				if ( Platform.isSameItem( t, input ) )
				{
					int oriStack = t.stackSize;
					t.stackSize += out.stackSize;

					target.setInventorySlotContents( x, t );

					if ( t.stackSize > target.getInventoryStackLimit() )
					{
						t.stackSize = target.getInventoryStackLimit();
					}

					if ( t.stackSize > t.getMaxStackSize() )
					{
						t.stackSize = t.getMaxStackSize();
					}

					out.stackSize -= t.stackSize - oriStack;

					if ( out.stackSize <= 0 )
					{
						return null;
					}
				}
			}
		}

		for (int x = 0; x < target.getSizeInventory(); x++)
		{
			ItemStack t = target.getStackInSlot( x );

			if ( t == null )
			{
				t = Platform.cloneItemStack( input );
				t.stackSize = out.stackSize;

				if ( t.stackSize > target.getInventoryStackLimit() )
				{
					t.stackSize = target.getInventoryStackLimit();
				}

				out.stackSize -= t.stackSize;
				if ( mode == Actionable.MODULATE )
					target.setInventorySlotContents( x, t );

				if ( out.stackSize <= 0 )
				{
					return null;
				}
			}
		}

		return AEItemStack.create( out );
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src)
	{
		ItemStack Gathered = null;
		ItemStack Req = request.getItemStack();

		int request_stackSize = Req.stackSize;

		if ( request_stackSize > Req.getMaxStackSize() )
		{
			request_stackSize = Req.getMaxStackSize();
		}

		Req.stackSize = request_stackSize;

		if ( adaptor != null )
		{
			Gathered = adaptor.removeItems( Req.stackSize, Req, null );
		}
		else
		{
			Gathered = request.getItemStack();
			Gathered.stackSize = 0;

			// try to find matching inventories that already have it...
			for (int x = 0; x < target.getSizeInventory(); x++)
			{
				ItemStack sub = target.getStackInSlot( x );

				if ( Platform.isSameItem( sub, Req ) )
				{
					int reqNum = Req.stackSize;

					if ( reqNum > sub.stackSize )
					{
						reqNum = Req.stackSize;
					}

					ItemStack retrieved = null;

					if ( sub.stackSize < Req.stackSize )
					{
						retrieved = Platform.cloneItemStack( sub );
						sub.stackSize = 0;
					}
					else
					{
						retrieved = sub.splitStack( Req.stackSize );
					}

					if ( sub.stackSize <= 0 )
						target.setInventorySlotContents( x, null );
					else
						target.setInventorySlotContents( x, sub );

					if ( retrieved != null )
					{
						Gathered.stackSize += retrieved.stackSize;
						Req.stackSize -= retrieved.stackSize;
					}

					if ( request_stackSize == Gathered.stackSize )
					{
						return AEItemStack.create( Gathered );
					}
				}
			}

			if ( Gathered.stackSize == 0 )
			{
				return null;
			}

		}

		return AEItemStack.create( Gathered );
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems(IItemList out)
	{
		for (int x = 0; x < target.getSizeInventory(); x++)
		{
			out.addStorage( AEItemStack.create( target.getStackInSlot( x ) ) );
		}

		return out;
	}

}
