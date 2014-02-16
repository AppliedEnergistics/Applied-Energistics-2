package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.items.tools.ToolNetworkTool;

public class PacketClick extends AppEngPacket
{

	int x, y, z, side;
	float hitX, hitY, hitZ;

	// automatic.
	public PacketClick(ByteBuf stream) throws IOException {
		x = stream.readInt();
		y = stream.readInt();
		z = stream.readInt();
		side = stream.readInt();
		hitX = stream.readFloat();
		hitY = stream.readFloat();
		hitZ = stream.readFloat();
	}

	@Override
	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player)
	{
		ItemStack is = player.inventory.getCurrentItem();
		if ( is != null && is.getItem() instanceof ToolNetworkTool )
		{
			ToolNetworkTool tnt = (ToolNetworkTool) is.getItem();
			tnt.serverSideToolLogic( is, player, player.worldObj, x, y, z, side, hitX, hitY, hitZ );
		}
	}

	// api
	public PacketClick(int x, int y, int z, int side, float hitX, float hitY, float hitZ) throws IOException {

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeInt( this.x = x );
		data.writeInt( this.y = y );
		data.writeInt( this.z = z );
		data.writeInt( this.side = side );
		data.writeFloat( this.hitX = hitX );
		data.writeFloat( this.hitY = hitY );
		data.writeFloat( this.hitZ = hitZ );

		configureWrite( data );
	}
}
