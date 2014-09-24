package appeng.core.features.registries.entries;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.me.storage.MEMonitorIInventory;
import appeng.util.InventoryAdaptor;

public class ExternalIInv implements IExternalStorageHandler
{

	@Override
	public boolean canHandle(TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource mySrc)
	{
		return channel == StorageChannel.ITEMS && te instanceof IInventory;
	}

	@Override
	public IMEInventory getInventory(TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource src)
	{
		InventoryAdaptor ad = InventoryAdaptor.getAdaptor( (IInventory) te, d );
		
		if ( channel == StorageChannel.ITEMS && ad != null )
			return new MEMonitorIInventory( ad );
		
		return null;
	}

}
