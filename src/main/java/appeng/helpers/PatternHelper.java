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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.ContainerNull;
import appeng.util.ItemSorters;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class PatternHelper implements ICraftingPatternDetails, Comparable<PatternHelper>
{

	final ItemStack patternItem;
	private IAEItemStack pattern;

	final InventoryCrafting crafting = new InventoryCrafting( new ContainerNull(), 3, 3 );
	final InventoryCrafting testFrame = new InventoryCrafting( new ContainerNull(), 3, 3 );

	final ItemStack correctOutput;
	final IRecipe standardRecipe;

	final IAEItemStack condensedInputs[];
	final IAEItemStack condensedOutputs[];
	final IAEItemStack inputs[];
	final IAEItemStack outputs[];

	final boolean isCrafting;
	public int priority = 0;

	static class TestLookup
	{

		final int slot;
		final int ref;
		final int hash;

		public TestLookup(int slot, ItemStack i)
		{
			this( slot, i.getItem(), i.getItemDamage() );
		}

		public TestLookup(int slot, Item item, int dmg)
		{
			this.slot = slot;
			ref = (dmg << Platform.DEF_OFFSET) | (Item.getIdFromItem( item ) & 0xffff);
			int offset = 3 * slot;
			hash = (ref << offset) | (ref >> (offset + 32));
		}

		@Override
		public int hashCode()
		{
			return hash;
		}

		@Override
		public boolean equals(Object obj)
		{
			final boolean equality;

			if ( obj instanceof TestLookup )
			{
				TestLookup b = (TestLookup) obj;
				equality = b.slot == slot && b.ref == ref;
			}
			else
			{
				equality = false;
			}

			return equality;
		}

	}

	enum TestStatus
	{
		ACCEPT, DECLINE, TEST
	}

	final HashSet<TestLookup> failCache = new HashSet<TestLookup>();
	final HashSet<TestLookup> passCache = new HashSet<TestLookup>();

	private void markItemAs(int slotIndex, ItemStack i, TestStatus b)
	{
		if ( b == TestStatus.TEST || i.hasTagCompound() )
			return;

		(b == TestStatus.ACCEPT ? passCache : failCache).add( new TestLookup( slotIndex, i ) );
	}

	private TestStatus getStatus(int slotIndex, ItemStack i)
	{
		if ( crafting.getStackInSlot( slotIndex ) == null )
			return i == null ? TestStatus.ACCEPT : TestStatus.DECLINE;

		if ( i == null )
			return TestStatus.DECLINE;

		if ( i.hasTagCompound() )
			return TestStatus.TEST;

		if ( passCache.contains( new TestLookup( slotIndex, i ) ) )
			return TestStatus.ACCEPT;

		if ( failCache.contains( new TestLookup( slotIndex, i ) ) )
			return TestStatus.DECLINE;

		return TestStatus.TEST;
	}

	private static ItemStack loadStackFromNBT(NBTTagCompound tag) throws InvalidPatternException
	{
		try
		{
			return ItemStack.loadItemStackFromNBT( tag );
		}
		catch (Throwable ex)
		{
			throw new InvalidPatternException( tag );
		}
	}

	public PatternHelper(ItemStack is, World w) throws InvalidPatternException
	{
		NBTTagCompound encodedValue = is.getTagCompound();

		if ( encodedValue == null )
			throw new InvalidPatternException( InvalidPatternException.REASON_EMPTY );

		NBTTagList inTag = encodedValue.getTagList( "in", 10 );
		NBTTagList outTag = encodedValue.getTagList( "out", 10 );
		isCrafting = encodedValue.getBoolean( "crafting" );
		patternItem = is;
		pattern = AEItemStack.create( is );

		List<IAEItemStack> in = new ArrayList<IAEItemStack>();
		List<IAEItemStack> out = new ArrayList<IAEItemStack>();

		for (int x = 0; x < inTag.tagCount(); x++)
		{
			ItemStack gs = loadStackFromNBT( inTag.getCompoundTagAt(x) );
			crafting.setInventorySlotContents( x, gs );

			if ( gs != null && (!isCrafting || !gs.hasTagCompound()) )
			{
				markItemAs( x, gs, TestStatus.ACCEPT );
			}

			in.add( AEApi.instance().storage().createItemStack( gs ) );
			testFrame.setInventorySlotContents( x, gs );
		}

		if ( isCrafting )
		{
			standardRecipe = Platform.findMatchingRecipe( crafting, w );
			if ( standardRecipe != null )
			{
				correctOutput = standardRecipe.getCraftingResult( crafting );
				out.add( AEApi.instance().storage().createItemStack( correctOutput ) );
			}
			else
				throw new InvalidPatternException( InvalidPatternException.REASON_UNCRAFTABLE );
		}
		else
		{
			standardRecipe = null;
			correctOutput = null;

			for (int x = 0; x < outTag.tagCount(); x++)
			{
				ItemStack gs = loadStackFromNBT( outTag.getCompoundTagAt(x) );
				if ( gs != null )
					out.add( AEApi.instance().storage().createItemStack( gs ) );
			}
		}

		outputs = out.toArray( new IAEItemStack[out.size()] );
		inputs = in.toArray( new IAEItemStack[in.size()] );

		HashMap<IAEItemStack, IAEItemStack> tmpOutputs = new HashMap<IAEItemStack, IAEItemStack>();
		for (IAEItemStack io : outputs)
		{
			if ( io == null )
				continue;

			IAEItemStack g = tmpOutputs.get( io );
			if ( g == null )
				tmpOutputs.put( io, io.copy() );
			else
				g.add( io );
		}

		HashMap<IAEItemStack, IAEItemStack> tmpInputs = new HashMap<IAEItemStack, IAEItemStack>();
		for (IAEItemStack io : inputs)
		{
			if ( io == null )
				continue;

			IAEItemStack g = tmpInputs.get( io );
			if ( g == null )
				tmpInputs.put( io, io.copy() );
			else
				g.add( io );
		}

		if ( tmpInputs.isEmpty() )
			throw new InvalidPatternException( InvalidPatternException.REASON_NO_INPUT );
		if ( tmpOutputs.isEmpty() )
			throw new InvalidPatternException( InvalidPatternException.REASON_NO_OUTPUT );

		int offset = 0;
		condensedInputs = new IAEItemStack[tmpInputs.size()];
		for (IAEItemStack io : tmpInputs.values())
			condensedInputs[offset++] = io;

		offset = 0;
		condensedOutputs = new IAEItemStack[tmpOutputs.size()];
		for (IAEItemStack io : tmpOutputs.values())
			condensedOutputs[offset++] = io;
	}

	@Override
	synchronized public boolean isValidItemForSlot(int slotIndex, ItemStack i, World w)
	{
		if ( !isCrafting )
		{
			throw new RuntimeException( "Only crafting recipes supported." );
		}

		TestStatus result = getStatus( slotIndex, i );

		switch (result)
		{
		case ACCEPT:
			return true;
		case DECLINE:
			return false;
		case TEST:
		default:
			break;
		}

		for (int x = 0; x < crafting.getSizeInventory(); x++)
			testFrame.setInventorySlotContents( x, crafting.getStackInSlot( x ) );

		testFrame.setInventorySlotContents( slotIndex, i );

		if ( standardRecipe.matches( testFrame, w ) )
		{
			ItemStack testOutput = standardRecipe.getCraftingResult( testFrame );

			if ( Platform.isSameItemPrecise( correctOutput, testOutput ) )
			{
				testFrame.setInventorySlotContents( slotIndex, crafting.getStackInSlot( slotIndex ) );
				markItemAs( slotIndex, i, TestStatus.ACCEPT );
				return true;
			}
		}
		else
		{
			ItemStack testOutput = CraftingManager.getInstance().findMatchingRecipe( testFrame, w );

			if ( Platform.isSameItemPrecise( correctOutput, testOutput ) )
			{
				testFrame.setInventorySlotContents( slotIndex, crafting.getStackInSlot( slotIndex ) );
				markItemAs( slotIndex, i, TestStatus.ACCEPT );
				return true;
			}
		}

		markItemAs( slotIndex, i, TestStatus.DECLINE );
		return false;
	}

	@Override
	public ItemStack getOutput(InventoryCrafting craftingInv, World w)
	{
		if ( !isCrafting )
			throw new RuntimeException( "Only crafting recipes supported." );

		for (int x = 0; x < craftingInv.getSizeInventory(); x++)
		{
			if ( !isValidItemForSlot( x, craftingInv.getStackInSlot( x ), w ) )
				return null;
		}

		if ( outputs != null && outputs.length > 0 )
			return outputs[0].getItemStack();

		return null;
	}

	@Override
	public boolean canSubstitute()
	{
		return false;
	}

	@Override
	public boolean isCraftable()
	{
		return isCrafting;
	}

	@Override
	public IAEItemStack[] getInputs()
	{
		return inputs;
	}

	@Override
	public IAEItemStack[] getOutputs()
	{
		return outputs;
	}

	@Override
	public ItemStack getPattern()
	{
		return patternItem;
	}

	@Override
	public IAEItemStack[] getCondensedInputs()
	{
		return condensedInputs;
	}

	@Override
	public IAEItemStack[] getCondensedOutputs()
	{
		return condensedOutputs;
	}

	@Override
	public int compareTo(PatternHelper o)
	{
		return ItemSorters.compareInt( o.priority, priority );
	}

	@Override
	public int hashCode()
	{
		return pattern.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		PatternHelper other = (PatternHelper) obj;
		if ( pattern != null && other.pattern != null )
			return pattern.equals( other.pattern );
		return false;
	}

	@Override
	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	@Override
	public int getPriority()
	{
		return priority;
	}
}
