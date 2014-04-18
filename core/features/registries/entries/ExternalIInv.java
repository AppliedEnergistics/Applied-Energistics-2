package appeng.core.features.registries.entries;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.me.storage.MEMonitorIInventory;

public class ExternalIInv implements IExternalStorageHandler
{

	@Override
	public boolean canHandle(TileEntity te, ForgeDirection d, StorageChannel channel)
	{
		return channel == StorageChannel.ITEMS && te instanceof IInventory;
	}

	@Override
	public IMEInventory getInventory(TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource src)
	{
		if ( channel == StorageChannel.ITEMS && te instanceof IInventory )
			return new MEMonitorIInventory( (IInventory) te, d );

		return null;
	}

}
