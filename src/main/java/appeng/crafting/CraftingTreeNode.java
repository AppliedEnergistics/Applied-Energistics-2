/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.crafting;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.me.cluster.implementations.CraftingCPUCluster;


public final class CraftingTreeNode
{

	// what slot!
	final int slot;
	final CraftingJob job;
	final IItemList<IAEItemStack> used = AEApi.instance().storage().createItemList();
	// parent node.
	private final CraftingTreeProcess parent;
	private final World world;
	// what item is this?
	private final IAEItemStack what;
	// what are the crafting patterns for this?
	private final ArrayList<CraftingTreeProcess> nodes = new ArrayList<CraftingTreeProcess>();
	int bytes = 0;
	boolean canEmit = false;
	boolean cannotUse = false;
	long missing = 0;
	long howManyEmitted = 0;
	boolean exhausted = false;

	boolean sim;

	public CraftingTreeNode( ICraftingGrid cc, CraftingJob job, IAEItemStack wat, CraftingTreeProcess par, int slot, int depth )
	{
		this.what = wat;
		this.parent = par;
		this.slot = slot;
		this.world = job.getWorld();
		this.job = job;
		this.sim = false;

		this.canEmit = cc.canEmitFor( this.what );
		if( this.canEmit )
		{
			return; // if you can emit for something, you can't make it with patterns.
		}

		for( ICraftingPatternDetails details : cc.getCraftingFor( this.what, this.parent == null ? null : this.parent.details, slot, this.world ) )// in
		// order.
		{
			if( this.parent == null || this.parent.notRecursive( details ) )
			{
				this.nodes.add( new CraftingTreeProcess( cc, job, details, this, depth + 1 ) );
			}
		}
	}

	final boolean notRecursive( ICraftingPatternDetails details )
	{
		IAEItemStack[] o = details.getCondensedOutputs();
		for( IAEItemStack i : o )
		{
			if( i.equals( this.what ) )
			{
				return false;
			}
		}

		o = details.getCondensedInputs();
		for( IAEItemStack i : o )
		{
			if( i.equals( this.what ) )
			{
				return false;
			}
		}

		if( this.parent == null )
		{
			return true;
		}

		return this.parent.notRecursive( details );
	}

	public final IAEItemStack request( MECraftingInventory inv, long l, BaseActionSource src ) throws CraftBranchFailure, InterruptedException
	{
		this.job.handlePausing();

		List<IAEItemStack> thingsUsed = new LinkedList<IAEItemStack>();

		this.what.setStackSize( l );
		if( this.slot >= 0 && this.parent != null && this.parent.details.isCraftable() )
		{
			for( IAEItemStack fuzz : inv.getItemList().findFuzzy( this.what, FuzzyMode.IGNORE_ALL ) )
			{
				if( this.parent.details.isValidItemForSlot( this.slot, fuzz.getItemStack(), this.world ) )
				{
					fuzz = fuzz.copy();
					fuzz.setStackSize( l );
					IAEItemStack available = inv.extractItems( fuzz, Actionable.MODULATE, src );

					if( available != null )
					{
						if( !this.exhausted )
						{
							IAEItemStack is = this.job.checkUse( available );
							if( is != null )
							{
								thingsUsed.add( is.copy() );
								this.used.add( is );
							}
						}

						this.bytes += available.getStackSize();
						l -= available.getStackSize();

						if( l == 0 )
						{
							return available;
						}
					}
				}
			}
		}
		else
		{
			IAEItemStack available = inv.extractItems( this.what, Actionable.MODULATE, src );

			if( available != null )
			{
				if( !this.exhausted )
				{
					IAEItemStack is = this.job.checkUse( available );
					if( is != null )
					{
						thingsUsed.add( is.copy() );
						this.used.add( is );
					}
				}

				this.bytes += available.getStackSize();
				l -= available.getStackSize();

				if( l == 0 )
				{
					return available;
				}
			}
		}

		if( this.canEmit )
		{
			IAEItemStack wat = this.what.copy();
			wat.setStackSize( l );

			this.howManyEmitted = wat.getStackSize();
			this.bytes += wat.getStackSize();

			return wat;
		}

		this.exhausted = true;

		if( this.nodes.size() == 1 )
		{
			CraftingTreeProcess pro = this.nodes.get( 0 );

			while( pro.possible && l > 0 )
			{
				IAEItemStack madeWhat = pro.getAmountCrafted( this.what );

				pro.request( inv, pro.getTimes( l, madeWhat.getStackSize() ), src );

				madeWhat.setStackSize( l );
				IAEItemStack available = inv.extractItems( madeWhat, Actionable.MODULATE, src );

				if( available != null )
				{
					this.bytes += available.getStackSize();
					l -= available.getStackSize();

					if( l <= 0 )
					{
						return available;
					}
				}
				else
				{
					pro.possible = false; // ;P
				}
			}
		}
		else if( this.nodes.size() > 1 )
		{
			for( CraftingTreeProcess pro : this.nodes )
			{
				try
				{
					while( pro.possible && l > 0 )
					{
						MECraftingInventory subInv = new MECraftingInventory( inv, true, true, true );
						pro.request( subInv, 1, src );

						this.what.setStackSize( l );
						IAEItemStack available = subInv.extractItems( this.what, Actionable.MODULATE, src );

						if( available != null )
						{
							if( !subInv.commit( src ) )
							{
								throw new CraftBranchFailure( this.what, l );
							}

							this.bytes += available.getStackSize();
							l -= available.getStackSize();

							if( l <= 0 )
							{
								return available;
							}
						}
						else
						{
							pro.possible = false; // ;P
						}
					}
				}
				catch( CraftBranchFailure fail )
				{
					pro.possible = true;
				}
			}
		}

		if( this.sim )
		{
			this.missing += l;
			this.bytes += l;
			IAEItemStack rv = this.what.copy();
			rv.setStackSize( l );
			return rv;
		}

		for( IAEItemStack o : thingsUsed )
		{
			this.job.refund( o.copy() );
			o.setStackSize( -o.getStackSize() );
			this.used.add( o );
		}

		throw new CraftBranchFailure( this.what, l );
	}

