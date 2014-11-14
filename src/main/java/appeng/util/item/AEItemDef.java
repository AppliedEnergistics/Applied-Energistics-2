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
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEItemDef
{

	public int myHash;

	public int def;

	private final int itemID;
	public final Item item;
	public int damageValue;

	public int displayDamage;
	public int maxDamage;

	public AESharedNBT tagCompound;

	@SideOnly(Side.CLIENT)
	public String displayName;

	@SideOnly(Side.CLIENT)
	public List tooltip;

	@SideOnly(Side.CLIENT)
	public UniqueIdentifier uniqueID;

	public OreReference isOre;

	static final AESharedNBT lowTag = new AESharedNBT( Integer.MIN_VALUE );
	static final AESharedNBT highTag = new AESharedNBT( Integer.MAX_VALUE );

	public AEItemDef(Item it) {
		item = it;
		itemID = System.identityHashCode( item );
	}

	public AEItemDef copy()
	{
		AEItemDef t = new AEItemDef( item );
		t.def = def;
		t.damageValue = damageValue;
		t.displayDamage = displayDamage;
		t.maxDamage = maxDamage;
		t.tagCompound = tagCompound;
		t.isOre = isOre;
		return t;
	}

	@Override
	public boolean equals(Object obj)
	{
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		AEItemDef other = (AEItemDef) obj;
		return other.damageValue == damageValue && other.item == item && tagCompound == other.tagCompound;
	}

	public int getDamageValueHack(ItemStack is)
	{
		return Items.blaze_rod.getDamage( is );
	}

	public boolean isItem(ItemStack otherStack)
	{
		// hackery!
		int dmg = getDamageValueHack( otherStack );

		if ( item == otherStack.getItem() && dmg == damageValue )
		{
			if ( (tagCompound != null) == otherStack.hasTagCompound() )
				return true;

			if ( tagCompound != null && otherStack.hasTagCompound() )
				return Platform.NBTEqualityTest( tagCompound, otherStack.getTagCompound() );

			return true;
		}
		return false;
	}

	public void reHash()
	{
		def = itemID << Platform.DEF_OFFSET | damageValue;
		myHash = def ^ (tagCompound == null ? 0 : System.identityHashCode( tagCompound ));
	}
}
