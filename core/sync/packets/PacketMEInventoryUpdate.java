package appeng.core.sync.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.core.sync.AppEngPacket;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketMEInventoryUpdate extends AppEngPacket
{

	// output...
	final private ByteArrayOutputStream bytes;
	final private DataOutputStream data;
	boolean empty = true;

	// input.
	final List<IAEItemStack> list;

	// automatic.
	public PacketMEInventoryUpdate(DataInputStream stream) throws IOException {
		bytes = null;
		data = null;
		list = new LinkedList();
		while (stream.available() > 0)
			list.add( AEItemStack.loadItemStackFromPacket( stream ) );
		empty = list.isEmpty();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPacketData(INetworkManager network, AppEngPacket packet, Player player)
	{
		GuiScreen gs = Minecraft.getMinecraft().currentScreen;
		if ( gs instanceof GuiMEMonitorable )
		{
			((GuiMEMonitorable) gs).postUpdate( list );
		}
	}

	@Override
	public Packet250CustomPayload getPacket()
	{
		isChunkDataPacket = false;
		configureWrite( bytes.toByteArray() );
		return super.getPacket();
	}

	// api
	public PacketMEInventoryUpdate() throws IOException {
		bytes = new ByteArrayOutputStream();
		data = new DataOutputStream( bytes );
		list = null;
		data.writeInt( getPacketID() );
	}

	public void appendItem(IAEItemStack is) throws IOException
	{
		is.writeToPacket( data );
		empty = false;
	}

	public boolean isEmpty()
	{
		return empty;
	}

}
