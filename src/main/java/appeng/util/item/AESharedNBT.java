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


import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.AEApi;
import appeng.api.features.IItemComparison;
import appeng.api.storage.data.IAETagCompound;
import appeng.util.Platform;


/*
 * this is used for the shared NBT Cache.
 */
public class AESharedNBT extends NBTTagCompound implements IAETagCompound
{

	/*
	 * Shared Tag Compound Cache.
	 */
	private static final WeakHashMap<SharedSearchObject, WeakReference<SharedSearchObject>> SHARED_TAG_COMPOUND = new WeakHashMap<SharedSearchObject, WeakReference<SharedSearchObject>>();
	private final Item item;
	private final int meta;
	public SharedSearchObject sso;
	private int hash;
	private IItemComparison comp;

	private AESharedNBT( Item itemID, int damageValue )
	{
		this.item = itemID;
		this.meta = damageValue;
	}

	public AESharedNBT( int fakeValue )
	{
		this.item = null;
		this.meta = 0;
		this.hash = fakeValue;
	}

	/*
	 * Debug purposes.
	 */
	public static int sharedTagLoad()
	{
		return SHARED_TAG_COMPOUND.size();
	}

	/*
	 * Returns an NBT Compound that is used for accelerating comparisons.
	 */
	public static synchronized NBTTagCompound getSharedTagCompound( NBTTagCompound tagCompound, ItemStack s )
	{
		if( tagCompound.hasNoTags() )
		{
			return null;
		}

		Item item = s.getItem();
		int meta = -1;
		if( s.getItem() != null && s.isItemStackDamageable() && s.getHasSubtypes() )
		{
			meta = s.getItemDamage();
		}

		if( isShared( tagCompound ) )
		{
			return tagCompound;
		}

		SharedSearchObject sso = new SharedSearchObject( item, meta, tagCompound );

		WeakReference<SharedSearchObject> c = SHARED_TAG_COMPOUND.get( sso );
		if( c != null )
		{
			SharedSearchObject cg = c.get();
			if( cg != null )
			{
				return cg.shared; // I don't think I really need to check this
			}
			// as its already certain to exist..
		}

		AESharedNBT clone = AESharedNBT.createFromCompound( item, meta, tagCompound );
		sso.compound = (NBTTagCompound) sso.compound.copy(); // prevent
		// modification
		// of data based
		// on original
		// item.
		sso.shared = clone;
		clone.sso = sso;

		SHARED_TAG_COMPOUND.put( sso, new WeakReference<SharedSearchObject>( sso ) );
		return clone;
	}

	/*
	 * returns true if the compound is part of the shared compound system ( and can thus be compared directly ).
	 */
	public static boolean isShared( NBTTagCompound ta )
	{
		return ta instanceof AESharedNBT;
	}

	public static AESharedNBT createFromCompound( Item itemID, int damageValue, NBTTagCompound c )
	{
		AESharedNBT x = new AESharedNBT( itemID, damageValue );

		// c.getTags()
		for( Object o : c.getKeySet() )
		{
			String name = (String) o;
			x.setTag( name, c.getTag( name ).copy() );
		}

		x.hash = Platform.NBTOrderlessHash( c );

		ItemStack isc = new ItemStack( itemID, 1, damageValue );
		isc.setTagCompound( c );
		x.comp = AEApi.instance().registries().specialComparison().getSpecialComparison( isc );

		return x;
	}

	public int getHash()
	{
		return this.hash;
	}

	@Override
	public NBTTagCompound getNBTTagCompoundCopy()
	{
		return (NBTTagCompound) this.copy();
	}

	@Override
	public IItemComparison getSpecialComparison()
	{
		return this.comp;
	}

	@Override
	public boolean equals( Object par1Obj )
	{
		if( par1Obj instanceof AESharedNBT )
		{
			return this == par1Obj;
		}
		return super.equals( par1Obj );
	}

	public boolean matches( Item item, int meta, int orderlessHash )
	{
		return item == this.item && this.meta == meta && this.hash == orderlessHash;
	}

	public boolean comparePreciseWithRegistry( AESharedNBT tagCompound )
	{
		if( this == tagCompound )
		{
			return true;
		}

		if( this.comp != null && tagCompound.comp != null )
		{
			return this.comp.sameAsPrecise( tagCompound.comp );
		}

		return false;
	}

	public boolean compareFuzzyWithRegistry( AESharedNBT tagCompound )
	{
		if( this == tagCompound )
		{
			return true;
		}
		if( tagCompound == null )
		{
			return false;
		}

		if( this.comp == tagCompound.comp )
		{
			return true;
		}

		if( this.comp != null )
		{
			return this.comp.sameAsFuzzy( tagCompound.comp );
		}

		return false;
	}
}
