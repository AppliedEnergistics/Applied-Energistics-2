package appeng.util.item;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import appeng.api.storage.data.IAEStack;

public abstract class AEStack<StackType extends IAEStack> implements IAEStack<StackType>
{

	protected boolean isCraftable;
	protected long stackSize;
	protected long countRequestable;

	@Override
	public boolean isMeaningful()
	{
		return stackSize != 0 || getCountRequestable() > 0 || isCraftable();
	}

	@Override
	public StackType reset()
	{
		stackSize = 0;
		// priority = Integer.MIN_VALUE;
		setCountRequestable( 0 );
		setCraftable( false );
		return (StackType) this;
	}

	@Override
	public long getStackSize()
	{
		return stackSize;
	}

	@Override
	public StackType setStackSize(long ss)
	{
		stackSize = ss;
		return (StackType) this;
	}

	@Override
	public long getCountRequestable()
	{
		return countRequestable;
	}

	@Override
	public StackType setCountRequestable(long countRequestable)
	{
		this.countRequestable = countRequestable;
		return (StackType) this;
	}

	@Override
	public boolean isCraftable()
	{
		return isCraftable;
	}

	@Override
	public StackType setCraftable(boolean isCraftable)
	{
		this.isCraftable = isCraftable;
		return (StackType) this;
	}

	@Override
	public void decStackSize(long i)
	{
		stackSize -= i;
	}

	@Override
	public void incStackSize(long i)
	{
		stackSize += i;
	}

	@Override
	public void decCountRequestable(long i)
	{
		countRequestable -= i;
	}

	@Override
	public void incCountRequestable(long i)
	{
		countRequestable += i;
	}

	void putPacketValue(ByteBuf tag, long num)
	{
		if ( num <= 255 )
			tag.writeByte( (byte) (num + Byte.MIN_VALUE) );
		else if ( num <= 65535 )
			tag.writeShort( (short) (num + Short.MIN_VALUE) );
		else if ( num <= 4294967295L )
			tag.writeInt( (int) (num + Integer.MIN_VALUE) );
		else
			tag.writeLong( num );
	}

	static long getPacketValue(byte type, ByteBuf tag)
	{
		if ( type == 0 )
		{
			long l = tag.readByte();
			l -= Byte.MIN_VALUE;
			return l;
		}
		else if ( type == 1 )
		{
			long l = tag.readShort();
			l -= Short.MIN_VALUE;
			return l;
		}
		else if ( type == 2 )
		{
			long l = tag.readInt();
			l -= Integer.MIN_VALUE;
			return l;
		}

		return tag.readLong();
	}

	byte getType(long num)
	{
		if ( num <= 255 )
			return 0;
		else if ( num <= 65535 )
			return 1;
		else if ( num <= 4294967295L )
			return 2;
		else
			return 3;
	}

	abstract void writeIdentity(ByteBuf i) throws IOException;

	abstract void readNBT(ByteBuf i) throws IOException;

	abstract boolean hasTagCompound();

	@Override
	public void writeToPacket(ByteBuf i) throws IOException
	{
		byte mask = (byte) (getType( 0 ) | (getType( stackSize ) << 2) | (getType( getCountRequestable() ) << 4) | ((byte) (isCraftable ? 1 : 0) << 6) | (hasTagCompound() ? 1
				: 0) << 7);

		i.writeByte( mask );
		writeIdentity( i );

		readNBT( i );

		// putPacketValue( i, priority );
		putPacketValue( i, stackSize );
		putPacketValue( i, getCountRequestable() );
	}

}
