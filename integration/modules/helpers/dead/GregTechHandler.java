package appeng.integration.modules.helpers;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.integration.modules.GT;
import appeng.me.storage.MEMonitorIInventory;
import appeng.util.inv.IMEAdaptor;

public class GregTechHandler implements IExternalStorageHandler
{

	@Override
	public boolean canHandle(TileEntity te, ForgeDirection d, StorageChannel chan)
	{
		return chan == StorageChannel.ITEMS && GT.instance.isQuantumChest( te );
	}

	@Override
	public IMEInventory getInventory(TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource src)
	{
		if ( channel == StorageChannel.ITEMS )
			return new MEMonitorIInventory( new IMEAdaptor( GT.instance.getQuantumChest( te ) ) );
		return null;
	}

}
