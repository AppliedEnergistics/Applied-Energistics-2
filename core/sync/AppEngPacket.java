package appeng.core.sync;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.network.NetworkHandler;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;

public abstract class AppEngPacket
{

	private ByteBuf p;

	AppEngPacketHandlerBase.PacketTypes id;

	final public int getPacketID()
	{
		return AppEngPacketHandlerBase.PacketTypes.getID( this.getClass() ).ordinal();
	}

	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player)
	{
		throw new RuntimeException( "This packet ( " + getPacketID() + " does not implement a server side handler." );
	}

	public void clientPacketData(INetworkInfo network, AppEngPacket packet, EntityPlayer player)
	{
		throw new RuntimeException( "This packet ( " + getPacketID() + " does not implement a client side handler." );
	}

	protected void configureWrite(ByteBuf data)
	{
		data.capacity( data.readableBytes() );
		p = data;
	}

	public FMLProxyPacket getProxy()
	{
		return new FMLProxyPacket( p, NetworkHandler.instance.getChannel() );
	}

}
