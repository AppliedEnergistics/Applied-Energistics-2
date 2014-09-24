package appeng.core.sync.network;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;

public interface IPacketHandler
{

	void onPacketData(INetworkInfo manager, FMLProxyPacket packet, EntityPlayer player);

}
