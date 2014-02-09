package appeng.integration.abstraction;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.util.InventoryAdaptor;

public interface IBS
{

	boolean isStorageCrate(Object te);

	InventoryAdaptor getAdaptor(Object te, ForgeDirection d);

}
