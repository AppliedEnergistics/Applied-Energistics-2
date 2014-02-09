package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.implementations.GuiNetworkStatus;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketMEInventoryUpdate extends AppEngPacket
{

	// output...
	final private ByteBuf data;
	int lastSize = 0;
	boolean empty = true;

	// input.
	final List<IAEItemStack> list;

	// automatic.
	public PacketMEInventoryUpdate(ByteBuf stream) throws IOException {
		data = null;
		list = new LinkedList();
		while (stream.readableBytes() > 0)
			list.add( AEItemStack.loadItemStackFromPacket( stream ) );
		empty = list.isEmpty();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPacketData(INetworkInfo network, AppEngPacket packet, EntityPlayer player)
	{
		GuiScreen gs = Minecraft.getMinecraft().currentScreen;

		if ( gs instanceof GuiMEMonitorable )
			((GuiMEMonitorable) gs).postUpdate( list );

		if ( gs instanceof GuiNetworkStatus )
			((GuiNetworkStatus) gs).postUpdate( list );

	}

	@Override
	public FMLProxyPacket getProxy()
	{
		data.capacity( lastSize );
		configureWrite( data );
		return super.getProxy();
	}

	// api
	public PacketMEInventoryUpdate() throws IOException {
		data = Unpooled.buffer( 2048 );
		list = null;
		data.writeInt( getPacketID() );
		lastSize = data.readableBytes();
	}

	public void appendItem(IAEItemStack is) throws IOException, BufferOverflowException
	{
		is.writeToPacket( data );
		empty = false;
		if ( data.readableBytes() > 20000 )
			throw new BufferOverflowException();
		else
			lastSize = data.readableBytes();
	}

	public int getLength()
	{
		return data.readableBytes();
	}

	public boolean isEmpty()
	{
		return empty;
	}

}
