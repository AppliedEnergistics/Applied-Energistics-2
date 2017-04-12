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

package appeng.helpers;


import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.ContainerNull;
import appeng.util.ItemSorters;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import java.util.*;


public class PatternHelper implements ICraftingPatternDetails, Comparable<PatternHelper>
{

	private final ItemStack patternItem;
	private final InventoryCrafting crafting = new InventoryCrafting( new ContainerNull(), 3, 3 );
	private final InventoryCrafting testFrame = new InventoryCrafting( new ContainerNull(), 3, 3 );
	private final ItemStack correctOutput;
	private final IRecipe standardRecipe;
	private final IAEItemStack[] condensedInputs;
	private final IAEItemStack[] condensedOutputs;
	private final IAEItemStack[] inputs;
	private final IAEItemStack[] outputs;
	private final boolean isCrafting;
	private final boolean canSubstitute;
	private final Set<TestLookup> failCache = new HashSet<TestLookup>();
	private final Set<TestLookup> passCache = new HashSet<TestLookup>();
	private final IAEItemStack pattern;
	private int priority = 0;

	public PatternHelper( final ItemStack is, final World w )
	{
		final NBTTagCompound encodedValue = is.getTagCompound();

		if( encodedValue == null )
		{
			throw new IllegalArgumentException( "No pattern here!" );
		}

		final NBTTagList inTag = encodedValue.getTagList( "in", 10 );
		final NBTTagList outTag = encodedValue.getTagList( "out", 10 );
		this.isCrafting = encodedValue.getBoolean( "crafting" );

		this.canSubstitute = encodedValue.getBoolean( "substitute" );
		this.patternItem = is;
		this.pattern = AEItemStack.create( is );

		final List<IAEItemStack> in = new ArrayList<IAEItemStack>();
		final List<IAEItemStack> out = new ArrayList<IAEItemStack>();

		for( int x = 0; x < inTag.tagCount(); x++ )
		{
			final ItemStack gs = ItemStack.loadItemStackFromNBT( inTag.getCompoundTagAt( x ) );

			this.crafting.setInventorySlotContents( x, gs );

			if( gs != null && ( !this.isCrafting || !gs.hasTagCompound() ) )
			{
				this.markItemAs( x, gs, TestStatus.ACCEPT );
			}

			in.add( AEApi.instance().storage().createItemStack( gs ) );
			this.testFrame.setInventorySlotContents( x, gs );
		}

		if( this.isCrafting )
		{
			this.standardRecipe = Platform.findMatchingRecipe( this.crafting, w );

			if( this.standardRecipe != null )
			{
				this.correctOutput = this.standardRecipe.getCraftingResult( this.crafting );
				out.add( AEApi.instance().storage().createItemStack( this.correctOutput ) );
			}
			else
			{
				throw new IllegalStateException( "No pattern here!" );
			}
		}
		else
		{
			this.standardRecipe = null;
			this.correctOutput = null;

			for( int x = 0; x < outTag.tagCount(); x++ )
			{
				final ItemStack gs = ItemStack.loadItemStackFromNBT( outTag.getCompoundTagAt( x ) );

				if( gs != null )
				{
					out.add( AEApi.instance().storage().createItemStack( gs ) );
				}
			}
		}

		this.outputs = out.toArray( new IAEItemStack[out.size()] );
		this.inputs = in.toArray( new IAEItemStack[in.size()] );

		final Map<IAEItemStack, IAEItemStack> tmpOutputs = new HashMap<IAEItemStack, IAEItemStack>();

		for( final IAEItemStack io : this.outputs )
		{
			if( io == null )
			{
				continue;
			}

			final IAEItemStack g = tmpOutputs.get( io );

			if( g == null )
			{
				tmpOutputs.put( io, io.copy() );
			}
			else
			{
				g.add( io );
			}
		}

		final Map<IAEItemStack, IAEItemStack> tmpInputs = new HashMap<IAEItemStack, IAEItemStack>();

		for( final IAEItemStack io : this.inputs )
		{
			if( io == null )
			{
				continue;
			}

			final IAEItemStack g = tmpInputs.get( io );

			if( g == null )
			{
				tmpInputs.put( io, io.copy() );
			}
			else
			{
				g.add( io );
			}
		}

		if( tmpOutputs.isEmpty() || tmpInputs.isEmpty() )
		{
			throw new IllegalStateException( "No pattern here!" );
		}

		this.condensedInputs = new IAEItemStack[tmpInputs.size()];
		int offset = 0;

		for( final IAEItemStack io : tmpInputs.values() )
		{
			this.condensedInputs[offset] = io;
			offset++;
		}

		offset = 0;
		this.condensedOutputs = new IAEItemStack[tmpOutputs.size()];

		for( final IAEItemStack io : tmpOutputs.values() )
		{
			this.condensedOutputs[offset] = io;
			offset++;
		}
	}

	private void markItemAs( final int slotIndex, final ItemStack i, final TestStatus b )
	{
		if( b == TestStatus.TEST || i.hasTagCompound() )
		{
			return;
		}

		( b == TestStatus.ACCEPT ? this.passCache : this.failCache ).add( new TestLookup( slotIndex, i ) );
	}

	@Override
	public ItemStack getPattern()
	{
		return this.patternItem;
	}

