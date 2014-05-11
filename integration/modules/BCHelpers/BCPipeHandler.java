package appeng.integration.modules.BCHelpers;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.integration.modules.BC;

public class BCPipeHandler implements IExternalStorageHandler
{

	@Override
	public boolean canHandle(TileEntity te, ForgeDirection d, StorageChannel chan)
	{
		return chan == StorageChannel.ITEMS && BC.instance.isPipe( te, d );
	}

	@Override
	public IMEInventory getInventory(TileEntity te, ForgeDirection d, StorageChannel chan, BaseActionSource src)
	{
		if ( chan == StorageChannel.ITEMS )
			return new BCPipeInventory( te, d );
		return null;
	}

}
