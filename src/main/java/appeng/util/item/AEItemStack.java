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


import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.Platform;


public final class AEItemStack extends AEStack<IAEItemStack> implements IAEItemStack
{
	private AESharedItemStack sharedStack;
	private Optional<OreReference> oreReference;

	@SideOnly( Side.CLIENT )
	private String displayName;
	@SideOnly( Side.CLIENT )
	private List<String> tooltip;
	@SideOnly( Side.CLIENT )
	private ResourceLocation uniqueID;

	private AEItemStack( final AEItemStack is )
	{
		this.setStackSize( is.getStackSize() );
		this.setCraftable( is.isCraftable() );
		this.setCountRequestable( is.getCountRequestable() );
		this.sharedStack = is.sharedStack;
		this.oreReference = is.oreReference;
	}

	private AEItemStack( final AESharedItemStack is, long size )
	{
		this.sharedStack = is;
		this.setStackSize( size );
		this.setCraftable( false );
		this.setCountRequestable( 0 );
		this.oreReference = OreHelper.INSTANCE.getOre( is.getItemStack() );
	}

	public static IAEItemStack loadItemStackFromNBT( final NBTTagCompound i )
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

		final AEItemStack item = AEItemStack.create( itemstack );
		item.setStackSize( i.getLong( "Cnt" ) );
		item.setCountRequestable( i.getLong( "Req" ) );
		item.setCraftable( i.getBoolean( "Craft" ) );
		return item;
	}

	@Nullable
	public static AEItemStack create( final ItemStack stack )
	{
		if( stack.isEmpty() )
		{
			return null;
		}

		return new AEItemStack( AEItemStackRegistry.getRegisteredStack( stack ), stack.getCount() );
	}

	public static IAEItemStack loadItemStackFromPacket( final ByteBuf data ) throws IOException
	{
		final byte mask = data.readByte();
		// byte PriorityType = (byte) (mask & 0x03);
		final byte stackType = (byte) ( ( mask & 0x0C ) >> 2 );
		final byte countReqType = (byte) ( ( mask & 0x30 ) >> 4 );
		final boolean isCraftable = ( mask & 0x40 ) > 0;

		final ItemStack itemstack = ByteBufUtils.readItemStack( data );
		final long stackSize = getPacketValue( stackType, data );
		final long countRequestable = getPacketValue( countReqType, data );

		if( itemstack.isEmpty() )
		{
			return null;
		}

		final AEItemStack item = AEItemStack.create( itemstack );
		// item.priority = (int) priority;
		item.setStackSize( stackSize );
		item.setCountRequestable( countRequestable );
		item.setCraftable( isCraftable );
		return item;
	}

	@Override
	public void add( final IAEItemStack option )
	{
		if( option == null )
		{
			return;
		}

		// if ( priority < ((AEItemStack) option).priority )
		// priority = ((AEItemStack) option).priority;

		this.incStackSize( option.getStackSize() );
		this.setCountRequestable( this.getCountRequestable() + option.getCountRequestable() );
		this.setCraftable( this.isCraftable() || option.isCraftable() );
	}

	@Override
	public void writeToNBT( final NBTTagCompound i )
	{
		this.getDefinition().writeToNBT( i );
		i.setLong( "Cnt", this.getStackSize() );
		i.setLong( "Req", this.getCountRequestable() );
		i.setBoolean( "Craft", this.isCraftable() );
	}

	@Override
	public boolean fuzzyComparison( final Object st, final FuzzyMode mode )
	{
		if( st instanceof IAEItemStack )
		{
			final IAEItemStack o = (IAEItemStack) st;

			if( this.sameOre( o ) )
			{
				return true;
			}

			if( o.getItem() == this.getItem() )
			{
				if( this.getDefinition().getItem().isDamageable() )
				{
					final ItemStack a = this.getDefinition();
					final ItemStack b = o.getDefinition();

					try
					{
						if( mode == FuzzyMode.IGNORE_ALL )
						{
							return true;
						}
						else if( mode == FuzzyMode.PERCENT_99 )
						{
							final Item ai = a.getItem();
							final Item bi = b.getItem();

							return ( ai.getDurabilityForDisplay( a ) < 0.001f ) == ( bi.getDurabilityForDisplay( b ) < 0.001f );
						}
						else
						{
							final Item ai = a.getItem();
							final Item bi = b.getItem();

							final float percentDamageOfA = 1.0f - (float) ai.getDurabilityForDisplay( a );
							final float percentDamageOfB = 1.0f - (float) bi.getDurabilityForDisplay( b );

							return ( percentDamageOfA > mode.breakPoint ) == ( percentDamageOfB > mode.breakPoint );
						}
					}
					catch( final Throwable e )
					{
						if( mode == FuzzyMode.IGNORE_ALL )
						{
							return true;
						}
						else if( mode == FuzzyMode.PERCENT_99 )
						{
							return ( a.getItemDamage() > 1 ) == ( b.getItemDamage() > 1 );
						}
						else
						{
							final float percentDamageOfA = (float) a.getItemDamage() / (float) a.getMaxDamage();
							final float percentDamageOfB = (float) b.getItemDamage() / (float) b.getMaxDamage();

							return ( percentDamageOfA > mode.breakPoint ) == ( percentDamageOfB > mode.breakPoint );
						}
					}
				}

				return this.getItemDamage() == o.getItemDamage();
			}
		}

		if( st instanceof ItemStack )
		{
			final ItemStack o = (ItemStack) st;

			OreHelper.INSTANCE.sameOre( this, o );

			if( o.getItem() == this.getItem() )
			{
				if( this.getDefinition().getItem().isDamageable() )
				{
					final ItemStack a = this.getDefinition();

					try
					{
						if( mode == FuzzyMode.IGNORE_ALL )
						{
							return true;
						}
						else if( mode == FuzzyMode.PERCENT_99 )
						{
							final Item ai = a.getItem();
							final Item bi = o.getItem();

							return ( ai.getDurabilityForDisplay( a ) < 0.001f ) == ( bi.getDurabilityForDisplay( o ) < 0.001f );
						}
						else
						{
							final Item ai = a.getItem();
							final Item bi = o.getItem();

							final float percentDamageOfA = 1.0f - (float) ai.getDurabilityForDisplay( a );
							final float percentDamageOfB = 1.0f - (float) bi.getDurabilityForDisplay( o );

							return ( percentDamageOfA > mode.breakPoint ) == ( percentDamageOfB > mode.breakPoint );
						}
					}
					catch( final Throwable e )
					{
						if( mode == FuzzyMode.IGNORE_ALL )
						{
							return true;
						}
						else if( mode == FuzzyMode.PERCENT_99 )
						{
							return ( a.getItemDamage() > 1 ) == ( o.getItemDamage() > 1 );
						}
						else
						{
							final float percentDamageOfA = (float) a.getItemDamage() / (float) a.getMaxDamage();
							final float percentDamageOfB = (float) o.getItemDamage() / (float) o.getMaxDamage();

							return ( percentDamageOfA > mode.breakPoint ) == ( percentDamageOfB > mode.breakPoint );
						}
					}
				}

				return this.getItemDamage() == o.getItemDamage();
			}
		}

		return false;
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
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
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
		return this.getDefinition().getItemDamage();
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

		return this.sharedStack == ( (AEItemStack) otherStack ).sharedStack;
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
		return this.getDefinition().toString();
	}

	@SideOnly( Side.CLIENT )
	public List<String> getToolTip()
	{
		if( this.tooltip == null )
		{
			this.tooltip = Platform.getTooltip( this.getDefinition() );
		}
		return this.tooltip;
	}

	@SideOnly( Side.CLIENT )
	public String getDisplayName()
	{
		if( this.displayName == null )
		{
			this.displayName = Platform.getItemDisplayName( this.getDefinition() );
		}
		return this.displayName;
	}

	@SideOnly( Side.CLIENT )
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

	public Optional<OreReference> getOre()
	{
		return this.oreReference;
	}

	@Override
	protected void writeToStream( final ByteBuf data ) throws IOException
	{
		ByteBufUtils.writeItemStack( data, this.getDefinition() );
	}

	@Override
	public boolean hasTagCompound()
	{
		return this.getDefinition().hasTagCompound();
	}

	@Override
	public ItemStack getDisplayStack()
	{
		return getDefinition();
	}

	@Override
	public ItemStack getDefinition()
	{
		return this.sharedStack.getItemStack();
	}

	AESharedItemStack getSharedStack()
	{
		return this.sharedStack;
	}

}
