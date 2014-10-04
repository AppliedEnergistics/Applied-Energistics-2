package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.hooks.CompassManager;
import appeng.hooks.CompassResult;

public class PacketCompassResponse extends AppEngPacket
{

	final public long attunement;
	final public int cx, cz, cdy;

	public CompassResult cr;

	// automatic.
	public PacketCompassResponse(ByteBuf stream) {
		attunement = stream.readLong();
		cx = stream.readInt();
		cz = stream.readInt();
		cdy = stream.readInt();

		cr = new CompassResult( stream.readBoolean(), stream.readBoolean(), stream.readDouble() );
	}

	@Override
	public void clientPacketData(INetworkInfo network, AppEngPacket packet, EntityPlayer player)
	{
		CompassManager.instance.postResult( attunement, cx << 4, cdy << 5, cz << 4, cr );
	}

	// api
	public PacketCompassResponse(PacketCompassRequest req, boolean hasResult, boolean spin, double radians) {

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeLong( this.attunement = req.attunement );
		data.writeInt( this.cx = req.cx );
		data.writeInt( this.cz = req.cz );
		data.writeInt( this.cdy = req.cdy );

		data.writeBoolean( hasResult );
		data.writeBoolean( spin );
		data.writeDouble( radians );

		configureWrite( data );

	}
}