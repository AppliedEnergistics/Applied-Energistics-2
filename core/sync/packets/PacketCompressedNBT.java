package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import appeng.client.gui.implementations.GuiInterfaceTerminal;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketCompressedNBT extends AppEngPacket
{

	// output...
	final private ByteBuf data;
	final private GZIPOutputStream compressFrame;

	int writtenBytes = 0;

	boolean empty = true;

	// input.
	final NBTTagCompound in;

	// automatic.
	public PacketCompressedNBT(final ByteBuf stream) throws IOException {
		data = null;
		compressFrame = null;

		GZIPInputStream gzReader = new GZIPInputStream( new InputStream() {

			@Override
			public int read() throws IOException
			{
				if ( stream.readableBytes() <= 0 )
					return -1;

				return (int) stream.readByte() & 0xff;
			}

		} );

		in = CompressedStreamTools.read( new DataInputStream( gzReader ) );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPacketData(INetworkInfo network, AppEngPacket packet, EntityPlayer player)
	{
		GuiScreen gs = Minecraft.getMinecraft().currentScreen;

		if ( gs instanceof GuiInterfaceTerminal )
			((GuiInterfaceTerminal) gs).postUpdate( in );

	}

	// api
	public PacketCompressedNBT(NBTTagCompound din) throws IOException {

		data = Unpooled.buffer( 2048 );
		data.writeInt( getPacketID() );

		in = din;

		compressFrame = new GZIPOutputStream( new OutputStream() {

			@Override
			public void write(int value) throws IOException
			{
				data.writeByte( value );
			}

		} );

		CompressedStreamTools.write( din, new DataOutputStream( compressFrame ) );
		compressFrame.close();

		configureWrite( data );
	}

}
