package appeng.util.item;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAETagCompound;
import appeng.util.Platform;

import com.google.common.io.ByteStreams;

public final class AEFluidStack extends AEStack<IAEFluidStack> implements IAEFluidStack, Comparable<AEFluidStack>
{

	public int myHash;
	Fluid fluid;
	protected IAETagCompound tagCompound;

	@Override
	public IAETagCompound getTagCompound()
	{
		return tagCompound;
	}

	@Override
	public void add(IAEFluidStack option)
	{
		if ( option == null )
			return;

		// if ( priority < ((AEFluidStack) option).priority )
		// priority = ((AEFluidStack) option).priority;

		incStackSize( option.getStackSize() );
		setCountRequestable( getCountRequestable() + option.getCountRequestable() );
		setCraftable( isCraftable() || option.isCraftable() );
	}

	private AEFluidStack(AEFluidStack is) {

		fluid = is.fluid;
		stackSize = is.stackSize;

		// priority = is.priority;
		setCraftable( is.isCraftable() );
		setCountRequestable( is.getCountRequestable() );

		this.myHash = is.myHash;
	}

	protected AEFluidStack(FluidStack is) {
		if ( is == null )
			throw new RuntimeException( "Invalid Itemstack." );

		fluid = is.getFluid();

		if ( fluid == null )
			throw new RuntimeException( "Fluid is null." );

		stackSize = is.amount;
		setCraftable( false );
		setCountRequestable( 0 );

		myHash = fluid.hashCode() ^ (tagCompound == null ? 0 : System.identityHashCode( tagCompound ));
	}

	public static AEFluidStack create(Object a)
	{
		if ( a == null )
			return null;
		if ( a instanceof AEFluidStack )
			((AEFluidStack) a).copy();
		if ( a instanceof FluidStack )
			return new AEFluidStack( (FluidStack) a );
		return null;
	}

	@Override
	public boolean equals(Object ia)
	{
		if ( ia instanceof AEFluidStack )
		{
			return ((AEFluidStack) ia).fluid == fluid && tagCompound == ((AEFluidStack) ia).tagCompound;
		}
		else if ( ia instanceof FluidStack )
		{
			FluidStack is = (FluidStack) ia;

			if ( is.fluidID == this.fluid.getID() )
			{
				NBTTagCompound ta = (NBTTagCompound) tagCompound;
				NBTTagCompound tb = is.tag;
				if ( ta == tb )
					return true;

				if ( (ta == null && tb == null) || (ta != null && ta.hasNoTags() && tb == null) || (tb != null && tb.hasNoTags() && ta == null)
						|| (ta != null && ta.hasNoTags() && tb != null && tb.hasNoTags()) )
					return true;

				if ( (ta == null && tb != null) || (ta != null && tb == null) )
					return false;

				if ( AESharedNBT.isShared( tb ) )
					return ta == tb;

				return Platform.NBTEqualityTest( ta, tb );
			}
		}
		return false;
	}

	@Override
	public FluidStack getFluidStack()
	{
		FluidStack is = new FluidStack( fluid, (int) Math.min( Integer.MAX_VALUE, stackSize ) );
		if ( tagCompound != null )
			is.tag = tagCompound.getNBTTagCompoundCopy();

		return is;
	}

	@Override
	public IAEFluidStack copy()
	{
		return new AEFluidStack( this );
	}

	@Override
	public void writeToNBT(NBTTagCompound i)
	{
		/*
		 * Mojang Fucked this over ; GC Optimization - Ugly Yes, but it saves a lot in the memory department.
		 */

		/*
		 * NBTBase FluidName = i.getTag( "FluidName" ); NBTBase Count = i.getTag( "Count" ); NBTBase Cnt = i.getTag(
		 * "Cnt" ); NBTBase Req = i.getTag( "Req" ); NBTBase Craft = i.getTag( "Craft" );
		 */

		/*
		 * if ( FluidName != null && FluidName instanceof NBTTagString ) ((NBTTagString) FluidName).data = (String)
		 * this.fluid.getName(); else
		 */
		i.setString( "FluidName", (String) this.fluid.getName() );

		/*
		 * if ( Count != null && Count instanceof NBTTagByte ) ((NBTTagByte) Count).data = (byte) 0; else
		 */
		i.setByte( "Count", (byte) 0 );

		/*
		 * if ( Cnt != null && Cnt instanceof NBTTagLong ) ((NBTTagLong) Cnt).data = this.stackSize; else
		 */
		i.setLong( "Cnt", this.stackSize );

		/*
		 * if ( Req != null && Req instanceof NBTTagLong ) ((NBTTagLong) Req).data = this.stackSize; else
		 */
		i.setLong( "Req", this.getCountRequestable() );

		/*
		 * if ( Craft != null && Craft instanceof NBTTagByte ) ((NBTTagByte) Craft).data = (byte) (this.isCraftable() ?
		 * 1 : 0); else
		 */i.setBoolean( "Craft", this.isCraftable() );

		if ( tagCompound != null )
			i.setTag( "tag", (NBTTagCompound) tagCompound );
		else
			i.removeTag( "tag" );

	}

