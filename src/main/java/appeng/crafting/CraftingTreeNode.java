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

package appeng.crafting;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.me.cluster.implementations.CraftingCPUCluster;


public class CraftingTreeNode
{

	// what slot!
	private final int slot;
	private final CraftingJob job;
	private final IItemList<IAEItemStack> used = AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createList();
	// parent node.
	private final CraftingTreeProcess parent;
	private final World world;
	// what item is this?
	private final IAEItemStack what;
	// what are the crafting patterns for this?
	private final ArrayList<CraftingTreeProcess> nodes = new ArrayList<>();
	private int bytes = 0;
	private boolean canEmit = false;
	private long missing = 0;
	private long howManyEmitted = 0;
	private boolean exhausted = false;

	private boolean sim;

	public CraftingTreeNode( final ICraftingGrid cc, final CraftingJob job, final IAEItemStack wat, final CraftingTreeProcess par, final int slot, final int depth )
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

		for( final ICraftingPatternDetails details : cc.getCraftingFor( this.what, this.parent == null ? null : this.parent.details, slot, this.world ) )// in
		// order.
		{
			if( this.parent == null || this.parent.notRecursive( details ) )
			{
				this.nodes.add( new CraftingTreeProcess( cc, job, details, this, depth + 1 ) );
			}
		}
	}

	boolean notRecursive( final ICraftingPatternDetails details )
	{
		IAEItemStack[] o = details.getCondensedOutputs();

		for( final IAEItemStack i : o )
		{
			if( i.equals( this.what ) )
			{
				return false;
			}
		}

		o = details.getCondensedInputs();

		for( final IAEItemStack i : o )
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

	IAEItemStack request( final MECraftingInventory inv, long l, final IActionSource src ) throws CraftBranchFailure, InterruptedException
	{
		this.job.handlePausing();
		final List<IAEItemStack> thingsUsed = new ArrayList<>();

		this.what.setStackSize( l );
		if( this.getSlot() >= 0 && this.parent != null && this.parent.details.isCraftable() )
		{
			final Collection<IAEItemStack> itemList;
			final IItemList<IAEItemStack> inventoryList = inv.getItemList();

			if( this.parent.details.canSubstitute() )
			{
				final List<IAEItemStack> substitutes = this.parent.details.getSubstituteInputs(this.slot);
				itemList = new ArrayList<>(substitutes.size());

				for (IAEItemStack stack : substitutes) {
					itemList.addAll(inventoryList.findFuzzy(stack, FuzzyMode.IGNORE_ALL));
				}
			}
			else
			{
				itemList = Lists.newArrayList();

				final IAEItemStack item = inventoryList.findPrecise( this.what );

				if( item != null )
				{
					itemList.add( item );
				}
			}

			for( IAEItemStack fuzz : itemList )
			{
				if( this.parent.details.isValidItemForSlot( this.getSlot(), fuzz.copy().setStackSize( 1 ).createItemStack(), this.world ) )
				{
					fuzz = fuzz.copy();
					fuzz.setStackSize( l );

					final IAEItemStack available = inv.extractItems( fuzz, Actionable.MODULATE, src );

					if( available != null )
					{
						if( !this.exhausted )
						{
							final IAEItemStack is = this.job.checkUse( available );

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
			final IAEItemStack available = inv.extractItems( this.what, Actionable.MODULATE, src );

			if( available != null )
			{
				if( !this.exhausted )
				{
					final IAEItemStack is = this.job.checkUse( available );

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
			final IAEItemStack wat = this.what.copy();
			wat.setStackSize( l );

			this.howManyEmitted = wat.getStackSize();
			this.bytes += wat.getStackSize();

			return wat;
		}

		this.exhausted = true;

		if( this.nodes.size() == 1 )
		{
			final CraftingTreeProcess pro = this.nodes.get( 0 );

			while( pro.possible && l > 0 )
			{
				final IAEItemStack madeWhat = pro.getAmountCrafted( this.what );

				pro.request( inv, pro.getTimes( l, madeWhat.getStackSize() ), src );

				madeWhat.setStackSize( l );

				final IAEItemStack available = inv.extractItems( madeWhat, Actionable.MODULATE, src );

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
			for( final CraftingTreeProcess pro : this.nodes )
			{
				try
				{
					while( pro.possible && l > 0 )
					{
						final MECraftingInventory subInv = new MECraftingInventory( inv, true, true, true );
						pro.request( subInv, 1, src );

						this.what.setStackSize( l );
						final IAEItemStack available = subInv.extractItems( this.what, Actionable.MODULATE, src );

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
				catch( final CraftBranchFailure fail )
				{
					pro.possible = true;
				}
			}
		}

		if( this.sim )
		{
			this.missing += l;
			this.bytes += l;
			final IAEItemStack rv = this.what.copy();
			rv.setStackSize( l );
			return rv;
		}

		for( final IAEItemStack o : thingsUsed )
		{
			this.job.refund( o.copy() );
			o.setStackSize( -o.getStackSize() );
			this.used.add( o );
		}

		throw new CraftBranchFailure( this.what, l );
	}

	void dive( final CraftingJob job )
	{
		if( this.missing > 0 )
		{
			job.addMissing( this.getStack( this.missing ) );
		}
		// missing = 0;

		job.addBytes( 8 + this.bytes );

		for( final CraftingTreeProcess pro : this.nodes )
		{
			pro.dive( job );
		}
	}

	IAEItemStack getStack( final long size )
	{
		final IAEItemStack is = this.what.copy();
		is.setStackSize( size );
		return is;
	}

	void setSimulate()
	{
		this.sim = true;
		this.missing = 0;
		this.bytes = 0;
		this.used.resetStatus();
		this.exhausted = false;

		for( final CraftingTreeProcess pro : this.nodes )
		{
			pro.setSimulate();
		}
	}

	public void setJob( final MECraftingInventory storage, final CraftingCPUCluster craftingCPUCluster, final IActionSource src ) throws CraftBranchFailure
	{
		for( final IAEItemStack i : this.used )
		{
			final IAEItemStack ex = storage.extractItems( i, Actionable.MODULATE, src );

			if( ex == null || ex.getStackSize() != i.getStackSize() )
			{
				throw new CraftBranchFailure( i, i.getStackSize() );
			}

			craftingCPUCluster.addStorage( ex );
		}

		if( this.howManyEmitted > 0 )
		{
			final IAEItemStack i = this.what.copy().reset();
			i.setStackSize( this.howManyEmitted );
			craftingCPUCluster.addEmitable( i );
		}

		for( final CraftingTreeProcess pro : this.nodes )
		{
			pro.setJob( storage, craftingCPUCluster, src );
		}
	}

	void getPlan( final IItemList<IAEItemStack> plan )
	{
		if( this.missing > 0 )
		{
			final IAEItemStack o = this.what.copy();
			o.setStackSize( this.missing );
			plan.add( o );
		}

		if( this.howManyEmitted > 0 )
		{
			final IAEItemStack i = this.what.copy();
			i.setCountRequestable( this.howManyEmitted );
			plan.addRequestable( i );
		}

		for( final IAEItemStack i : this.used )
		{
			plan.add( i.copy() );
		}

		for( final CraftingTreeProcess pro : this.nodes )
		{
			pro.getPlan( plan );
		}
	}

	int getSlot()
	{
		return this.slot;
	}
}
