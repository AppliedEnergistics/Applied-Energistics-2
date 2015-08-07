//
//package appeng.core.sync.packets;
//
//
//import io.netty.buffer.ByteBuf;
//
//import appeng.core.sync.AppEngPacket;
//
//
///**
// * An abstract packet providing basic information about an {@link AppEngPacket}.
// *
// */
//public abstract class AbstractPacket implements AppEngPacket
//{
//
//	private int writtenBytes = 0;
//
//	/**
//	 * Ensure that this method is called from any overridden one as last instruction.
//	 */
//	@Override
//	public void fromBytes( ByteBuf buf )
//	{
//		this.writtenBytes = buf.readableBytes();
//	}
//
//	/**
//	 * Ensure that this method is called from any overridden one as last instruction.
//	 */
//	@Override
//	public void toBytes( ByteBuf buf )
//	{
//		this.writtenBytes = buf.readableBytes();
//	}
//
//	@Override
//	public int getSize()
//	{
//		return this.writtenBytes;
//	}
//
// }
