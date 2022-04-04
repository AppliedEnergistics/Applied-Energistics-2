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
import java.util.List;
import java.util.Map.Entry;

import appeng.api.config.FuzzyMode;
import appeng.util.item.AEItemStack;
import com.google.common.collect.ImmutableCollection;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.Platform;


public class CraftingTreeProcess
{
	private final CraftingTreeNode parent;
	final ICraftingPatternDetails details;
	private final CraftingJob job;
	private final Object2LongArrayMap<CraftingTreeNode> nodes = new Object2LongArrayMap<>();
	private final int depth;
	private final ICraftingGrid cc;
	private final World world;
	boolean possible = true;
	private long crafts = 0;
	private long bytes = 0;

	public CraftingTreeProcess( final ICraftingGrid cc, final CraftingJob job, final ICraftingPatternDetails details, final CraftingTreeNode craftingTreeNode, final int depth )
	{
		this.parent = craftingTreeNode;
		this.details = details;
		this.job = job;
		this.depth = depth;
		this.cc = cc;
		this.world = job.getWorld();
	}

	public void addProcess()
	{
		if( !nodes.isEmpty() )
		{
			return;
		}

		final IAEItemStack[] list = details.getInputs();

		// this is minor different then below, this slot uses the pattern, but kinda fudges it.
		for( IAEItemStack part : details.getCondensedInputs() )
		{
			if( part == null )
			{
				continue;
			}
			for( int x = 0; x < list.length; x++ )
			{
				final IAEItemStack comparePart = list[x];
				if( part.equals( comparePart ) )
				{
					boolean isPartContainer = false;
					if( part.getItem().hasContainerItem( part.getDefinition() ) )
					{
						part = list[x];
						isPartContainer = true;
					}

					long wantedSize = part.getStackSize();
					IAEItemStack found;
					long remaining = 0;
					long requestAmount = 0;

					if( wantedSize > 0 )
					{
						if( details.canSubstitute() )
						{
							for( IAEItemStack subs : details.getSubstituteInputs( x ) )
							{
								found = job.checkAvailable( subs );

								if( found != null )
								{
									remaining = found.getStackSize();
								}
								else
								{
									remaining = 0;
								}

								if( remaining > 0 )
								{
									if( remaining >= wantedSize )
									{
										requestAmount = wantedSize;
										wantedSize = 0;
										//we have the items
									}
									else
									{
										requestAmount = remaining;
										wantedSize -= remaining;
									}
									subs = subs.copy().setStackSize( requestAmount );
									this.nodes.put( new CraftingTreeNode( cc, job, subs, this, x, depth + 1 ), requestAmount );
									job.checkUse( subs );
									if( wantedSize == 0 )
									{
										break;
									}
								}
							}
						}
						else
						{
							found = job.checkAvailable( part );

							if( found != null )
							{
								remaining = found.getStackSize();
							}

							if( remaining > 0 )
							{
								if( remaining >= wantedSize )
								{
									requestAmount = wantedSize;
									wantedSize = 0;
									//we have the items
								}
								else
								{
									requestAmount = remaining;
									wantedSize -= remaining;
								}
								part = part.copy().setStackSize( requestAmount );
								this.nodes.put( new CraftingTreeNode( cc, job, part, this, x, depth + 1 ), requestAmount );
								job.checkUse( part );
							}
						}
					}
					if( wantedSize > 0 )
					{
						if( details.canSubstitute() && cc.getCraftingFor( part, details, x, world ).isEmpty() )
						{
							for( IAEItemStack subs : details.getSubstituteInputs( x ) )
							{
								if( subs.fuzzyComparison( part, FuzzyMode.IGNORE_ALL ) )
								{
									wantedSize -= 1;
									this.nodes.put( new CraftingTreeNode( cc, job, subs.copy().setStackSize( 1 ), this, x, depth + 1 ), 1 );
									if( wantedSize == 0 )
									{
										break;
									}
								}
							}
							//try to order the crafting of a substitute
							ICraftingPatternDetails prioritizedPattern = null;
							IAEItemStack prioritizedIAE = null;
							for( IAEItemStack subs : details.getSubstituteInputs( x ) )
							{
								if( subs.equals( part ) )
								{
									continue;
								}
								ImmutableCollection<ICraftingPatternDetails> detailCollection = cc.getCraftingFor( subs, details, x, world );

								for( ICraftingPatternDetails sp : detailCollection )
								{
									if( prioritizedPattern == null )
									{
										prioritizedPattern = sp;
										prioritizedIAE = subs;
									}
									else
									{
										if( sp.getPriority() > prioritizedPattern.getPriority() )
										{
											prioritizedPattern = sp;
										}
									}
								}
								if( prioritizedIAE != null )
								{
									this.nodes.put( new CraftingTreeNode( cc, job, prioritizedIAE.copy(), this, x, depth + 1 ), wantedSize );
									wantedSize = 0;
									break;
								}
							}
						}
					}
					if( wantedSize > 0 )
					{
						part = part.copy();
						// use the first slot...
						this.nodes.put( new CraftingTreeNode( cc, job, part, this, x, depth + 1 ), wantedSize );
						wantedSize = 0;
					}
					if( !isPartContainer && wantedSize == 0 )
					{
						break;
					}
				}
			}
		}
	}

