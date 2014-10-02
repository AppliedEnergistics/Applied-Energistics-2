package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.items.tools.ToolNetworkTool;
import appeng.items.tools.powered.ToolColorApplicator;

public class PacketClick extends AppEngPacket
{

	final int x;
	final int y;
	final int z;
	final int side;
	final float hitX;
	final float hitY;
	final float hitZ;

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
		else if ( is != null && AEApi.instance().items().itemMemoryCard.sameAsStack( is ) )
		{
			IMemoryCard mem = (IMemoryCard) is.getItem();
			mem.notifyUser( player, MemoryCardMessages.SETTINGS_CLEARED );
			is.setTagCompound( null );
		}
		else if ( is != null && AEApi.instance().items().itemColorApplicator.sameAsStack( is ) )
		{
			ToolColorApplicator mem = (ToolColorApplicator) is.getItem();
			mem.cycleColors( is, mem.getColor( is ), 1 );
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
