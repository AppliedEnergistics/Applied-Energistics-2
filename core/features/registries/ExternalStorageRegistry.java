package appeng.core.features.registries;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IExternalStorageRegistry;
import appeng.api.storage.StorageChannel;
import appeng.core.features.registries.entries.ExternalIInv;

public class ExternalStorageRegistry implements IExternalStorageRegistry
{

	List<IExternalStorageHandler> Handlers;
	final ExternalIInv lastHandler = new ExternalIInv();

	public ExternalStorageRegistry() {
		Handlers = new ArrayList();
	}

	@Override
	public IExternalStorageHandler getHandler(TileEntity te, ForgeDirection d, StorageChannel chan)
	{
		for (IExternalStorageHandler x : Handlers)
		{
			if ( x.canHandle( te, d, chan ) )
				return x;
		}

		if ( lastHandler.canHandle( te, d, chan ) )
			return lastHandler;

		return null;
	}

	@Override
	public void addExternalStorageInterface(IExternalStorageHandler ei)
	{
		Handlers.add( ei );
	}

}
