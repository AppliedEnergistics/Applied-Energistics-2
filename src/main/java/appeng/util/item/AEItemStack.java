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


import appeng.api.config.FuzzyMode;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAETagCompound;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.io.*;
import java.security.InvalidParameterException;
import java.util.List;


public final class AEItemStack extends AEStack<IAEItemStack> implements IAEItemStack, Comparable<AEItemStack>
{

	private AEItemDef def;

	private AEItemStack( final AEItemStack is )
	{
		this.setDefinition( is.getDefinition() );
		this.setStackSize( is.getStackSize() );
		this.setCraftable( is.isCraftable() );
		this.setCountRequestable( is.getCountRequestable() );
	}

	private AEItemStack( final ItemStack is )
	{
		if( is == null )
		{
			throw new InvalidParameterException( "null is not a valid ItemStack for AEItemStack." );
		}

		final Item item = is.getItem();
		if( item == null )
		{
			throw new InvalidParameterException( "Contained item is null, thus not a valid ItemStack for AEItemStack." );
		}

		this.setDefinition( new AEItemDef( item ) );

		if( this.getDefinition().getItem() == null )
		{
			throw new InvalidParameterException( "This ItemStack is bad, it has a null item." );
		}

		/*
		 * Prevent an Item from changing the damage value on me... Either, this or a core mod.
		 */

		/*
		 * Super hackery.
		 * is.itemID = appeng.api.Materials.matQuartz.itemID; damageValue = is.getItemDamage(); is.itemID = itemID;
		 */

		/*
		 * Kinda hackery
		 */
		this.getDefinition().setDamageValue( this.getDefinition().getDamageValueHack( is ) );
		this.getDefinition().setDisplayDamage( is.getItemDamageForDisplay() );
		this.getDefinition().setMaxDamage( is.getMaxDamage() );

		final NBTTagCompound tagCompound = is.getTagCompound();
		if( tagCompound != null )
		{
			this.getDefinition().setTagCompound( (AESharedNBT) AESharedNBT.getSharedTagCompound( tagCompound, is ) );
		}

		this.setStackSize( is.stackSize );
		this.setCraftable( false );
		this.setCountRequestable( 0 );

		this.getDefinition().reHash();
		this.getDefinition().setIsOre( OreHelper.INSTANCE.isOre( is ) );
	}

	public static IAEItemStack loadItemStackFromNBT( final NBTTagCompound i )
	{
		if( i == null )
		{
			return null;
		}

		final ItemStack itemstack = ItemStack.loadItemStackFromNBT( i );
		if( itemstack == null )
		{
			return null;
		}

		final AEItemStack item = AEItemStack.create( itemstack );
		// item.priority = i.getInteger( "Priority" );
		item.setStackSize( i.getLong( "Cnt" ) );
		item.setCountRequestable( i.getLong( "Req" ) );
		item.setCraftable( i.getBoolean( "Craft" ) );
		return item;
	}

	@Nullable
	public static AEItemStack create( final ItemStack stack )
	{
		if( stack == null )
		{
			return null;
		}

		return new AEItemStack( stack );
	}

