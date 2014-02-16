package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import appeng.container.AEBaseContainer;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;

public class PacketProgressBar extends AppEngPacket
{

	short id;
	long value;

	// automatic.
	public PacketProgressBar(ByteBuf stream) throws IOException {
		id = stream.readShort();
		value = stream.readLong();
	}

	@Override
	public void clientPacketData(INetworkInfo network, AppEngPacket packet, EntityPlayer player)
	{
		Container c = player.openContainer;
		if ( c instanceof AEBaseContainer )
			((AEBaseContainer) c).updateFullProgressBar( id, value );
	}

	@Override
	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player)
	{
		Container c = player.openContainer;
		if ( c instanceof AEBaseContainer )
			((AEBaseContainer) c).updateFullProgressBar( id, value );
	}

	// api
	public PacketProgressBar(int short_id, long value) throws IOException {

		this.id = (short) short_id;
		this.value = value;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeShort( short_id );
		data.writeLong( value );

		configureWrite( data );
	}
}
