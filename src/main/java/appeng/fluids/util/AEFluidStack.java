/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.fluids.util;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.Api;
import appeng.fluids.items.FluidDummyItem;
import appeng.util.Platform;
import appeng.util.item.AEStack;


public final class AEFluidStack extends AEStack<IAEFluidStack> implements IAEFluidStack, Comparable<AEFluidStack>
{

	private final Fluid fluid;
	private NBTTagCompound tagCompound;

	private AEFluidStack( final AEFluidStack is )
	{
		this.fluid = is.fluid;
		this.setStackSize( is.getStackSize() );

		// priority = is.priority;
		this.setCraftable( is.isCraftable() );
		this.setCountRequestable( is.getCountRequestable() );

		if( is.hasTagCompound() )
		{
			this.tagCompound = is.tagCompound.copy();
		}
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

		if( is.tag != null )
		{
			this.tagCompound = is.tag.copy();
		}
	}

	public static AEFluidStack fromFluidStack( final FluidStack input )
	{
		if( input == null )
		{
			return null;
		}

		return new AEFluidStack( input );
	}

	public static IAEFluidStack fromNBT( final NBTTagCompound i )
	{
		final FluidStack fluidStack = FluidStack.loadFluidStackFromNBT( i );

		if( fluidStack == null )
		{
			return null;
		}

		final AEFluidStack fluid = AEFluidStack.fromFluidStack( fluidStack );
		fluid.setStackSize( i.getLong( "Cnt" ) );
		fluid.setCountRequestable( i.getLong( "Req" ) );
		fluid.setCraftable( i.getBoolean( "Craft" ) );

		if( fluid.hasTagCompound() )
		{
			fluid.tagCompound = fluid.tagCompound.copy();
		}

		return fluid;
	}

	public static IAEFluidStack fromPacket( final ByteBuf data ) throws IOException
	{
		final byte mask = data.readByte();
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
			d.setTag( "Tag", CompressedStreamTools.read( di ) );
		}

		final long stackSize = getPacketValue( stackType, data );
		final long countRequestable = getPacketValue( countReqType, data );

		final FluidStack fluidStack = FluidStack.loadFluidStackFromNBT( d );

		if( fluidStack == null )
		{
			return null;
		}

		final AEFluidStack fluid = AEFluidStack.fromFluidStack( fluidStack );
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
		this.incStackSize( option.getStackSize() );
		this.setCountRequestable( this.getCountRequestable() + option.getCountRequestable() );
		this.setCraftable( this.isCraftable() || option.isCraftable() );
	}

	@Override
	public void writeToNBT( final NBTTagCompound i )
	{
		i.setString( "FluidName", this.fluid.getName() );
		i.setByte( "Count", (byte) 0 );
		i.setLong( "Cnt", this.getStackSize() );
		i.setLong( "Req", this.getCountRequestable() );
		i.setBoolean( "Craft", this.isCraftable() );

		if( this.hasTagCompound() )
		{
			i.setTag( "Tag", this.tagCompound );
		}
		else
		{
			i.removeTag( "Tag" );
		}
	}

	@Override
	public boolean fuzzyComparison( final IAEFluidStack other, final FuzzyMode mode )
	{
		return this.fluid == other.getFluid();
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
	public IStorageChannel<IAEFluidStack> getChannel()
	{
		return AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class );
	}

	@Override
	public int compareTo( final AEFluidStack b )
	{
		if( this.fluid != b.fluid )
		{
			return this.fluid.getName().compareTo( b.fluid.getName() );
		}

		if( Platform.itemComparisons().isNbtTagEqual( this.tagCompound, b.tagCompound ) )
		{
			return 0;
		}

		return this.tagCompound.hashCode() - b.tagCompound.hashCode();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( this.fluid == null ) ? 0 : this.fluid.hashCode() );
		result = prime * result + ( ( this.tagCompound == null ) ? 0 : this.tagCompound.hashCode() );

		return result;
	}

	@Override
	public boolean equals( final Object ia )
	{
		if( ia instanceof AEFluidStack )
		{
			final AEFluidStack is = (AEFluidStack) ia;
			return is.fluid == this.fluid && Platform.itemComparisons().isNbtTagEqual( this.tagCompound, is.tagCompound );
		}
		else if( ia instanceof FluidStack )
		{
			final FluidStack is = (FluidStack) ia;
			return is.getFluid() == this.fluid && Platform.itemComparisons().isNbtTagEqual( this.tagCompound, is.tag );
		}
		return false;
	}

	@Override
	public String toString()
	{
		return this.getStackSize() + "x" + this.getFluidStack().getFluid().getName() + " " + this.tagCompound;
	}

	@Override
	public boolean hasTagCompound()
	{
		return this.tagCompound != null;
	}

	@Override
	public FluidStack getFluidStack()
	{
		final int amount = (int) Math.min( Integer.MAX_VALUE, this.getStackSize() );
		final FluidStack is = new FluidStack( this.fluid, amount, this.tagCompound );

		return is;
	}

	@Override
	public Fluid getFluid()
	{
		return this.fluid;
	}

	@Override
	public ItemStack asItemStackRepresentation()
	{
		ItemStack is = Api.INSTANCE.definitions().items().dummyFluidItem().maybeStack( 1 ).orElse( ItemStack.EMPTY );
		if( !is.isEmpty() )
		{
			FluidDummyItem item = (FluidDummyItem) is.getItem();
			item.setFluidStack( is, this.getFluidStack() );
			return is;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void writeToPacket( final ByteBuf i ) throws IOException
	{
		final byte mask = (byte) ( ( this.getType( this.getStackSize() ) << 2 ) | ( this
				.getType( this.getCountRequestable() ) << 4 ) | ( (byte) ( this.isCraftable() ? 1 : 0 ) << 6 ) | ( this.hasTagCompound() ? 1 : 0 ) << 7 );

		i.writeByte( mask );

		this.writeToStream( i );

		this.putPacketValue( i, this.getStackSize() );
		this.putPacketValue( i, this.getCountRequestable() );
	}

	private void writeToStream( final ByteBuf i ) throws IOException
	{
		final byte[] name = this.fluid.getName().getBytes( "UTF-8" );
		i.writeByte( (byte) name.length );
		i.writeBytes( name );
		if( this.hasTagCompound() )
		{
			final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			final DataOutputStream data = new DataOutputStream( bytes );

			CompressedStreamTools.write( this.tagCompound, data );

			final byte[] tagBytes = bytes.toByteArray();
			final int size = tagBytes.length;

			i.writeInt( size );
			i.writeBytes( tagBytes );
		}
	}
}
