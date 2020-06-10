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

package appeng.util.item;


import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.ItemHandlerHelper;

import appeng.api.AEApi;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.Platform;



public final class AEItemStack extends AEStack<IAEItemStack> implements IAEItemStack
{
	private AESharedItemStack sharedStack;

	@OnlyIn( Dist.CLIENT )
	private String displayName;
	@OnlyIn( Dist.CLIENT )
	private List<String> tooltip;
	@OnlyIn( Dist.CLIENT )
	private ResourceLocation uniqueID;

	private AEItemStack( final AEItemStack is )
	{
		this.setStackSize( is.getStackSize() );
		this.setCraftable( is.isCraftable() );
		this.setCountRequestable( is.getCountRequestable() );
		this.sharedStack = is.sharedStack;
	}

	private AEItemStack( final AESharedItemStack is, long size )
	{
		this.sharedStack = is;
		this.setStackSize( size );
		this.setCraftable( false );
		this.setCountRequestable( 0 );
	}

	@Nullable
	public static AEItemStack fromItemStack( @Nonnull final ItemStack stack )
	{
		if( stack.isEmpty() )
		{
			return null;
		}

		return new AEItemStack( AEItemStackRegistry.getRegisteredStack( stack ), stack.getCount() );
	}

	public static IAEItemStack fromNBT( final CompoundNBT i )
	{
		if( i == null )
		{
			return null;
		}

		final ItemStack itemstack = ItemStack.read( i );
		if( itemstack.isEmpty() )
		{
			return null;
		}

		final AEItemStack item = AEItemStack.fromItemStack( itemstack );
		item.setStackSize( i.getLong( "Cnt" ) );
		item.setCountRequestable( i.getLong( "Req" ) );
		item.setCraftable( i.getBoolean( "Craft" ) );
		return item;
	}

	@Override
	public void write( final CompoundNBT i )
	{
		this.getDefinition().write( i );
		i.putLong( "Cnt", this.getStackSize() );
		i.putLong( "Req", this.getCountRequestable() );
		i.putBoolean( "Craft", this.isCraftable() );
	}

	public static AEItemStack fromPacket( final ByteBuf data )
	{
		final byte mask = data.readByte();
		final byte stackType = (byte) ( ( mask & 0x0C ) >> 2 );
		final byte countReqType = (byte) ( ( mask & 0x30 ) >> 4 );
		final boolean isCraftable = ( mask & 0x40 ) > 0;

		final ItemStack itemstack = new ItemStack( ByteBufUtils.readTag( data ) );
		final long stackSize = getPacketValue( stackType, data );
		final long countRequestable = getPacketValue( countReqType, data );

		if( itemstack.isEmpty() )
		{
			return null;
		}

		final AEItemStack item = new AEItemStack( AEItemStackRegistry.getRegisteredStack( itemstack ), stackSize );
		item.setCountRequestable( countRequestable );
		item.setCraftable( isCraftable );
		return item;
	}

	@Override
	public void writeToPacket( final ByteBuf i )
	{
		final byte mask = (byte) ( ( this.getType( this.getStackSize() ) << 2 ) | ( this
				.getType( this.getCountRequestable() ) << 4 ) | ( (byte) ( this.isCraftable() ? 1 : 0 ) << 6 ) | ( this.hasTagCompound() ? 1 : 0 ) << 7 );

		i.writeByte( mask );

		ByteBufUtils.writeTag( i, this.getDefinition().serializeNBT() );
		this.putPacketValue( i, this.getStackSize() );
		this.putPacketValue( i, this.getCountRequestable() );
	}

	@Override
	public void add( final IAEItemStack option )
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
	public boolean tagComparison( final IAEItemStack other )
	{
		return ItemTagHelper.INSTANCE.isSimilarItem( this, other );
	}

	@Override
	public IAEItemStack copy()
	{
		return new AEItemStack( this );
	}

	@Override
	public boolean isItem()
	{
		return true;
	}

	@Override
	public boolean isFluid()
	{
		return false;
	}

	@Override
	public IStorageChannel<IAEItemStack> getChannel()
	{
		return AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class );
	}

	@Override
	public ItemStack createItemStack()
	{
		return ItemHandlerHelper.copyStackWithSize( this.getDefinition(), (int) Math.min( Integer.MAX_VALUE, this.getStackSize() ) );
	}

	@Override
	public Item getItem()
	{
		return this.getDefinition().getItem();
	}

	@Override
	public int getItemDamage()
	{
		return this.sharedStack.getDamage();
	}

	public boolean isSimilarItem( final IAEItemStack is )
	{
		return ItemTagHelper.INSTANCE.isSimilarItem( this, is );
	}

	@Override
	public boolean isSameType( final IAEItemStack otherStack )
	{
		if( otherStack == null )
		{
			return false;
		}

		return Objects.equals( this.sharedStack, ( (AEItemStack) otherStack ).sharedStack );
	}

	@Override
	public boolean isSameType( final ItemStack otherStack )
	{
		if( otherStack.isEmpty() )
		{
			return false;
		}
		int oldSize = otherStack.getCount();

		otherStack.setCount( 1 );
		boolean ret = ItemStack.areItemStacksEqual( this.getDefinition(), otherStack );
		otherStack.setCount( oldSize );

		return ret;
	}

	@Override
	public int hashCode()
	{
		return this.sharedStack.hashCode();
	}

	@Override
	public boolean equals( final Object ia )
	{
		if( ia instanceof AEItemStack )
		{
			return this.isSameType( (AEItemStack) ia );
		}
		else if( ia instanceof ItemStack )
		{
			return this.isSameType( (ItemStack) ia );
		}
		return false;
	}

	@Override
	public String toString()
	{
		return this.getStackSize() + "x" + this.getDefinition().getItem().getTranslationKey() + "@" + this.getDefinition().getDamage();
	}

	@OnlyIn( Dist.CLIENT )
	public List<String> getToolTip()
	{
		if( this.tooltip == null )
		{
			this.tooltip = Platform.getTooltip( this.asItemStackRepresentation() );
		}
		return this.tooltip;
	}

	@OnlyIn( Dist.CLIENT )
	public String getDisplayName()
	{
		if( this.displayName == null )
		{
			this.displayName = Platform.getItemDisplayName( this.asItemStackRepresentation() );
		}
		return this.displayName;
	}

	@OnlyIn( Dist.CLIENT )
	public String getModID()
	{
		if( this.uniqueID == null )
		{
			this.uniqueID = Item.REGISTRY.getNameForObject( this.getDefinition().getItem() );
		}

		if( this.uniqueID == null )
		{
			return "** Null";
		}

		return this.uniqueID.getResourceDomain() == null ? "** Null" : this.uniqueID.getResourceDomain();
	}

	public Collection<ResourceLocation> getItemTags()
	{
		return ItemTagHelper.INSTANCE.getItemTags(this);
	}

	@Override
	public boolean hasTagCompound()
	{
		return this.getDefinition().hasTag();
	}

	@Override
	public ItemStack asItemStackRepresentation()
	{
		return this.getDefinition().copy();
	}

	@Override
	public ItemStack getDefinition()
	{
		return this.sharedStack.getDefinition();
	}

	AESharedItemStack getSharedStack()
	{
		return this.sharedStack;
	}

}
