package appeng.integration.modules.dead;

import net.mcft.copy.betterstorage.api.ICrateStorage;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.integration.IIntegrationModule;
import appeng.integration.abstraction.IBS;
import appeng.integration.modules.helpers.dead.BSCrateHandler;
import appeng.integration.modules.helpers.dead.BSCrateStorageAdaptor;
import appeng.util.InventoryAdaptor;

public class BS implements IIntegrationModule, IBS
{

	public static BS instance;

	@Override
	public boolean isStorageCrate(Object te)
	{
		return te instanceof ICrateStorage;
	}

	@Override
	public InventoryAdaptor getAdaptor(Object te, ForgeDirection d)
	{
		if ( te instanceof ICrateStorage )
		{
			return new BSCrateStorageAdaptor( te, d );
		}
		return null;
	}

	@Override
	public void Init()
	{

	}

	@Override
	public void PostInit()
	{
		AEApi.instance().registries().externalStorage().addExternalStorageInterface( new BSCrateHandler() );
	}

}
