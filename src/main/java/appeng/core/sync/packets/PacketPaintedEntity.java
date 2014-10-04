package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import appeng.api.util.AEColor;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.hooks.TickHandler;
import appeng.hooks.TickHandler.PlayerColor;

public class PacketPaintedEntity extends AppEngPacket
{

	private final AEColor myColor;
	private final int entityId;
	private int ticks;

	// automatic.
	public PacketPaintedEntity(ByteBuf stream)
	{
		entityId = stream.readInt();
		myColor = AEColor.values()[stream.readByte()];
		ticks = stream.readInt();
	}

	@Override
	public void clientPacketData(INetworkInfo network, AppEngPacket packet, EntityPlayer player)
	{
		PlayerColor pc = new PlayerColor( entityId, myColor, ticks );
		TickHandler.instance.getPlayerColors().put( entityId, pc );
	}

	// api
	public PacketPaintedEntity(int myEntity, AEColor myColor, int ticksLeft) {

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeInt( this.entityId = myEntity );
		data.writeByte( (this.myColor = myColor).ordinal() );
		data.writeInt( ticksLeft );

		configureWrite( data );
	}
}
