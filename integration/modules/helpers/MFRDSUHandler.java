package appeng.integration.modules.helpers;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.integration.modules.MFR;

public class MFRDSUHandler implements IExternalStorageHandler
{

	@Override
	public boolean canHandle(TileEntity te, ForgeDirection d, StorageChannel chan)
	{
		return chan == StorageChannel.ITEMS && MFR.instance.isDSU( te );
	}

	@Override
	public IMEInventory getInventory(TileEntity te, ForgeDirection d, StorageChannel chan)
	{
		if ( chan == StorageChannel.ITEMS )
			return MFR.instance.getDSU( te );
		return null;
	}

}
