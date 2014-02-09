package appeng.integration.abstraction;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import appeng.api.parts.IPartHost;
import appeng.parts.CableBusContainer;
import cpw.mods.fml.common.eventhandler.Event;

public interface IFMP
{

	IPartHost getOrCreateHost(TileEntity tile);

	CableBusContainer getCableContainer(TileEntity te);

	void registerPassThru(Class<?> layerInterface);

	Event newFMPPacketEvent(EntityPlayerMP sender);

}
