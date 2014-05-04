package appeng.integration.modules.helpers;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.integration.modules.DSU;
import appeng.me.storage.MEMonitorIInventory;
import appeng.util.inv.IMEAdaptor;

public class MFRDSUHandler implements IExternalStorageHandler
{

	@Override
	public boolean canHandle(TileEntity te, ForgeDirection d, StorageChannel chan)
	{
		return chan == StorageChannel.ITEMS && DSU.instance.isDSU( te );
	}

	@Override
	public IMEInventory getInventory(TileEntity te, ForgeDirection d, StorageChannel chan, BaseActionSource src)
	{
		if ( chan == StorageChannel.ITEMS )
			return new MEMonitorIInventory( new IMEAdaptor( DSU.instance.getDSU( te ), src ) );
		return null;
	}
}
