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


import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;


public class AEItemDef
{

	static final AESharedNBT LOW_TAG = new AESharedNBT( Integer.MIN_VALUE );
	static final AESharedNBT HIGH_TAG = new AESharedNBT( Integer.MAX_VALUE );

	private final int itemID;
	private final Item item;
	private int myHash;
	private int def;
	private int damageValue;
	private int displayDamage;
	private int maxDamage;
	private AESharedNBT tagCompound;
	@SideOnly( Side.CLIENT )
	private String displayName;
	@SideOnly( Side.CLIENT )
	private List tooltip;
	@SideOnly( Side.CLIENT )
	private UniqueIdentifier uniqueID;
	private OreReference isOre;

	public AEItemDef( final Item it )
	{
		this.item = it;
		this.itemID = Item.getIdFromItem( it );
	}

	AEItemDef copy()
	{
		final AEItemDef t = new AEItemDef( this.getItem() );
		t.def = this.def;
		t.setDamageValue( this.getDamageValue() );
		t.setDisplayDamage( this.getDisplayDamage() );
		t.setMaxDamage( this.getMaxDamage() );
		t.setTagCompound( this.getTagCompound() );
		t.setIsOre( this.getIsOre() );
		return t;
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null )
		{
			return false;
		}
		if( this.getClass() != obj.getClass() )
		{
			return false;
		}
		final AEItemDef other = (AEItemDef) obj;
		return other.getDamageValue() == this.getDamageValue() && other.getItem() == this.getItem() && this.getTagCompound() == other.getTagCompound();
	}

	boolean isItem( final ItemStack otherStack )
	{
		// hackery!
		final int dmg = this.getDamageValueHack( otherStack );

		if( this.getItem() == otherStack.getItem() && dmg == this.getDamageValue() )
		{
			if( ( this.getTagCompound() != null ) == otherStack.hasTagCompound() )
			{
				return true;
			}

			if( this.getTagCompound() != null && otherStack.hasTagCompound() )
			{
				return Platform.NBTEqualityTest( this.getTagCompound(), otherStack.getTagCompound() );
			}

			return true;
		}
		return false;
	}

	int getDamageValueHack( final ItemStack is )
	{
		return Items.blaze_rod.getDamage( is );
	}

	void reHash()
	{
		this.def = this.getItemID() << Platform.DEF_OFFSET | this.getDamageValue();
		this.myHash = this.def ^ ( this.getTagCompound() == null ? 0 : System.identityHashCode( this.getTagCompound() ) );
	}

	AESharedNBT getTagCompound()
	{
		return this.tagCompound;
	}

	void setTagCompound( final AESharedNBT tagCompound )
	{
		this.tagCompound = tagCompound;
	}

	int getDamageValue()
	{
		return this.damageValue;
	}

	int setDamageValue( final int damageValue )
	{
		this.damageValue = damageValue;
		return damageValue;
	}

	Item getItem()
	{
		return this.item;
	}

	int getDisplayDamage()
	{
		return this.displayDamage;
	}

	void setDisplayDamage( final int displayDamage )
	{
		this.displayDamage = displayDamage;
	}

	String getDisplayName()
	{
		return this.displayName;
	}

	void setDisplayName( final String displayName )
	{
		this.displayName = displayName;
	}

	List getTooltip()
	{
		return this.tooltip;
	}

	List setTooltip( final List tooltip )
	{
		this.tooltip = tooltip;
		return tooltip;
	}

	UniqueIdentifier getUniqueID()
	{
		return this.uniqueID;
	}

	UniqueIdentifier setUniqueID( final UniqueIdentifier uniqueID )
	{
		this.uniqueID = uniqueID;
		return uniqueID;
	}

	OreReference getIsOre()
	{
		return this.isOre;
	}

	void setIsOre( final OreReference isOre )
	{
		this.isOre = isOre;
	}

	int getItemID()
	{
		return this.itemID;
	}

	int getMaxDamage()
	{
		return this.maxDamage;
	}

	void setMaxDamage( final int maxDamage )
	{
		this.maxDamage = maxDamage;
	}

	/**
	 * TODO: Check if replaceable by hashCode();
	 */
	int getMyHash()
	{
		return this.myHash;
	}

}
