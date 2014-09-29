package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.client.gui.implementations.GuiCraftingCPU;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.implementations.GuiNetworkStatus;
import appeng.core.AELog;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketMEInventoryUpdate extends AppEngPacket
{

	// output...
	final private byte ref;
	final private ByteBuf data;
	final private GZIPOutputStream compressFrame;

	int writtenBytes = 0;

	boolean empty = true;

	// input.
	final List<IAEItemStack> list;

	// automatic.
	public PacketMEInventoryUpdate(final ByteBuf stream) throws IOException {
		data = null;
		compressFrame = null;
		list = new LinkedList<IAEItemStack>();
		ref = stream.readByte();

		// int originalBytes = stream.readableBytes();

		GZIPInputStream gzReader = new GZIPInputStream( new InputStream() {

			@Override
			public int read() throws IOException
			{
				if ( stream.readableBytes() <= 0 )
					return -1;

				return (int) stream.readByte() & 0xff;
			}

		} );

		ByteBuf uncompressed = Unpooled.buffer( stream.readableBytes() );
		byte tmp[] = new byte[1024];
		while (gzReader.available() != 0)
		{
			int bytes = gzReader.read( tmp );
			if ( bytes > 0 )
				uncompressed.writeBytes( tmp, 0, bytes );
		}
		gzReader.close();

		// int uncompressedBytes = uncompressed.readableBytes();
		// AELog.info( "Receiver: " + originalBytes + " -> " + uncompressedBytes );

		while (uncompressed.readableBytes() > 0)
			list.add( AEItemStack.loadItemStackFromPacket( uncompressed ) );

		empty = list.isEmpty();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPacketData(INetworkInfo network, AppEngPacket packet, EntityPlayer player)
	{
		GuiScreen gs = Minecraft.getMinecraft().currentScreen;

		if ( gs instanceof GuiCraftConfirm )
			((GuiCraftConfirm) gs).postUpdate( list, ref );

		if ( gs instanceof GuiCraftingCPU )
			((GuiCraftingCPU) gs).postUpdate( list, ref );

		if ( gs instanceof GuiMEMonitorable )
			((GuiMEMonitorable) gs).postUpdate( list );

		if ( gs instanceof GuiNetworkStatus )
			((GuiNetworkStatus) gs).postUpdate( list );

	}

	@Override
	public FMLProxyPacket getProxy()
	{
		try
		{
			compressFrame.close();

			configureWrite( data );
			return super.getProxy();
		}
		catch (IOException e)
		{
			AELog.error( e );
		}
		return null;
	}

	// api
	public PacketMEInventoryUpdate() throws IOException {
		this( (byte) 0 );
	}

	// api
	public PacketMEInventoryUpdate(byte ref) throws IOException {

		data = Unpooled.buffer( 2048 );
		data.writeInt( getPacketID() );
		data.writeByte( this.ref = ref );

		compressFrame = new GZIPOutputStream( new OutputStream() {

			@Override
			public void write(int value) throws IOException
			{
				data.writeByte( value );
			}

		} );

		list = null;
	}

	public void appendItem(IAEItemStack is) throws IOException, BufferOverflowException
	{
		ByteBuf tmp = Unpooled.buffer( 2048 );
		is.writeToPacket( tmp );

		compressFrame.flush();
		if ( writtenBytes + tmp.readableBytes() > 2 * 1024 * 1024 ) // 2mb!
			throw new BufferOverflowException();
		else
		{
			writtenBytes += tmp.readableBytes();
			compressFrame.write( tmp.array(), 0, tmp.readableBytes() );
			empty = false;
		}
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
