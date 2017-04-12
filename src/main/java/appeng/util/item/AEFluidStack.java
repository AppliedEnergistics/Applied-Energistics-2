/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.util.item;


import appeng.api.config.FuzzyMode;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAETagCompound;
import appeng.util.Platform;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.io.*;


public final class AEFluidStack extends AEStack<IAEFluidStack> implements IAEFluidStack, Comparable<AEFluidStack>
{

	private final int myHash;
	private final Fluid fluid;
	private IAETagCompound tagCompound;

	private AEFluidStack( final AEFluidStack is )
	{

		this.fluid = is.fluid;
		this.setStackSize( is.getStackSize() );

		// priority = is.priority;
		this.setCraftable( is.isCraftable() );
		this.setCountRequestable( is.getCountRequestable() );

		this.myHash = is.myHash;
	}

	private AEFluidStack( @Nonnull final FluidStack is )
	{
		this.fluid = is.getFluid();

		if( this.fluid == null )
		{
			throw new IllegalArgumentException( "Fluid is null." );
		}

		this.setStackSize( is.amount );
		this.setCraftable( false );
		this.setCountRequestable( 0 );

		this.myHash = this.fluid.hashCode() ^ ( this.tagCompound == null ? 0 : System.identityHashCode( this.tagCompound ) );
	}

	public static IAEFluidStack loadFluidStackFromNBT( final NBTTagCompound i )
	{
		final ItemStack itemstack = ItemStack.loadItemStackFromNBT( i );
		if( itemstack == null )
		{
			return null;
		}
		final AEFluidStack fluid = AEFluidStack.create( itemstack );
		// fluid.priority = i.getInteger( "Priority" );
		fluid.setStackSize( i.getLong( "Cnt" ) );
		fluid.setCountRequestable( i.getLong( "Req" ) );
		fluid.setCraftable( i.getBoolean( "Craft" ) );
		return fluid;
	}

	public static AEFluidStack create( final Object a )
	{
		if( a == null )
		{
			return null;
		}
		if( a instanceof AEFluidStack )
		{
			( (IAEStack<IAEFluidStack>) a ).copy();
		}
		if( a instanceof FluidStack )
		{
			return new AEFluidStack( (FluidStack) a );
		}
		return null;
	}

	public static IAEFluidStack loadFluidStackFromPacket( final ByteBuf data ) throws IOException
	{
		final byte mask = data.readByte();
		// byte PriorityType = (byte) (mask & 0x03);
		final byte stackType = (byte) ( ( mask & 0x0C ) >> 2 );
		final byte countReqType = (byte) ( ( mask & 0x30 ) >> 4 );
		final boolean isCraftable = ( mask & 0x40 ) > 0;
		final boolean hasTagCompound = ( mask & 0x80 ) > 0;

		// don't send this...
		final NBTTagCompound d = new NBTTagCompound();

		final byte len2 = data.readByte();
		final byte[] name = new byte[len2];
		data.readBytes( name, 0, len2 );

		d.setString( "FluidName", new String( name, "UTF-8" ) );
		d.setByte( "Count", (byte) 0 );

		if( hasTagCompound )
		{
			final int len = data.readInt();

			final byte[] bd = new byte[len];
			data.readBytes( bd );

			final DataInputStream di = new DataInputStream( new ByteArrayInputStream( bd ) );
			d.setTag( "tag", CompressedStreamTools.read( di ) );
		}

		// long priority = getPacketValue( PriorityType, data );
		final long stackSize = getPacketValue( stackType, data );
		final long countRequestable = getPacketValue( countReqType, data );

		final FluidStack fluidStack = FluidStack.loadFluidStackFromNBT( d );
		if( fluidStack == null )
		{
			return null;
		}

		final AEFluidStack fluid = AEFluidStack.create( fluidStack );
		// fluid.priority = (int) priority;
		fluid.setStackSize( stackSize );
		fluid.setCountRequestable( countRequestable );
		fluid.setCraftable( isCraftable );
		return fluid;
	}

	@Override
	public void add( final IAEFluidStack option )
	{
		if( option == null )
		{
			return;
		}

		// if ( priority < ((AEFluidStack) option).priority )
		// priority = ((AEFluidStack) option).priority;

		this.incStackSize( option.getStackSize() );
		this.setCountRequestable( this.getCountRequestable() + option.getCountRequestable() );
		this.setCraftable( this.isCraftable() || option.isCraftable() );
	}

	@Override
	public void writeToNBT( final NBTTagCompound i )
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
		i.setString( "FluidName", this.fluid.getName() );

