package appeng.core.sync.network;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.InvocationTargetException;

import net.minecraft.entity.player.EntityPlayer;
import appeng.core.AELog;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandlerBase;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;

public final class AppEngServerPacketHandler extends AppEngPacketHandlerBase implements IPacketHandler
{

	@Override
	public void onPacketData(INetworkInfo manager, FMLProxyPacket packet, EntityPlayer player)
	{
		ByteBuf stream = packet.payload();
		int packetType = -1;

		try
		{
			packetType = stream.readInt();
			AppEngPacket pack = PacketTypes.getPacket( packetType ).parsePacket( stream );
			pack.serverPacketData( manager, pack, player );
		}
		catch (InstantiationException e)
		{
			AELog.error( e );
		}
		catch (IllegalAccessException e)
		{
			AELog.error( e );
		}
		catch (IllegalArgumentException e)
		{
			AELog.error( e );
		}
		catch (InvocationTargetException e)
		{
			AELog.error( e );
		}

	}
}
