package appeng.integration.modules.helpers;

import net.mcft.copy.betterstorage.api.crate.ICrateStorage;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;

public class BSCrateHandler implements IExternalStorageHandler
{

	@Override
	public boolean canHandle(TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource mySrc)
	{
		return channel == StorageChannel.ITEMS && te instanceof ICrateStorage;
	}

	@Override
	public IMEInventory getInventory(TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource src)
	{
		if ( channel == StorageChannel.ITEMS )
			return new BSCrate( te, ForgeDirection.UNKNOWN );
		return null;
	}

}
