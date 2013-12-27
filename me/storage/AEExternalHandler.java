package appeng.me.storage;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.implementations.IStorageMonitorable;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.tile.misc.TileCondenser;

public class AEExternalHandler implements IExternalStorageHandler
{

	@Override
	public boolean canHandle(TileEntity te, ForgeDirection d, StorageChannel channel)
	{
		if ( channel == StorageChannel.ITEMS && te instanceof IStorageMonitorable )
			return true;

		if ( channel == StorageChannel.ITEMS && te instanceof IInventory )
			return true;

		return te instanceof TileCondenser;
	}

	@Override
	public IMEInventory getInventory(TileEntity te, ForgeDirection d, StorageChannel channel)
	{
		if ( te instanceof TileCondenser )
		{
			if ( channel == StorageChannel.ITEMS )
				return new VoidItemInventory( (TileCondenser) te );
			else
				return new VoidFluidInventory( (TileCondenser) te );
		}

		if ( te instanceof IStorageMonitorable )
		{
			IStorageMonitorable iface = (IStorageMonitorable) te;

			if ( channel == StorageChannel.ITEMS )
			{
				IMEInventory<IAEItemStack> ii = iface.getItemInventory();
				if ( ii != null )
					return ii;
			}

			if ( channel == StorageChannel.FLUIDS )
			{
				IMEInventory<IAEFluidStack> fi = iface.getFluidInventory();
				if ( fi != null )
					return fi;
			}
		}

		if ( channel == StorageChannel.ITEMS && te instanceof IInventory )
		{
			return new MEMonitorIInventory( (IInventory) te, d );
		}

		return null;
	}
}
