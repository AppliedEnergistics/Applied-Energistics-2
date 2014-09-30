package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import appeng.container.AEBaseContainer;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;

public class PacketSwapSlots extends AppEngPacket
{

	final int slotA;
	final int slotB;

	// automatic.
	public PacketSwapSlots(ByteBuf stream) throws IOException {
		slotA = stream.readInt();
		slotB = stream.readInt();
	}

	@Override
	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player)
	{
		if ( player != null && player.openContainer instanceof AEBaseContainer )
		{
			((AEBaseContainer) player.openContainer).swapSlotContents( slotA, slotB );
		}
	}

	// api
	public PacketSwapSlots(int slotA, int slotB) throws IOException {

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeInt( this.slotA = slotA );
		data.writeInt( this.slotB = slotB );

		configureWrite( data );
	}
}
