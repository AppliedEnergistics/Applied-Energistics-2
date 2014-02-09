package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import appeng.core.AppEng;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.integration.abstraction.IFMP;

public class PacketMultiPart extends AppEngPacket
{

	// automatic.
	public PacketMultiPart(ByteBuf stream) throws IOException {

	}

	@Override
	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player)
	{
		IFMP fmp = (IFMP) AppEng.instance.getIntegration( "FMP" );
		if ( fmp != null )
		{
			EntityPlayerMP sender = (EntityPlayerMP) player;
			MinecraftForge.EVENT_BUS.post( fmp.newFMPPacketEvent( sender ) ); // when received it just pots this event.
		}
	}

	// api
	public PacketMultiPart() throws IOException {

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );

		configureWrite( data );
	}

}