	public final void dive( CraftingJob job )
	{
		if( this.missing > 0 )
		{
			job.addMissing( this.getStack( this.missing ) );
		}
		// missing = 0;

		job.addBytes( 8 + this.bytes );

		for( CraftingTreeProcess pro : this.nodes )
		{
			pro.dive( job );
		}
	}

	public final IAEItemStack getStack( long size )
	{
		IAEItemStack is = this.what.copy();
		is.setStackSize( size );
		return is;
	}

	public final void setSimulate()
	{
		this.sim = true;
		this.missing = 0;
		this.bytes = 0;
		this.used.resetStatus();
		this.exhausted = false;

		for( CraftingTreeProcess pro : this.nodes )
		{
			pro.setSimulate();
		}
	}

	public final void setJob( MECraftingInventory storage, CraftingCPUCluster craftingCPUCluster, BaseActionSource src ) throws CraftBranchFailure
	{
		for( IAEItemStack i : this.used )
		{
			IAEItemStack ex = storage.extractItems( i, Actionable.MODULATE, src );

			if( ex == null || ex.getStackSize() != i.getStackSize() )
			{
				throw new CraftBranchFailure( i, i.getStackSize() );
			}

			craftingCPUCluster.addStorage( ex );
		}

		if( this.howManyEmitted > 0 )
		{
			IAEItemStack i = this.what.copy();
			i.setStackSize( this.howManyEmitted );
			craftingCPUCluster.addEmitable( i );
		}

		for( CraftingTreeProcess pro : this.nodes )
		{
			pro.setJob( storage, craftingCPUCluster, src );
		}
	}

	public final void getPlan( IItemList<IAEItemStack> plan )
	{
		if( this.missing > 0 )
		{
			IAEItemStack o = this.what.copy();
			o.setStackSize( this.missing );
			plan.add( o );
		}

		if( this.howManyEmitted > 0 )
		{
			IAEItemStack i = this.what.copy();
			i.setCountRequestable( this.howManyEmitted );
			plan.addRequestable( i );
		}

		for( IAEItemStack i : this.used )
		{
			plan.add( i.copy() );
		}

		for( CraftingTreeProcess pro : this.nodes )
		{
			pro.getPlan( plan );
		}
	}
}
