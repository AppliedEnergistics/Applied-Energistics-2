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


import appeng.api.AEApi;
import appeng.api.features.IItemComparison;
import appeng.api.storage.data.IAETagCompound;
import appeng.util.Platform;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;


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
	private SharedSearchObject sso;
	private int hash;
	private IItemComparison comp;

	private AESharedNBT( final Item itemID, final int damageValue )
	{
		this.item = itemID;
		this.meta = damageValue;
	}

	public AESharedNBT( final int fakeValue )
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
	static synchronized NBTTagCompound getSharedTagCompound( final NBTTagCompound tagCompound, final ItemStack s )
	{
		if( tagCompound.hasNoTags() )
		{
			return null;
		}

		final Item item = s.getItem();
		int meta = -1;
		if( s.getItem() != null && s.isItemStackDamageable() && s.getHasSubtypes() )
		{
			meta = s.getItemDamage();
		}

		if( isShared( tagCompound ) )
		{
			return tagCompound;
		}

		final SharedSearchObject sso = new SharedSearchObject( item, meta, tagCompound );

		final WeakReference<SharedSearchObject> c = SHARED_TAG_COMPOUND.get( sso );
		if( c != null )
		{
			final SharedSearchObject cg = c.get();
			if( cg != null )
			{
				return cg.getShared(); // I don't think I really need to check this
			}
			// as its already certain to exist..
		}

		final AESharedNBT clone = AESharedNBT.createFromCompound( item, meta, tagCompound );
		sso.setCompound( (NBTTagCompound) sso.getCompound().copy() ); // prevent
		// modification
		// of data based
		// on original
		// item.
		sso.setShared( clone );
		clone.sso = sso;

		SHARED_TAG_COMPOUND.put( sso, new WeakReference<SharedSearchObject>( sso ) );
		return clone;
	}

	/*
	 * returns true if the compound is part of the shared compound system ( and can thus be compared directly ).
	 */
	public static boolean isShared( final NBTTagCompound ta )
	{
		return ta instanceof AESharedNBT;
	}

	private static AESharedNBT createFromCompound( final Item itemID, final int damageValue, final NBTTagCompound c )
	{
		final AESharedNBT x = new AESharedNBT( itemID, damageValue );

		// c.getTags()
		for( final Object o : c.func_150296_c() )
		{
			final String name = (String) o;
			x.setTag( name, c.getTag( name ).copy() );
		}

		x.hash = Platform.NBTOrderlessHash( c );

		final ItemStack isc = new ItemStack( itemID, 1, damageValue );
		isc.setTagCompound( c );
		x.comp = AEApi.instance().registries().specialComparison().getSpecialComparison( isc );

		return x;
	}

	int getHash()
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
	public boolean equals( final Object par1Obj )
	{
		if( par1Obj instanceof AESharedNBT )
		{
			return this == par1Obj;
		}
		return super.equals( par1Obj );
	}

	public boolean matches( final Item item, final int meta, final int orderlessHash )
	{
		return item == this.item && this.meta == meta && this.hash == orderlessHash;
	}

	public boolean comparePreciseWithRegistry( final AESharedNBT tagCompound )
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

	public boolean compareFuzzyWithRegistry( final AESharedNBT tagCompound )
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
