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


import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.util.Platform;


public class AEItemDef
{

	static final AESharedNBT LOW_TAG = new AESharedNBT( Integer.MIN_VALUE );
	static final AESharedNBT HIGH_TAG = new AESharedNBT( Integer.MAX_VALUE );
	public final int itemID;
	public final Item item;
	public int myHash;
	public int def;
	public int damageValue;
	public int displayDamage;
	public int maxDamage;
	public AESharedNBT tagCompound;
	@SideOnly( Side.CLIENT )
	public String displayName;
	@SideOnly( Side.CLIENT )
	public List tooltip;
	@SideOnly( Side.CLIENT )
	public UniqueIdentifier uniqueID;
	public OreReference isOre;

	public AEItemDef( final Item it )
	{
		this.item = it;
		this.itemID = Item.getIdFromItem( it );
	}

	public AEItemDef copy()
	{
		final AEItemDef t = new AEItemDef( this.item );
		t.def = this.def;
		t.damageValue = this.damageValue;
		t.displayDamage = this.displayDamage;
		t.maxDamage = this.maxDamage;
		t.tagCompound = this.tagCompound;
		t.isOre = this.isOre;
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
		return other.damageValue == this.damageValue && other.item == this.item && this.tagCompound == other.tagCompound;
	}

	public boolean isItem( final ItemStack otherStack )
	{
		// hackery!
		final int dmg = this.getDamageValueHack( otherStack );

		if( this.item == otherStack.getItem() && dmg == this.damageValue )
		{
			if( ( this.tagCompound != null ) == otherStack.hasTagCompound() )
			{
				return true;
			}

			if( this.tagCompound != null && otherStack.hasTagCompound() )
			{
				return Platform.NBTEqualityTest( this.tagCompound, otherStack.getTagCompound() );
			}

			return true;
		}
		return false;
	}

	public int getDamageValueHack( final ItemStack is )
	{
		return Items.blaze_rod.getDamage( is );
	}

	public void reHash()
	{
		this.def = this.itemID << Platform.DEF_OFFSET | this.damageValue;
		this.myHash = this.def ^ ( this.tagCompound == null ? 0 : System.identityHashCode( this.tagCompound ) );
	}
}
