package appeng.core.sync;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import appeng.core.Configuration;

public abstract class AppEngPacket
{

	private Packet250CustomPayload p;
	protected boolean isChunkDataPacket;

	AppEngPacketHandlerBase.PacketTypes id;

	final public int getPacketID()
	{
		return AppEngPacketHandlerBase.PacketTypes.getID( this.getClass() ).ordinal();
	}

	public void serverPacketData(INetworkManager manager, AppEngPacket packet, EntityPlayer player)
	{
		throw new RuntimeException( "This packet ( " + getPacketID() + " does not implement a server side handler." );
	}

	public void clientPacketData(INetworkManager network, AppEngPacket packet, EntityPlayer player)
	{
		throw new RuntimeException( "This packet ( " + getPacketID() + " does not implement a client side handler." );
	}

	public Packet250CustomPayload getPacket()
	{
		// / += p.getPacketSize();
		return p;
	}

	protected void configureWrite(byte[] par2ArrayOfByte)
	{
		p = new Packet250CustomPayload( Configuration.PACKET_CHANNEL, par2ArrayOfByte );
		p.isChunkDataPacket = isChunkDataPacket;
	}

}
