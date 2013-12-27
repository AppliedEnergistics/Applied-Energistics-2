package appeng.integration.abstraction;

import net.minecraft.tileentity.TileEntity;
import appeng.api.parts.IPartHost;
import appeng.parts.CableBusContainer;

public interface IFMP
{

	IPartHost getOrCreateHost(TileEntity tile);

	CableBusContainer getCableContainer(TileEntity te);

	void registerPassThru(Class<?> layerInterface);

}
