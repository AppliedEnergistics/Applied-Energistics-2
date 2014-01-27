package appeng.core.sync.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.INetworkManager;
import net.minecraft.tileentity.TileEntity;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;

public class PacketSwitchGuis extends AppEngPacket
{

	final GuiBridge newGui;

	// automatic.
	public PacketSwitchGuis(DataInputStream stream) throws IOException {
		newGui = GuiBridge.values()[stream.readInt()];
	}

	@Override
	public void serverPacketData(INetworkManager manager, AppEngPacket packet, EntityPlayer player)
	{
		Container c = player.openContainer;
		if ( c instanceof AEBaseContainer )
		{
			AEBaseContainer bc = (AEBaseContainer) c;
			ContainerOpenContext context = bc.openContext;
			if ( context != null )
			{
				TileEntity te = context.w.getBlockTileEntity( context.x, context.y, context.z );
				Platform.openGUI( player, te, context.side, newGui );
			}
		}
	}

	// api
	public PacketSwitchGuis(GuiBridge newGui) throws IOException {

		this.newGui = newGui;

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream( bytes );

		data.writeInt( getPacketID() );
		data.writeInt( newGui.ordinal() );

		isChunkDataPacket = false;
		configureWrite( bytes.toByteArray() );
	}
}
