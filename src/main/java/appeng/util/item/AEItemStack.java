/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2020, AlgorithmX2, All rights reserved.
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

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.primitives.Ints;
import gregtech.common.items.MetaTool;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.Api;
import appeng.util.Platform;


public final class AEItemStack extends AEStack<IAEItemStack> implements IAEItemStack
{
	private static final String NBT_STACKSIZE = "Cnt";
	private static final String NBT_REQUESTABLE = "Req";
	private static final String NBT_CRAFTABLE = "Craft";

	private final AESharedItemStack sharedStack;
	private Optional<OreReference> oreReference;

	@SideOnly( Side.CLIENT )
	private String displayName;
	@SideOnly( Side.CLIENT )
	private List<String> tooltip;
	private ItemStack cachedItemStack;

	private AEItemStack( final AEItemStack is )
	{
		this.setStackSize( is.getStackSize() );
		this.setCraftable( is.isCraftable() );
		this.setCountRequestable( is.getCountRequestable() );
		this.sharedStack = is.sharedStack;
		this.oreReference = is.oreReference;
		this.cachedItemStack = is.cachedItemStack;
	}

	private AEItemStack( final AESharedItemStack is, long size )
	{
		this.sharedStack = is;
		this.setStackSize( size );
		this.setCraftable( false );
		this.setCountRequestable( 0 );
		this.oreReference = OreHelper.INSTANCE.getOre( is.getDefinition() );
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

	public static IAEItemStack fromNBT( final NBTTagCompound i )
	{
		if( i == null )
		{
			return null;
		}

		final ItemStack itemstack = new ItemStack( i );
		if( itemstack.isEmpty() )
		{
			return null;
		}

		final AEItemStack item = AEItemStack.fromItemStack( itemstack );

		item.setStackSize( i.getLong( NBT_STACKSIZE ) );
		item.setCountRequestable( i.getLong( NBT_REQUESTABLE ) );
		item.setCraftable( i.getBoolean( NBT_CRAFTABLE ) );
		return item;
	}

	@Override
	public void writeToNBT( final NBTTagCompound i )
	{
		this.getDefinition().writeToNBT( i );

		i.setLong( NBT_STACKSIZE, this.getStackSize() );
		i.setLong( NBT_REQUESTABLE, this.getCountRequestable() );
		i.setBoolean( NBT_CRAFTABLE, this.isCraftable() );
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
		final byte mask = (byte) ( ( this.getType( this.getStackSize() ) << 2 ) | ( this.getType( this.getCountRequestable() ) << 4 ) | ( (byte) ( this.isCraftable() ? 1 : 0 ) << 6 ) | ( this.hasTagCompound() ? 1 : 0 ) << 7 );

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
	public boolean fuzzyComparison( final IAEItemStack other, final FuzzyMode mode )
	{
		final ItemStack itemStack = this.getDefinition();
		final ItemStack otherStack = other.getDefinition();

		return this.fuzzyItemStackComparison( itemStack, otherStack, mode );
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
		return Api.INSTANCE.storage().getStorageChannel( IItemStorageChannel.class );
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
		return this.sharedStack.getItemDamage();
	}

	@Override
	public boolean sameOre( final IAEItemStack is )
	{
		return OreHelper.INSTANCE.sameOre( this, is );
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
			// this actually breaks the equals contract (being equals to unrelated classes)
			return equals( (ItemStack) ia );
		}
		return false;
	}

	@Override
	public boolean equals( final ItemStack is )
	{
		return this.isSameType( is );
	}

	@Override
	public ItemStack getCachedItemStack( long stackSize ) {
        if (this.cachedItemStack != null) {
            ItemStack currentCached = this.cachedItemStack;
            this.cachedItemStack = null;
            currentCached.setCount(Ints.saturatedCast(stackSize));
            return currentCached;
        }

        ItemStack itemStack = this.createItemStack();
        itemStack.setCount(Ints.saturatedCast(stackSize));

        return itemStack;
    }

	@Override
	public void setCachedItemStack( ItemStack itemStack )
	{
		this.cachedItemStack = itemStack;
	}

	@Override
	public String toString()
	{
		return this.getStackSize() + "x" + this.getDefinition().getItem().getRegistryName();
	}

	@SideOnly( Side.CLIENT )
	public List<String> getToolTip()
	{
		if( this.tooltip == null )
		{
			this.tooltip = Platform.getTooltip( this.asItemStackRepresentation() );
		}
		return this.tooltip;
	}

	@SideOnly( Side.CLIENT )
	public String getDisplayName()
	{
		if( this.displayName == null )
		{
			this.displayName = Platform.getItemDisplayName( this.asItemStackRepresentation() );
		}
		return this.displayName;
	}

	@SideOnly( Side.CLIENT )
	public String getModID()
	{
		return this.getDefinition().getItem().getRegistryName().getResourceDomain();
	}

	public Optional<OreReference> getOre()
	{
		return this.oreReference;
	}

	@Override
	public boolean hasTagCompound()
	{
		return this.getDefinition().hasTagCompound();
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

	private boolean fuzzyItemStackComparison( ItemStack a, ItemStack b, FuzzyMode mode )
	{
		if( a.getItem() == b.getItem() && ( a.getItem().isDamageable() || a.getItem() instanceof MetaTool ) )
		{
			if( mode == FuzzyMode.IGNORE_ALL )
			{
				if( a.getItem().isDamageable() )
				{
					return true;
				}
				else if( a.getItem() instanceof MetaTool )
				{
					return a.getItemDamage() == b.getItemDamage();
				}
			}
			else if( mode == FuzzyMode.PERCENT_99 )
			{
				if( a.getItem().isDamageable() )
				{
					return a.getItemDamage() > 1 == b.getItemDamage() > 1;
				}
				else if( a.getItem() instanceof MetaTool )
				{
					return ( (MetaTool) a.getItem() ).getItemDamage( a ) == ( (MetaTool) b.getItem() ).getItemDamage( b );
				}
			}
			else
			{
				float percentDamageOfA = 0;
				float percentDamageOfB = 0;
				if( a.getItem().isDamageable() )
				{
					percentDamageOfA = (float) a.getItemDamage() / a.getMaxDamage();
					percentDamageOfB = (float) b.getItemDamage() / b.getMaxDamage();
				}
				else if( a.getItem() instanceof MetaTool )
				{
					percentDamageOfA = (float) ( (MetaTool) a.getItem() ).getItemDamage( a ) / ( (MetaTool) a.getItem() ).getMaxItemDamage( a );
					percentDamageOfB = (float) ( (MetaTool) b.getItem() ).getItemDamage( b ) / ( (MetaTool) b.getItem() ).getMaxItemDamage( b );
				}

				return percentDamageOfA > mode.breakPoint == percentDamageOfB > mode.breakPoint;
			}
		}

		return false;
	}
}