		/*
		 * if ( Count != null && Count instanceof NBTTagByte ) ((NBTTagByte) Count).data = (byte) 0; else
		 */
		i.setByte( "Count", (byte) 0 );

		/*
		 * if ( Cnt != null && Cnt instanceof NBTTagLong ) ((NBTTagLong) Cnt).data = this.stackSize; else
		 */
		i.setLong( "Cnt", this.getStackSize() );

		/*
		 * if ( Req != null && Req instanceof NBTTagLong ) ((NBTTagLong) Req).data = this.stackSize; else
		 */
		i.setLong( "Req", this.getCountRequestable() );

		/*
		 * if ( Craft != null && Craft instanceof NBTTagByte ) ((NBTTagByte) Craft).data = (byte) (this.isCraftable() ?
		 * 1 : 0); else
		 */
		i.setBoolean( "Craft", this.isCraftable() );

		if( this.tagCompound != null )
		{
			i.setTag( "tag", (NBTBase) this.tagCompound );
		}
		else
		{
			i.removeTag( "tag" );
		}
	}

	@Override
	public boolean fuzzyComparison( final Object st, final FuzzyMode mode )
	{
		if( st instanceof FluidStack )
		{
			return ( (FluidStack) st ).getFluid() == this.fluid;
		}

		if( st instanceof IAEFluidStack )
		{
			return ( (IAEFluidStack) st ).getFluid() == this.fluid;
		}

		return false;
	}

	@Override
	public IAEFluidStack copy()
	{
		return new AEFluidStack( this );
	}

	@Override
	public IAEFluidStack empty()
	{
		final IAEFluidStack dup = this.copy();
		dup.reset();
		return dup;
	}

	@Override
	public IAETagCompound getTagCompound()
	{
		return this.tagCompound;
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

	@Override
	public int compareTo( final AEFluidStack b )
	{
		final int diff = this.hashCode() - b.hashCode();
		return diff > 0 ? 1 : ( diff < 0 ? -1 : 0 );
	}

	@Override
	public int hashCode()
	{
		return this.myHash;
	}

	@Override
	public boolean equals( final Object ia )
	{
		if( ia instanceof AEFluidStack )
		{
			return ( (AEFluidStack) ia ).fluid == this.fluid && this.tagCompound == ( (AEFluidStack) ia ).tagCompound;
		}
		else if( ia instanceof FluidStack )
		{
			final FluidStack is = (FluidStack) ia;

			if( is.getFluidID() == this.fluid.getID() )
			{
				final NBTTagCompound ta = (NBTTagCompound) this.tagCompound;
				final NBTTagCompound tb = is.tag;
				if( ta == tb )
				{
					return true;
				}

				if( ( ta == null && tb == null ) || ( ta != null && ta.hasNoTags() && tb == null ) || ( tb != null && tb.hasNoTags() && ta == null ) || ( ta != null && ta.hasNoTags() && tb != null && tb.hasNoTags() ) )
				{
					return true;
				}

				if( ( ta == null && tb != null ) || ( ta != null && tb == null ) )
				{
					return false;
				}

				if( AESharedNBT.isShared( tb ) )
				{
					return ta == tb;
				}

				return Platform.NBTEqualityTest( ta, tb );
			}
		}
		return false;
	}

	@Override
	public String toString()
	{
		return this.getFluidStack().toString();
	}

	@Override
	public boolean hasTagCompound()
	{
		return this.tagCompound != null;
	}

	@Override
	void writeIdentity( final ByteBuf i ) throws IOException
	{
		final byte[] name = this.fluid.getName().getBytes( "UTF-8" );
		i.writeByte( (byte) name.length );
		i.writeBytes( name );
	}

	@Override
	void readNBT( final ByteBuf i ) throws IOException
	{
		if( this.hasTagCompound() )
		{
			final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			final DataOutputStream data = new DataOutputStream( bytes );

			CompressedStreamTools.write( (NBTTagCompound) this.tagCompound, data );

			final byte[] tagBytes = bytes.toByteArray();
			final int size = tagBytes.length;

			i.writeInt( size );
			i.writeBytes( tagBytes );
		}
	}

	@Override
	public FluidStack getFluidStack()
	{
		final FluidStack is = new FluidStack( this.fluid, (int) Math.min( Integer.MAX_VALUE, this.getStackSize() ) );
		if( this.tagCompound != null )
		{
			is.tag = this.tagCompound.getNBTTagCompoundCopy();
		}

		return is;
	}

	@Override
	public Fluid getFluid()
	{
		return this.fluid;
	}
}
