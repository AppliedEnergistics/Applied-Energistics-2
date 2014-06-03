package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import appeng.client.gui.AEBaseGui;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.Platform;

public class PacketSwitchGuis extends AppEngPacket
{

	final GuiBridge newGui;

	// automatic.
	public PacketSwitchGuis(ByteBuf stream) throws IOException {
		newGui = GuiBridge.values()[stream.readInt()];
	}

	@Override
	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player)
	{
		Container c = player.openContainer;
		if ( c instanceof AEBaseContainer )
		{
			AEBaseContainer bc = (AEBaseContainer) c;
			ContainerOpenContext context = bc.openContext;
			if ( context != null )
			{
				TileEntity te = context.w.getTileEntity( context.x, context.y, context.z );
				Platform.openGUI( player, te, context.side, newGui );
			}
		}
	}

	// api
	public PacketSwitchGuis(GuiBridge newGui) throws IOException {

		this.newGui = newGui;

		AEBaseGui.switchingGuis = true;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeInt( newGui.ordinal() );

		configureWrite( data );
	}
}
