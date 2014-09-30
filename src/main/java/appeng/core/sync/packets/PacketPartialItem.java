package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import appeng.container.AEBaseContainer;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;

public class PacketPartialItem extends AppEngPacket
{

	final short pageNum;
	final byte[] data;

	// automatic.
	public PacketPartialItem(ByteBuf stream) throws IOException {
		pageNum = stream.readShort();
		stream.readBytes( data = new byte[stream.readableBytes()] );
	}

	@Override
	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player)
	{
		if ( player.openContainer instanceof AEBaseContainer )
		{
			((AEBaseContainer) player.openContainer).postPartial( this );
		}
	}

	// api
	public PacketPartialItem(int page, int maxPages, byte[] buf) throws IOException {

		ByteBuf data = Unpooled.buffer();

		pageNum = (short) (page | (maxPages << 8));
		this.data = buf;
		data.writeInt( getPacketID() );
		data.writeShort( pageNum );
		data.writeBytes( buf );

		configureWrite( data );
	}

	public int getPageCount()
	{
		return pageNum >> 8;
	}

	public int getSize()
	{
		return data.length;
	}

	public int write(byte[] buffer, int cursor)
	{
		System.arraycopy( data, 0, buffer, cursor, data.length );
		return cursor + data.length;
	}
}