	public static IAEItemStack loadItemStackFromPacket( final ByteBuf data ) throws IOException
	{
		final byte mask = data.readByte();
		// byte PriorityType = (byte) (mask & 0x03);
		final byte stackType = (byte) ( ( mask & 0x0C ) >> 2 );
		final byte countReqType = (byte) ( ( mask & 0x30 ) >> 4 );
		final boolean isCraftable = ( mask & 0x40 ) > 0;
		final boolean hasTagCompound = ( mask & 0x80 ) > 0;

		// don't send this...
		final NBTTagCompound d = new NBTTagCompound();

		d.setShort( "id", data.readShort() );
		d.setShort( "Damage", data.readShort() );
		d.setByte( "Count", (byte) 0 );

		if( hasTagCompound )
		{
			final int len = data.readInt();

			final byte[] bd = new byte[len];
			data.readBytes( bd );

			final ByteArrayInputStream di = new ByteArrayInputStream( bd );
			d.setTag( "tag", CompressedStreamTools.read( new DataInputStream( di ) ) );
		}

		// long priority = getPacketValue( PriorityType, data );
		final long stackSize = getPacketValue( stackType, data );
		final long countRequestable = getPacketValue( countReqType, data );

		final ItemStack itemstack = ItemStack.loadItemStackFromNBT( d );
		if( itemstack == null )
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
		/*
		 * Mojang Fucked this over ; GC Optimization - Ugly Yes, but it saves a lot in the memory department.
		 */

		/*
		 * NBTBase id = i.getTag( "id" ); NBTBase Count = i.getTag( "Count" ); NBTBase Cnt = i.getTag( "Cnt" ); NBTBase
		 * Req = i.getTag( "Req" ); NBTBase Craft = i.getTag( "Craft" ); NBTBase Damage = i.getTag( "Damage" );
		 */

		/*
		 * if ( id != null && id instanceof NBTTagShort ) ((NBTTagShort) id).data = (short) this.def.item.itemID; else
		 */
		i.setShort( "id", (short) Item.itemRegistry.getIDForObject( this.getDefinition().getItem() ) );

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

		/*
		 * if ( Damage != null && Damage instanceof NBTTagShort ) ((NBTTagShort) Damage).data = (short)
		 * this.def.damageValue; else
		 */
		i.setShort( "Damage", (short) this.getDefinition().getDamageValue() );

		if( this.getDefinition().getTagCompound() != null )
		{
			i.setTag( "tag", this.getDefinition().getTagCompound() );
		}
		else
		{
			i.removeTag( "tag" );
		}
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
					final ItemStack a = this.getItemStack();
					final ItemStack b = o.getItemStack();

					try
					{
						if( mode == FuzzyMode.IGNORE_ALL )
						{
							return true;
						}
						else if( mode == FuzzyMode.PERCENT_99 )
						{
							return ( a.getItemDamageForDisplay() > 1 ) == ( b.getItemDamageForDisplay() > 1 );
						}
						else
						{
							final float percentDamageOfA = 1.0f - (float) a.getItemDamageForDisplay() / (float) a.getMaxDamage();
							final float percentDamageOfB = 1.0f - (float) b.getItemDamageForDisplay() / (float) b.getMaxDamage();

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
					final ItemStack a = this.getItemStack();

					try
					{
						if( mode == FuzzyMode.IGNORE_ALL )
						{
							return true;
						}
						else if( mode == FuzzyMode.PERCENT_99 )
						{
							return ( a.getItemDamageForDisplay() > 1 ) == ( o.getItemDamageForDisplay() > 1 );
						}
						else
						{
							final float percentDamageOfA = 1.0f - (float) a.getItemDamageForDisplay() / (float) a.getMaxDamage();
							final float percentDamageOfB = 1.0f - (float) o.getItemDamageForDisplay() / (float) o.getMaxDamage();

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
	public IAEItemStack empty()
	{
		final IAEItemStack dup = this.copy();
		dup.reset();
		return dup;
	}

	@Override
	public IAETagCompound getTagCompound()
	{
		return this.getDefinition().getTagCompound();
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
	public ItemStack getItemStack()
	{
		final ItemStack is = new ItemStack( this.getDefinition().getItem(), (int) Math.min( Integer.MAX_VALUE, this.getStackSize() ), this.getDefinition().getDamageValue() );
		if( this.getDefinition().getTagCompound() != null )
		{
			is.setTagCompound( this.getDefinition().getTagCompound().getNBTTagCompoundCopy() );
		}

		return is;
	}

	@Override
	public Item getItem()
	{
		return this.getDefinition().getItem();
	}

	@Override
	public int getItemDamage()
	{
		return this.getDefinition().getDamageValue();
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

		return this.getDefinition().equals( ( (AEItemStack) otherStack ).getDefinition() );
	}

	@Override
	public boolean isSameType( final ItemStack otherStack )
	{
		if( otherStack == null )
		{
			return false;
		}

		return this.getDefinition().isItem( otherStack );
	}

	@Override
	public int hashCode()
	{
		return this.getDefinition().getMyHash();
	}

	@Override
	public boolean equals( final Object ia )
	{
		if( ia instanceof AEItemStack )
		{
			return ( (AEItemStack) ia ).getDefinition().equals( this.getDefinition() );// && def.tagCompound ==
			// ((AEItemStack)
			// ia).def.tagCompound;
		}
		else if( ia instanceof ItemStack )
		{
			final ItemStack is = (ItemStack) ia;

			if( is.getItem() == this.getDefinition().getItem() && is.getItemDamage() == this.getDefinition().getDamageValue() )
			{
				final NBTTagCompound ta = this.getDefinition().getTagCompound();
				final NBTTagCompound tb = is.getTagCompound();
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
		return this.getItemStack().toString();
	}

	@Override
	public int compareTo( final AEItemStack b )
	{
		final int id = this.getDefinition().getItemID() - b.getDefinition().getItemID();
		if( id != 0 )
		{
			return id;
		}

		final int damageValue = this.getDefinition().getDamageValue() - b.getDefinition().getDamageValue();
		if( damageValue != 0 )
		{
			return damageValue;
		}

		final int displayDamage = this.getDefinition().getDisplayDamage() - b.getDefinition().getDisplayDamage();
		if( displayDamage != 0 )
		{
			return displayDamage;
		}

		return ( this.getDefinition().getTagCompound() == b.getDefinition().getTagCompound() ) ? 0 : this.compareNBT( b.getDefinition() );
	}

	private int compareNBT( final AEItemDef b )
	{
		final int nbt = this.compare( ( this.getDefinition().getTagCompound() == null ? 0 : this.getDefinition().getTagCompound().getHash() ), ( b.getTagCompound() == null ? 0 : b.getTagCompound().getHash() ) );
		if( nbt == 0 )
		{
			return this.compare( System.identityHashCode( this.getDefinition().getTagCompound() ), System.identityHashCode( b.getTagCompound() ) );
		}
		return nbt;
	}

	private int compare( final int l, final int m )
	{
		return l < m ? -1 : ( l > m ? 1 : 0 );
	}

	@SideOnly( Side.CLIENT )
	public List getToolTip()
	{
		if( this.getDefinition().getTooltip() != null )
		{
			return this.getDefinition().getTooltip();
		}

		return this.getDefinition().setTooltip( Platform.getTooltip( this.getItemStack() ) );
	}

	@SideOnly( Side.CLIENT )
	public String getDisplayName()
	{
		if( this.getDefinition().getDisplayName() == null )
		{
			this.getDefinition().setDisplayName( Platform.getItemDisplayName( this.getItemStack() ) );
		}

		return this.getDefinition().getDisplayName();
	}

	@SideOnly( Side.CLIENT )
	public String getModID()
	{
		if( this.getDefinition().getUniqueID() != null )
		{
			return this.getModName( this.getDefinition().getUniqueID() );
		}

		return this.getModName( this.getDefinition().setUniqueID( GameRegistry.findUniqueIdentifierFor( this.getDefinition().getItem() ) ) );
	}

	private String getModName( final UniqueIdentifier uniqueIdentifier )
	{
		if( uniqueIdentifier == null )
		{
			return "** Null";
		}

		return uniqueIdentifier.modId == null ? "** Null" : uniqueIdentifier.modId;
	}

	IAEItemStack getLow( final FuzzyMode fuzzy, final boolean ignoreMeta )
	{
		final AEItemStack bottom = new AEItemStack( this );
		final AEItemDef newDef = bottom.setDefinition( bottom.getDefinition().copy() );

		if( ignoreMeta )
		{
			newDef.setDisplayDamage( newDef.setDamageValue( 0 ) );
			newDef.reHash();
			return bottom;
		}

		if( newDef.getItem().isDamageable() )
		{
			if( fuzzy == FuzzyMode.IGNORE_ALL )
			{
				newDef.setDisplayDamage( 0 );
			}
			else if( fuzzy == FuzzyMode.PERCENT_99 )
			{
				if( this.getDefinition().getDamageValue() == 0 )
				{
					newDef.setDisplayDamage( 0 );
				}
				else
				{
					newDef.setDisplayDamage( 1 );
				}
			}
			else
			{
				final int breakpoint = fuzzy.calculateBreakPoint( this.getDefinition().getMaxDamage() );
				newDef.setDisplayDamage( breakpoint <= this.getDefinition().getDisplayDamage() ? breakpoint : 0 );
			}

			newDef.setDamageValue( newDef.getDisplayDamage() );
		}

		newDef.setTagCompound( AEItemDef.LOW_TAG );
		newDef.reHash();
		return bottom;
	}

	IAEItemStack getHigh( final FuzzyMode fuzzy, final boolean ignoreMeta )
	{
		final AEItemStack top = new AEItemStack( this );
		final AEItemDef newDef = top.setDefinition( top.getDefinition().copy() );

		if( ignoreMeta )
		{
			newDef.setDisplayDamage( newDef.setDamageValue( Integer.MAX_VALUE ) );
			newDef.reHash();
			return top;
		}

		if( newDef.getItem().isDamageable() )
		{
			if( fuzzy == FuzzyMode.IGNORE_ALL )
			{
				newDef.setDisplayDamage( this.getDefinition().getMaxDamage() + 1 );
			}
			else if( fuzzy == FuzzyMode.PERCENT_99 )
			{
				if( this.getDefinition().getDamageValue() == 0 )
				{
					newDef.setDisplayDamage( 0 );
				}
				else
				{
					newDef.setDisplayDamage( this.getDefinition().getMaxDamage() + 1 );
				}
			}
			else
			{
				final int breakpoint = fuzzy.calculateBreakPoint( this.getDefinition().getMaxDamage() );
				newDef.setDisplayDamage( this.getDefinition().getDisplayDamage() < breakpoint ? breakpoint - 1 : this.getDefinition().getMaxDamage() + 1 );
			}

			newDef.setDamageValue( newDef.getDisplayDamage() );
		}

		newDef.setTagCompound( AEItemDef.HIGH_TAG );
		newDef.reHash();
		return top;
	}

	public boolean isOre()
	{
		return this.getDefinition().getIsOre() != null;
	}

	@Override
	void writeIdentity( final ByteBuf i ) throws IOException
	{
		i.writeShort( Item.itemRegistry.getIDForObject( this.getDefinition().getItem() ) );
		i.writeShort( this.getItemDamage() );
	}

	@Override
	void readNBT( final ByteBuf i ) throws IOException
	{
		if( this.hasTagCompound() )
		{
			final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			final DataOutputStream data = new DataOutputStream( bytes );

			CompressedStreamTools.write( (NBTTagCompound) this.getTagCompound(), data );

			final byte[] tagBytes = bytes.toByteArray();
			final int size = tagBytes.length;

			i.writeInt( size );
			i.writeBytes( tagBytes );
		}
	}

	@Override
	public boolean hasTagCompound()
	{
		return this.getDefinition().getTagCompound() != null;
	}

	AEItemDef getDefinition()
	{
		return this.def;
	}

	private AEItemDef setDefinition( final AEItemDef def )
	{
		this.def = def;
		return def;
	}
}
