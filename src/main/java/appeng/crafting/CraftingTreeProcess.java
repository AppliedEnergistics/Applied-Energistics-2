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
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import appeng.api.config.FuzzyMode;
import appeng.util.item.AEItemStack;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.channels.IItemStorageChannel;
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
	boolean possible = true;
	private long crafts = 0;
	private IItemList<IAEItemStack> containerItems;
	private boolean limitQty;
	private List<IAEItemStack> containerItemsList;
	private long bytes = 0;

	public CraftingTreeProcess( final ICraftingGrid cc, final CraftingJob job, final ICraftingPatternDetails details, final CraftingTreeNode craftingTreeNode, final int depth )
	{
		this.parent = craftingTreeNode;
		this.details = details;
		this.job = job;
		this.depth = depth;
		final World world = job.getWorld();

		final IAEItemStack[] list = details.getInputs();

		for( final IAEItemStack part : details.getCondensedInputs() )
		{
			if( part.getItem().hasContainerItem( part.getDefinition() ) )
			{
				if( containerItems == null )
				{
					containerItems = AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createList();
					containerItemsList = new ArrayList<>();
				}
				containerItems.add( part );
				this.limitQty = true;
				//break;
			}
		}
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
					if( containerItems != null && !containerItems.findFuzzy( list[x], FuzzyMode.IGNORE_ALL ).isEmpty() )
					{
						part = list[x];
						isPartContainer = true;
					}
					long wantedSize = part.getStackSize();

					if( !isPartContainer )
					{
						IAEItemStack found = job.checkAvailable( part );
						IAEItemStack used;
						long requestAmount;

						long remaining = 0;
						if( found != null )
						{
							used = job.getUsedWhileBuilding().findPrecise( part );
							remaining = found.getStackSize();
							if( used != null )
							{
								if( used.getStackSize() >= found.getStackSize() )
								{
									remaining = 0;
								}
								else
								{
									remaining -= used.getStackSize();
								}
							}
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
							job.getUsedWhileBuilding().addStorage( part );
							this.nodes.put( new CraftingTreeNode( cc, job, part, this, x, depth + 1 ), requestAmount );
							if( wantedSize == 0 )
							{
								break;
							}
						}
						if( wantedSize > 0 )
						{
							if( details.canSubstitute() && cc.getCraftingFor( part, details, x, world ).isEmpty() )
							{
								//try to extract substitutes
								for( IAEItemStack subs : details.getSubstituteInputs( x ) )
								{
									if( subs.equals( part ) )
									{
										continue;
									}
									found = job.checkAvailable( subs );
									remaining = 0;

									if( found != null )
									{
										used = job.getUsedWhileBuilding().findPrecise( subs );
										remaining = found.getStackSize();
										if( used != null )
										{
											if( used.getStackSize() >= found.getStackSize() )
											{
												remaining = 0;
											}
											else
											{
												remaining -= used.getStackSize();
											}
										}
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
										job.getUsedWhileBuilding().addStorage( subs );
										this.nodes.put( new CraftingTreeNode( cc, job, subs, this, x, depth + 1 ), requestAmount );
									}
									if( wantedSize == 0 )
									{
										break;
									}
								}
								if( wantedSize > 0 )
								{
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
													prioritizedIAE = subs;
												}
											}
											this.nodes.put( new CraftingTreeNode( cc, job, prioritizedIAE.copy(), this, x, depth + 1 ), wantedSize );
											wantedSize = 0;
											break;
										}
									}
									if( wantedSize == 0 )
									{
										break;
									}
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

		for( final IAEItemStack part : details.getCondensedOutputs() )
		{
			for( final IAEItemStack o : details.getCondensedInputs() )
			{
				if( part.equals( o ) )
				{
					this.limitQty = true;
					break;
				}
			}
		}
	}

	IItemList<IAEItemStack> getContainerItems()
	{
		return this.containerItems;
	}

	boolean notRecursive()
	{
		return this.parent == null || this.parent.notRecursive();
	}

	long getTimes( final long remaining, final long stackSize )
	{
		if( this.limitQty )
		{
			return 1;
		}
		return ( remaining / stackSize ) + ( remaining % stackSize != 0 ? 1 : 0 );
	}

	void request( final MECraftingInventory inv, final long amountOfTimes, final IActionSource src ) throws CraftBranchFailure, InterruptedException
	{
		this.job.handlePausing();

		// request and remove inputs...
		for( final Entry<CraftingTreeNode, Long> entry : this.nodes.object2LongEntrySet() )
		{
			final IAEItemStack craftableStack = entry.getKey().getStack( entry.getValue() );
			final IAEItemStack stack = entry.getKey().request( inv, craftableStack.getStackSize() * amountOfTimes, src );

			if( containerItems != null && !this.containerItems.findFuzzy( stack, FuzzyMode.IGNORE_ALL ).isEmpty() )
			{
				final ItemStack is = Platform.getContainerItem( stack.createItemStack() );
				final IAEItemStack o = AEItemStack.fromItemStack( is );
				if( o != null )
				{
					this.bytes++;
					containerItemsList.add( o );
				}
			}
		}

		if( containerItems != null) {
			for (IAEItemStack i : containerItemsList) {
				inv.injectItems(i, Actionable.MODULATE, src);
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