	@Override
	public synchronized boolean isValidItemForSlot( final int slotIndex, final ItemStack i, final World w )
	{
		if( !this.isCrafting )
		{
			throw new IllegalStateException( "Only crafting recipes supported." );
		}

		final TestStatus result = this.getStatus( slotIndex, i );

		switch( result )
		{
			case ACCEPT:
				return true;
			case DECLINE:
				return false;
			case TEST:
			default:
				break;
		}

		for( int x = 0; x < this.crafting.getSizeInventory(); x++ )
		{
			this.testFrame.setInventorySlotContents( x, this.crafting.getStackInSlot( x ) );
		}

		this.testFrame.setInventorySlotContents( slotIndex, i );

		if( this.standardRecipe.matches( this.testFrame, w ) )
		{
			final ItemStack testOutput = this.standardRecipe.getCraftingResult( this.testFrame );

			if( Platform.isSameItemPrecise( this.correctOutput, testOutput ) )
			{
				this.testFrame.setInventorySlotContents( slotIndex, this.crafting.getStackInSlot( slotIndex ) );
				this.markItemAs( slotIndex, i, TestStatus.ACCEPT );
				return true;
			}
		}
		else
		{
			final ItemStack testOutput = CraftingManager.getInstance().findMatchingRecipe( this.testFrame, w );

			if( Platform.isSameItemPrecise( this.correctOutput, testOutput ) )
			{
				this.testFrame.setInventorySlotContents( slotIndex, this.crafting.getStackInSlot( slotIndex ) );
				this.markItemAs( slotIndex, i, TestStatus.ACCEPT );
				return true;
			}
		}

		this.markItemAs( slotIndex, i, TestStatus.DECLINE );
		return false;
	}

	@Override
	public boolean isCraftable()
	{
		return this.isCrafting;
	}

	@Override
	public IAEItemStack[] getInputs()
	{
		return this.inputs;
	}

	@Override
	public IAEItemStack[] getCondensedInputs()
	{
		return this.condensedInputs;
	}

	@Override
	public IAEItemStack[] getCondensedOutputs()
	{
		return this.condensedOutputs;
	}

	@Override
	public IAEItemStack[] getOutputs()
	{
		return this.outputs;
	}

	@Override
	public boolean canSubstitute()
	{
		return this.canSubstitute;
	}

	@Override
	public ItemStack getOutput( final InventoryCrafting craftingInv, final World w )
	{
		if( !this.isCrafting )
		{
			throw new IllegalStateException( "Only crafting recipes supported." );
		}

		for( int x = 0; x < craftingInv.getSizeInventory(); x++ )
		{
			if( !this.isValidItemForSlot( x, craftingInv.getStackInSlot( x ), w ) )
			{
				return null;
			}
		}

		if( this.outputs != null && this.outputs.length > 0 )
		{
			return this.outputs[0].getItemStack();
		}

		return null;
	}

	private TestStatus getStatus( final int slotIndex, final ItemStack i )
	{
		if( this.crafting.getStackInSlot( slotIndex ) == null )
		{
			return i == null ? TestStatus.ACCEPT : TestStatus.DECLINE;
		}

		if( i == null )
		{
			return TestStatus.DECLINE;
		}

		if( i.hasTagCompound() )
		{
			return TestStatus.TEST;
		}

		if( this.passCache.contains( new TestLookup( slotIndex, i ) ) )
		{
			return TestStatus.ACCEPT;
		}

		if( this.failCache.contains( new TestLookup( slotIndex, i ) ) )
		{
			return TestStatus.DECLINE;
		}

		return TestStatus.TEST;
	}

	@Override
	public int getPriority()
	{
		return this.priority;
	}

	@Override
	public void setPriority( final int priority )
	{
		this.priority = priority;
	}

	@Override
	public int compareTo( final PatternHelper o )
	{
		return ItemSorters.compareInt( o.priority, this.priority );
	}

	@Override
	public int hashCode()
	{
		return this.pattern.hashCode();
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

		final PatternHelper other = (PatternHelper) obj;

		if( this.pattern != null && other.pattern != null )
		{
			return this.pattern.equals( other.pattern );
		}
		return false;
	}

	private enum TestStatus
	{
		ACCEPT, DECLINE, TEST
	}


	private static final class TestLookup
	{

		private final int slot;
		private final int ref;
		private final int hash;

		public TestLookup( final int slot, final ItemStack i )
		{
			this( slot, i.getItem(), i.getItemDamage() );
		}

		public TestLookup( final int slot, final Item item, final int dmg )
		{
			this.slot = slot;
			this.ref = ( dmg << Platform.DEF_OFFSET ) | ( Item.getIdFromItem( item ) & 0xffff );
			final int offset = 3 * slot;
			this.hash = ( this.ref << offset ) | ( this.ref >> ( offset + 32 ) );
		}

		@Override
		public int hashCode()
		{
			return this.hash;
		}

		@Override
		public boolean equals( final Object obj )
		{
			final boolean equality;

			if( obj instanceof TestLookup )
			{
				final TestLookup b = (TestLookup) obj;

				equality = b.slot == this.slot && b.ref == this.ref;
			}
			else
			{
				equality = false;
			}

			return equality;
		}
	}
}