	public static IAEFluidStack loadFluidStackFromNBT(NBTTagCompound i)
	{
		ItemStack itemstack = ItemStack.loadItemStackFromNBT( i );
		if ( itemstack == null )
			return null;
		AEFluidStack aeis = AEFluidStack.create( itemstack );
		// aeis.priority = i.getInteger( "Priority" );
		aeis.stackSize = i.getLong( "Cnt" );
		aeis.setCountRequestable( i.getLong( "Req" ) );
		aeis.setCraftable( i.getBoolean( "Craft" ) );
		return aeis;
	}

	@Override
	public boolean hasTagCompound()
	{
		return tagCompound != null;
	}

	@Override
	public int hashCode()
	{
		return myHash;
	}

	@Override
	public int compareTo(AEFluidStack b)
	{
		int diff = hashCode() - b.hashCode();
		return diff > 0 ? 1 : (diff < 0 ? -1 : 0);
	}

	@Override
	void writeIdentity(ByteBuf i) throws IOException
	{
		byte[] name = fluid.getName().getBytes( "UTF-8" );
		i.writeByte( (byte) name.length );
		i.writeBytes( name );
	}

	@Override
	void readNBT(ByteBuf i) throws IOException
	{
		if ( hasTagCompound() )
		{
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream( bytes );

			CompressedStreamTools.write( (NBTTagCompound) getTagCompound(), data );

			byte[] tagBytes = bytes.toByteArray();
			int size = tagBytes.length;

			i.writeInt( size );
			i.writeBytes( tagBytes );
		}
	}

	public static IAEFluidStack loadFluidStackFromPacket(ByteBuf data) throws IOException
	{
		byte mask = data.readByte();
		// byte PriorityType = (byte) (mask & 0x03);
		byte StackType = (byte) ((mask & 0x0C) >> 2);
		byte CountReqType = (byte) ((mask & 0x30) >> 4);
		boolean isCraftable = (mask & 0x40) > 0;
		boolean hasTagCompound = (mask & 0x80) > 0;

		// don't send this...
		NBTTagCompound d = new NBTTagCompound();

		byte len2 = data.readByte();
		byte name[] = new byte[len2];
		data.readBytes( name, 0, len2 );

		d.setString( "FluidName", new String( name, "UTF-8" ) );
		d.setByte( "Count", (byte) 0 );

		if ( hasTagCompound )
		{
			int len = data.readInt();

			byte[] bd = new byte[len];
			data.readBytes( bd );

			DataInput di = ByteStreams.newDataInput( bd );
			d.setTag( "tag", CompressedStreamTools.read( di ) );
		}

		// long priority = getPacketValue( PriorityType, data );
		long stackSize = getPacketValue( StackType, data );
		long countRequestable = getPacketValue( CountReqType, data );

		FluidStack fluidStack = FluidStack.loadFluidStackFromNBT( d );
		if ( fluidStack == null )
			return null;

		AEFluidStack aeis = AEFluidStack.create( fluidStack );
		// aeis.priority = (int) priority;
		aeis.stackSize = stackSize;
		aeis.setCountRequestable( countRequestable );
		aeis.setCraftable( isCraftable );
		return aeis;
	}

	@Override
	public Fluid getFluid()
	{
		return fluid;
	}

	@Override
	public IAEFluidStack empty()
	{
		IAEFluidStack dup = copy();
		dup.reset();
		return dup;
	}

	@Override
	public boolean fuzzyComparison(Object st, FuzzyMode mode)
	{
		if ( st instanceof FluidStack )
		{
			return ((FluidStack) st).getFluid() == getFluid();
		}

		if ( st instanceof IAEFluidStack )
		{
			return ((IAEFluidStack) st).getFluid() == getFluid();
		}

		return false;
	}

	@Override
	public boolean isItem()
	{
		return false;
	}

	@Override
	public boolean isFluid()
	{
		return true;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.FLUIDS;
	}

}