	boolean notRecursive( ICraftingPatternDetails details )
	{
		return this.parent == null || this.parent.notRecursive( details );
	}

	long getTimes( final long remaining, final long stackSize )
	{
		for( final IAEItemStack part : details.getCondensedOutputs() )
		{
			for( final IAEItemStack o : details.getCondensedInputs() )
			{
				if( part.equals( o ) || o.getItem().hasContainerItem( part.getDefinition() ) )
				{
					return 1;
				}
			}
		}
		return ( remaining / stackSize ) + ( remaining % stackSize != 0 ? 1 : 0 );
	}

	void request( final MECraftingInventory inv, final long amountOfTimes, final IActionSource src ) throws CraftBranchFailure, InterruptedException
	{
		addProcess();
		this.job.handlePausing();
		List<IAEItemStack> containerItems = null;

		// request and remove inputs...
		for( final Entry<CraftingTreeNode, Long> entry : this.nodes.object2LongEntrySet() )
		{
			final IAEItemStack stack = entry.getKey().request( inv, entry.getValue() * amountOfTimes, src );

			if( stack.getItem().hasContainerItem( stack.getDefinition() ) )
			{
				final ItemStack is = Platform.getContainerItem( stack.createItemStack() );
				final IAEItemStack o = AEItemStack.fromItemStack( is );
				if( o != null )
				{
					if( containerItems == null )
					{
						containerItems = new ArrayList<>();
					}
					this.bytes++;
					o.setCachedItemStack( is );
					containerItems.add( o );
				}
			}
		}

		if( containerItems != null )
		{
			for( IAEItemStack i : containerItems )
			{
				inv.injectItems( i, Actionable.MODULATE, src );
			}
		}

		// assume its possible.

		// add crafting results..
		for( final IAEItemStack out : this.details.getCondensedOutputs() )
		{
			final IAEItemStack o = out.copy();
			o.setStackSize( o.getStackSize() * amountOfTimes );
			inv.injectItems( o, Actionable.MODULATE, src );
		}
		this.crafts += amountOfTimes;
	}

	void dive( final CraftingJob job )
	{
		job.addTask( this.getAmountCrafted( this.parent.getStack( 1 ) ), this.crafts, this.details, this.depth );
		for( final Entry<CraftingTreeNode, Long> entry : this.nodes.object2LongEntrySet() )
		{
			entry.getKey().dive( job );
		}

		job.addBytes( this.crafts * 8 + this.bytes );
	}

	IAEItemStack getAmountCrafted( IAEItemStack what2 )
	{
		for( final IAEItemStack is : this.details.getCondensedOutputs() )
		{
			if( is.isSameType( what2 ) )
			{
				what2 = what2.copy();
				what2.setStackSize( is.getStackSize() );
				return what2;
			}
		}

		// more fuzzy!
		for( final IAEItemStack is : this.details.getCondensedOutputs() )
		{
			if( is.getItem() == what2.getItem() && ( is.getItem().isDamageable() || is.getItemDamage() == what2.getItemDamage() ) )
			{
				what2 = is.copy();
				what2.setStackSize( is.getStackSize() );
				return what2;
			}
		}

		throw new IllegalStateException( "Crafting Tree construction failed." );
	}

	void setSimulate()
	{
		this.crafts = 0;
		this.bytes = 0;

		for( final Entry<CraftingTreeNode, Long> entry : this.nodes.object2LongEntrySet() )
		{
			entry.getKey().setSimulate();
		}
	}

	void setJob( final MECraftingInventory storage, final CraftingCPUCluster craftingCPUCluster, final IActionSource src ) throws CraftBranchFailure
	{
		craftingCPUCluster.addCrafting( this.details, this.crafts );

		for( final Entry<CraftingTreeNode, Long> entry : this.nodes.object2LongEntrySet() )
		{
			entry.getKey().setJob( storage, craftingCPUCluster, src );
		}
	}

	void getPlan( final IItemList<IAEItemStack> plan )
	{
		for( IAEItemStack i : this.details.getOutputs() )
		{
			i = i.copy();
			i.setCountRequestable( i.getStackSize() * this.crafts );
			plan.addRequestable( i );
		}

		for( final Entry<CraftingTreeNode, Long> entry : this.nodes.object2LongEntrySet() )
		{
			entry.getKey().getPlan( plan );
		}
	}
}
