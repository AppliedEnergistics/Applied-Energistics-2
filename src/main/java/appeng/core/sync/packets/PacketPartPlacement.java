package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import appeng.core.CommonHelper;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.parts.PartPlacement;

public class PacketPartPlacement extends AppEngPacket
{

	int x, y, z, face;
	float eyeHeight;

	// automatic.
	public PacketPartPlacement(ByteBuf stream) throws IOException {
		x = stream.readInt();
		y = stream.readInt();
		z = stream.readInt();
		face = stream.readByte();
		eyeHeight = stream.readFloat();
	}

	@Override
	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player)
	{
		EntityPlayerMP sender = (EntityPlayerMP) player;
		CommonHelper.proxy.updateRenderMode( sender );
		PartPlacement.eyeHeight = eyeHeight;
		PartPlacement.place( sender.getHeldItem(), x, y, z, face, sender, sender.worldObj, PartPlacement.PlaceType.INTERACT_FIRST_PASS, 0 );
		CommonHelper.proxy.updateRenderMode( null );
	}

	// api
	public PacketPartPlacement(int x, int y, int z, int face, float eyeHeight ) throws IOException {

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeInt( x );
		data.writeInt( y );
		data.writeInt( z );
		data.writeByte( face );
		data.writeFloat( eyeHeight );
		
		configureWrite( data );
	}

}
