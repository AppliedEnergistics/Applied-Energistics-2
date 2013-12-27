package appeng.integration.modules.helpers;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.integration.modules.GT;

public class GregTechHandler implements IExternalStorageHandler
{

	@Override
	public boolean canHandle(TileEntity te, ForgeDirection d, StorageChannel chan)
	{
		return chan == StorageChannel.ITEMS && GT.instance.isQuantumChest( te );
	}

	@Override
	public IMEInventory getInventory(TileEntity te, ForgeDirection d, StorageChannel chan)
	{
		if ( chan == StorageChannel.ITEMS )
			return GT.instance.getQuantumChest( te );
		return null;
	}

}
