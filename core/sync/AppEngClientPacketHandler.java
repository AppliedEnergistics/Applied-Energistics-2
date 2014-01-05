package appeng.core.sync;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class AppEngClientPacketHandler extends AppEngPacketHandlerBase implements IPacketHandler
{

	@Override
	public void onPacketData(INetworkManager network, Packet250CustomPayload packet, Player player)
	{
		DataInputStream stream = new DataInputStream( new ByteArrayInputStream( packet.data ) );
		// Determine packet type and coordinates of affected tile entity
		int packetType = -1;

		try
		{
			packetType = stream.readInt();
			AppEngPacket pack = PacketTypes.getPacket( packetType ).parsePacket( stream );
			pack.clientPacketData( network, pack, (EntityPlayer) player );
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}

	}
}
