package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import appeng.api.util.DimensionalCoord;
import appeng.core.AELog;
import appeng.core.WorldSettings;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.network.NetworkHandler;
import appeng.services.helpers.ICompassCallback;

public class PacketCompassRequest extends AppEngPacket implements ICompassCallback
{

	final public long attunement;
	final public int cx, cz, cdy;

	EntityPlayer talkBackTo;

	// automatic.
	public PacketCompassRequest(ByteBuf stream) throws IOException {
		attunement = stream.readLong();
		cx = stream.readInt();
		cz = stream.readInt();
		cdy = stream.readInt();
	}

	@Override
	public void calculatedDirection(boolean hasResult, boolean spin, double radians)
	{
		NetworkHandler.instance.sendTo( new PacketCompassResponse( this, hasResult, spin, radians ), (EntityPlayerMP) talkBackTo );
	}

	@Override
	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player)
	{
		AELog.info( "PacketCompassRequest.serverPacketData" );
		talkBackTo = player;

		DimensionalCoord loc = new DimensionalCoord( player.worldObj, this.cx << 4, this.cdy << 5, this.cz << 4 );
		WorldSettings.getInstance().getCompass().getCompassDirection( loc, this );
	}

	// api
	public PacketCompassRequest(long attunement, int cx, int cz, int cdy) throws IOException {

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeLong( this.attunement = attunement );
		data.writeInt( this.cx = cx );
		data.writeInt( this.cz = cz );
		data.writeInt( this.cdy = cdy );

		configureWrite( data );

	}
}
