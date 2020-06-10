package appeng.core.sync;


import appeng.core.AELog;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class AppEngCompressedPacket extends AppEngPacket
{
	protected static final int UNCOMPRESSED_PACKET_BYTE_LIMIT = 16 * 1024 * 1024;
	protected static final int OPERATION_BYTE_LIMIT = 2 * 1024;
	protected static final int TEMP_BUFFER_SIZE = 1024;
	private static final int STREAM_MASK = 0xff;


	// output...
	protected final byte ref;

	@Nullable
	private final PacketBuffer data;
	@Nullable
	private final GZIPOutputStream compressFrame;

	protected ByteBuf uncompressed;

	private int writtenBytes = 0;

	public AppEngCompressedPacket( final ByteBuf stream ) throws IOException
	{
		this.data = null;
		this.compressFrame = null;
		this.ref = stream.readByte();

		// int originalBytes = stream.readableBytes();

		try( GZIPInputStream gzReader = new GZIPInputStream( new InputStream()
		{
			@Override
			public int read()
			{
				if( stream.readableBytes() <= 0 )
				{
					return -1;
				}

				return stream.readByte() & STREAM_MASK;
			}
		} ) )
		{
			uncompressed = Unpooled.buffer( stream.readableBytes() );
			final byte[] tmp = new byte[TEMP_BUFFER_SIZE];

			while( gzReader.available() != 0 )
			{
				final int bytes = gzReader.read( tmp );

				if( bytes > 0 )
				{
					uncompressed.writeBytes( tmp, 0, bytes );
				}
			}
		}
	}

	// api
	public AppEngCompressedPacket() throws IOException
	{
		this( (byte) 0 );
	}

	// api
	public AppEngCompressedPacket( final byte ref ) throws IOException
	{
		this.ref = ref;
		this.data = new PacketBuffer(Unpooled.buffer(OPERATION_BYTE_LIMIT));
		this.data.writeByte( this.ref );

		this.compressFrame = new GZIPOutputStream( new OutputStream()
		{
			@Override
			public void write( final int value ) throws IOException
			{
				AppEngCompressedPacket.this.data.writeByte( value );
			}
		} );
	}

	@Override
	public void encode( PacketBuffer packetBuffer )
	{
		try
		{
			this.compressFrame.close();

			this.configureWrite( this.data );

			super.encode( packetBuffer );
		}
		catch( final IOException e )
		{
			AELog.debug( e );
		}
	}

	public void append( final ByteBuf buf ) throws IOException, BufferOverflowException
	{
		this.compressFrame.flush();
		if( this.writtenBytes + buf.readableBytes() > UNCOMPRESSED_PACKET_BYTE_LIMIT )
		{
			throw new BufferOverflowException();
		}
		else
		{
			this.writtenBytes += buf.readableBytes();
			this.compressFrame.write( buf.array(), 0, buf.readableBytes() );
		}
	}

	public int getLength()
	{
		return this.data.readableBytes();
	}
}
