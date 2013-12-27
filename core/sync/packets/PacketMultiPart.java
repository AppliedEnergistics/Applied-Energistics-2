package appeng.core.sync.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraftforge.common.MinecraftForge;
import appeng.core.sync.AppEngPacket;
import appeng.integration.modules.helpers.FMPPacketEvent;
import cpw.mods.fml.common.network.Player;

public class PacketMultiPart extends AppEngPacket
{

	// automatic.
	public PacketMultiPart(DataInputStream stream) throws IOException {

	}

	@Override
	public void serverPacketData(INetworkManager manager, AppEngPacket packet, Player player)
	{
		EntityPlayerMP sender = (EntityPlayerMP) player;
		MinecraftForge.EVENT_BUS.post( new FMPPacketEvent( sender ) );
	}

	// api
	public PacketMultiPart() throws IOException {

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream( bytes );

		data.writeInt( getPacketID() );

		isChunkDataPacket = false;
		configureWrite( bytes.toByteArray() );
	}

}
