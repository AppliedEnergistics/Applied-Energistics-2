package appeng.util.item;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAETagCompound;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class AEItemStack extends AEStack<IAEItemStack> implements IAEItemStack, Comparable<AEItemStack>
{

	AEItemDef def;

	@Override
	public String toString()
	{
		return getItemStack().toString();
	}

	@Override
	public void add(IAEItemStack option)
	{
		if ( option == null )
			return;

		// if ( priority < ((AEItemStack) option).priority )
		// priority = ((AEItemStack) option).priority;

		incStackSize( option.getStackSize() );
		setCountRequestable( getCountRequestable() + option.getCountRequestable() );
		setCraftable( isCraftable() || option.isCraftable() );
	}

	private AEItemStack(AEItemStack is) {
		def = is.def;
		stackSize = is.stackSize;
		setCraftable( is.isCraftable() );
		setCountRequestable( is.getCountRequestable() );
	}

	protected AEItemStack(ItemStack is) {
		if ( is == null )
			throw new RuntimeException( "Invalid Itemstack." );

		def = new AEItemDef( is.getItem() );

		if ( def.item == null )
			throw new RuntimeException( "This ItemStack is bad, it has a null item." );

		/*
		 * Prevent an Item from changing the damage value on me... Either, this or a core mod.
		 */

		/*
		 * Super hackery.
		 * 
		 * is.itemID = appeng.api.Materials.matQuartz.itemID; damageValue = is.getItemDamage(); is.itemID = itemID;
		 */

		/*
		 * Kinda hackery
		 */
		def.damageValue = def.getDamageValueHack( is );
		def.displayDamage = is.getItemDamageForDisplay();
		def.maxDamage = is.getMaxDamage();

		NBTTagCompound tagCompound = is.getTagCompound();
		if ( tagCompound != null )
			def.tagCompound = (AESharedNBT) AESharedNBT.getSharedTagCompound( tagCompound, is );

		stackSize = is.stackSize;
		setCraftable( false );
		setCountRequestable( 0 );

		def.reHash();
		def.isOre = OreHelper.instance.isOre( is );
	}

	public static AEItemStack create(Object a)
	{
		if ( a instanceof ItemStack )
			return new AEItemStack( (ItemStack) a );
		if ( a instanceof IAEItemStack )
			((IAEItemStack) a).copy();
		return null;
	}

	@Override
	public Item getItem()
	{
		return def.item;
	}

	@Override
	public boolean equals(Object ia)
	{
		if ( ia instanceof AEItemStack )
		{
			return ((AEItemStack) ia).def.equals( def );// && def.tagCompound == ((AEItemStack) ia).def.tagCompound;
		}
		else if ( ia instanceof ItemStack )
		{
			ItemStack is = (ItemStack) ia;

			if ( is.getItem() == def.item && is.getItemDamage() == this.def.damageValue )
			{
				NBTTagCompound ta = def.tagCompound;
				NBTTagCompound tb = is.getTagCompound();
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
	public ItemStack getItemStack()
	{
		ItemStack is = new ItemStack( def.item, (int) Math.min( Integer.MAX_VALUE, stackSize ), def.damageValue );
		if ( def.tagCompound != null )
			is.setTagCompound( def.tagCompound.getNBTTagCompoundCopy() );

		return is;
	}

	@Override
	public IAEItemStack copy()
	{
		return new AEItemStack( this );
	}

	@Override
	public void writeToNBT(NBTTagCompound i)
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
		i.setShort( "id", (short) Item.itemRegistry.getIDForObject( this.def.item ) );

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
		 */
		i.setBoolean( "Craft", this.isCraftable() );

		/*
		 * if ( Damage != null && Damage instanceof NBTTagShort ) ((NBTTagShort) Damage).data = (short)
		 * this.def.damageValue; else
		 */
		i.setShort( "Damage", (short) this.def.damageValue );

		if ( def.tagCompound != null )
			i.setTag( "tag", def.tagCompound );
		else
			i.removeTag( "tag" );

	}

	public static IAEItemStack loadItemStackFromNBT(NBTTagCompound i)
	{
		if ( i == null )
			return null;

		ItemStack itemstack = ItemStack.loadItemStackFromNBT( i );
		if ( itemstack == null )
			return null;

		AEItemStack item = AEItemStack.create( itemstack );
		// item.priority = i.getInteger( "Priority" );
		item.stackSize = i.getLong( "Cnt" );
		item.setCountRequestable( i.getLong( "Req" ) );
		item.setCraftable( i.getBoolean( "Craft" ) );
		return item;
	}

	@Override
	public boolean hasTagCompound()
	{
		return def.tagCompound != null;
	}

	@Override
	public int hashCode()
	{
		return def.myHash;
	}

	@Override
	public int compareTo(AEItemStack b)
	{
		int id = compare( def.item.hashCode(), b.def.item.hashCode() );
		int damageValue = compare( def.damageValue, b.def.damageValue );
		int displayDamage = compare( def.displayDamage, b.def.displayDamage );
		// AELog.info( "NBT: " + nbt );
		return id == 0 ? (damageValue == 0 ? (displayDamage == 0 ? compareNBT( b.def ) : displayDamage) : damageValue) : id;
	}

	private int compareNBT(AEItemDef b)
	{
		int nbt = compare( (def.tagCompound == null ? 0 : def.tagCompound.getHash()), (b.tagCompound == null ? 0 : b.tagCompound.getHash()) );
		if ( nbt == 0 )
			return compare( System.identityHashCode( def.tagCompound ), System.identityHashCode( b.tagCompound ) );
		return nbt;
	}

	private int compare(int l, long m)
	{
		return l < m ? -1 : (l > m ? 1 : 0);
	}

	@SideOnly(Side.CLIENT)
	public List getToolTip()
	{
		if ( def.tooltip != null )
			return def.tooltip;

		return def.tooltip = Platform.getTooltip( getItemStack() );
	}

	@SideOnly(Side.CLIENT)
	public String getDisplayName()
	{
		if ( def.displayName != null )
			return def.displayName;

		return def.displayName = Platform.getItemDisplayName( getItemStack() );
	}

	@SideOnly(Side.CLIENT)
	public String getModID()
	{
		if ( def.uniqueID != null )
			return getModName( def.uniqueID );

		return getModName( def.uniqueID = GameRegistry.findUniqueIdentifierFor( def.item ) );
	}

	private String getModName(UniqueIdentifier uniqueIdentifier)
	{
		if ( uniqueIdentifier == null )
			return "** Null";

		return uniqueIdentifier.modId == null ? "** Null" : uniqueIdentifier.modId;
	}

	@Override
	public IAEItemStack empty()
	{
		IAEItemStack dup = copy();
		dup.reset();
		return dup;
	}

	@Override
	public int getItemDamage()
	{
		return def.damageValue;
	}

	@Override
	void writeIdentity(ByteBuf i) throws IOException
	{
		i.writeShort( Item.itemRegistry.getIDForObject( def.item ) );
		i.writeShort( getItemDamage() );
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

	public static IAEItemStack loadItemStackFromPacket(ByteBuf data) throws IOException
	{
		byte mask = data.readByte();
		// byte PriorityType = (byte) (mask & 0x03);
		byte StackType = (byte) ((mask & 0x0C) >> 2);
		byte CountReqType = (byte) ((mask & 0x30) >> 4);
		boolean isCraftable = (mask & 0x40) > 0;
		boolean hasTagCompound = (mask & 0x80) > 0;

		// don't send this...
		NBTTagCompound d = new NBTTagCompound();

		d.setShort( "id", data.readShort() );
		d.setShort( "Damage", data.readShort() );
		d.setByte( "Count", (byte) 0 );

		if ( hasTagCompound )
		{
			int len = data.readInt();

			byte[] bd = new byte[len];
			data.readBytes( bd );

			ByteArrayInputStream di = new ByteArrayInputStream( bd );
			d.setTag( "tag", CompressedStreamTools.read( new DataInputStream( di ) ) );
		}

		// long priority = getPacketValue( PriorityType, data );
		long stackSize = getPacketValue( StackType, data );
		long countRequestable = getPacketValue( CountReqType, data );

		ItemStack itemstack = ItemStack.loadItemStackFromNBT( d );
		if ( itemstack == null )
			return null;

		AEItemStack item = AEItemStack.create( itemstack );
		// item.priority = (int) priority;
		item.stackSize = stackSize;
		item.setCountRequestable( countRequestable );
		item.setCraftable( isCraftable );
		return item;
	}

	@Override
	public boolean sameOre(IAEItemStack is)
	{
		return OreHelper.instance.sameOre( this, is );
	}

	@Override
	public boolean fuzzyComparison(Object st, FuzzyMode Mode)
	{
		if ( st instanceof IAEItemStack )
		{
			IAEItemStack o = (IAEItemStack) st;

			if ( sameOre( o ) )
				return true;

			if ( o.getItem() == getItem() )
			{
				if ( def.item.isDamageable() )
				{
					ItemStack a = getItemStack();
					ItemStack b = o.getItemStack();

					try
					{
						if ( Mode == FuzzyMode.IGNORE_ALL )
						{
							return true;
						}
						else if ( Mode == FuzzyMode.PERCENT_99 )
						{
							return (a.getItemDamageForDisplay() > 1) == (b.getItemDamageForDisplay() > 1);
						}
						else
						{
							float APercentDamaged = 1.0f - (float) a.getItemDamageForDisplay() / (float) a.getMaxDamage();
							float BPercentDamaged = 1.0f - (float) b.getItemDamageForDisplay() / (float) b.getMaxDamage();

							return (APercentDamaged > Mode.breakPoint) == (BPercentDamaged > Mode.breakPoint);
						}
					}
					catch (Throwable e)
					{
						if ( Mode == FuzzyMode.IGNORE_ALL )
						{
							return true;
						}
						else if ( Mode == FuzzyMode.PERCENT_99 )
						{
							return (a.getItemDamage() > 1) == (b.getItemDamage() > 1);
						}
						else
						{
							float APercentDamaged = (float) a.getItemDamage() / (float) a.getMaxDamage();
							float BPercentDamaged = (float) b.getItemDamage() / (float) b.getMaxDamage();

							return (APercentDamaged > Mode.breakPoint) == (BPercentDamaged > Mode.breakPoint);
						}
					}
				}

				return this.getItemDamage() == o.getItemDamage();
			}
		}

		if ( st instanceof ItemStack )
		{
			ItemStack o = (ItemStack) st;

			OreHelper.instance.sameOre( this, o );

			if ( o.getItem() == getItem() )
			{
				if ( def.item.isDamageable() )
				{
					ItemStack a = getItemStack();

					try
					{
						if ( Mode == FuzzyMode.IGNORE_ALL )
						{
							return true;
						}
						else if ( Mode == FuzzyMode.PERCENT_99 )
						{
							return (a.getItemDamageForDisplay() > 1) == (o.getItemDamageForDisplay() > 1);
						}
						else
						{
							float APercentDamaged = 1.0f - (float) a.getItemDamageForDisplay() / (float) a.getMaxDamage();
							float BPercentDamaged = 1.0f - (float) o.getItemDamageForDisplay() / (float) o.getMaxDamage();

							return (APercentDamaged > Mode.breakPoint) == (BPercentDamaged > Mode.breakPoint);
						}
					}
					catch (Throwable e)
					{
						if ( Mode == FuzzyMode.IGNORE_ALL )
						{
							return true;
						}
						else if ( Mode == FuzzyMode.PERCENT_99 )
						{
							return (a.getItemDamage() > 1) == (o.getItemDamage() > 1);
						}
						else
						{
							float APercentDamaged = (float) a.getItemDamage() / (float) a.getMaxDamage();
							float BPercentDamaged = (float) o.getItemDamage() / (float) o.getMaxDamage();

							return (APercentDamaged > Mode.breakPoint) == (BPercentDamaged > Mode.breakPoint);
						}
					}
				}

				return this.getItemDamage() == o.getItemDamage();
			}
		}

		return false;
	}

	public IAEItemStack getLow(FuzzyMode fuzzy, boolean ignoreMeta)
	{
		AEItemStack bottom = new AEItemStack( this );
		AEItemDef newDef = bottom.def = bottom.def.copy();

		if ( ignoreMeta )
		{
			newDef.displayDamage = newDef.damageValue = 0;
			newDef.reHash();
			return bottom;
		}

		if ( newDef.item.isDamageable() )
		{
			if ( fuzzy == FuzzyMode.IGNORE_ALL )
			{
				newDef.displayDamage = 0;
			}
			else if ( fuzzy == FuzzyMode.PERCENT_99 )
			{
				if ( def.damageValue == 0 )
					newDef.displayDamage = 0;
				else
					newDef.displayDamage = 1;

			}
			else
			{
				int breakpoint = fuzzy.calculateBreakPoint( def.maxDamage );
				newDef.displayDamage = breakpoint <= def.displayDamage ? breakpoint : 0;
			}

			newDef.damageValue = newDef.displayDamage;
		}

		newDef.tagCompound = AEItemDef.lowTag;
		newDef.reHash();
		return bottom;
	}

	public IAEItemStack getHigh(FuzzyMode fuzzy, boolean ignoreMeta)
	{
		AEItemStack top = new AEItemStack( this );
		AEItemDef newDef = top.def = top.def.copy();

		if ( ignoreMeta )
		{
			newDef.displayDamage = newDef.damageValue = Integer.MAX_VALUE;
			newDef.reHash();
			return top;
		}

		if ( newDef.item.isDamageable() )
		{
			if ( fuzzy == FuzzyMode.IGNORE_ALL )
			{
				newDef.displayDamage = def.maxDamage + 1;
			}
			else if ( fuzzy == FuzzyMode.PERCENT_99 )
			{
				if ( def.damageValue == 0 )
					newDef.displayDamage = 0;
				else
					newDef.displayDamage = def.maxDamage + 1;
			}
			else
			{
				int breakpoint = fuzzy.calculateBreakPoint( def.maxDamage );
				newDef.displayDamage = def.displayDamage < breakpoint ? breakpoint - 1 : def.maxDamage + 1;
			}

			newDef.damageValue = newDef.displayDamage;
		}

		newDef.tagCompound = AEItemDef.highTag;
		newDef.reHash();
		return top;
	}

	@Override
	public IAETagCompound getTagCompound()
	{
		return def.tagCompound;
	}

	@Override
	public boolean isSameType(IAEItemStack otherStack)
	{
		if ( otherStack == null )
			return false;

		return def.equals( ((AEItemStack) otherStack).def );
	}

	@Override
	public boolean isSameType(ItemStack otherStack)
	{
		if ( otherStack == null )
			return false;

		return def.isItem( otherStack );
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

	public boolean isOre()
	{
		return def.isOre != null;
	}

}
