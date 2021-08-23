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


import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.ContainerNull;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import net.minecraftforge.common.crafting.IShapedRecipe;


public class PatternHelper implements ICraftingPatternDetails, Comparable<PatternHelper>
{

	private static final int CRAFTING_GRID_DIMENSION = 3;
	private static final int ALL_INPUT_LIMIT = CRAFTING_GRID_DIMENSION * CRAFTING_GRID_DIMENSION;
	private static final int CRAFTING_OUTPUT_LIMIT = 1;
	private static final int PROCESSING_OUTPUT_LIMIT = 3;

	private final ItemStack patternItem;
	private final InventoryCrafting crafting = new InventoryCrafting( new ContainerNull(), 3, 3 );
	private final InventoryCrafting testFrame = new InventoryCrafting( new ContainerNull(), 3, 3 );
	private final ItemStack correctOutput;
	private final IRecipe standardRecipe;
	private final IAEItemStack[] condensedInputs;
	private final IAEItemStack[] condensedOutputs;
	private final IAEItemStack[] inputs;
	private final IAEItemStack[] outputs;
	private final Map<Integer, List<IAEItemStack>> substituteInputs;
	private final boolean isCrafting;
	private final boolean canSubstitute;
	private final Set<TestLookup> failCache = new HashSet<>();
	private final Set<TestLookup> passCache = new HashSet<>();
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

		this.canSubstitute = this.isCrafting && encodedValue.getBoolean( "substitute" );
		this.patternItem = is;
		this.pattern = AEItemStack.fromItemStack( is );

		final List<IAEItemStack> in = new ArrayList<>();
		final List<IAEItemStack> out = new ArrayList<>();

		for( int x = 0; x < inTag.tagCount(); x++ )
		{
			NBTTagCompound ingredient = inTag.getCompoundTagAt( x );
			final ItemStack gs = new ItemStack( ingredient );

			if( !ingredient.hasNoTags() && gs.isEmpty() )
			{
				throw new IllegalArgumentException( "No pattern here!" );
			}

			this.crafting.setInventorySlotContents( x, gs );

			if( !gs.isEmpty() && ( !this.isCrafting || !gs.hasTagCompound() ) )
			{
				this.markItemAs( x, gs, TestStatus.ACCEPT );
			}

			in.add( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createStack( gs ) );
			this.testFrame.setInventorySlotContents( x, gs );
		}

		if( this.isCrafting )
		{
			this.standardRecipe = CraftingManager.findMatchingRecipe( this.crafting, w );

			if( this.standardRecipe != null )
			{
				this.correctOutput = this.standardRecipe.getCraftingResult( this.crafting );
				out.add( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createStack( this.correctOutput ) );
			}
			else
			{
				throw new IllegalStateException( "No pattern here!" );
			}
		}
		else
		{
			this.standardRecipe = null;
			this.correctOutput = ItemStack.EMPTY;

			for( int x = 0; x < outTag.tagCount(); x++ )
			{
				NBTTagCompound resultItemTag = outTag.getCompoundTagAt( x );
				final ItemStack gs = new ItemStack( resultItemTag );

				if( !resultItemTag.hasNoTags() && gs.isEmpty() )
				{
					throw new IllegalArgumentException( "No pattern here!" );
				}

				if( !gs.isEmpty() )
				{
					out.add( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createStack( gs ) );
				}
			}
		}
		final int outputLength = out.size();

		this.inputs = in.toArray(new IAEItemStack[ALL_INPUT_LIMIT]);
		this.outputs = out.toArray(new IAEItemStack[outputLength]);
		this.substituteInputs = new HashMap<>(ALL_INPUT_LIMIT);

		final Map<IAEItemStack, IAEItemStack> tmpOutputs = new HashMap<>();

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

		final Map<IAEItemStack, IAEItemStack> tmpInputs = new HashMap<>();

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

		// If we cannot substitute, the items must match exactly
		if (!canSubstitute && slotIndex < inputs.length) {
			if (!inputs[slotIndex].isSameType(i)) {
				this.markItemAs(slotIndex, i, TestStatus.DECLINE);
				return false;
			}
		}

		if( this.standardRecipe.matches( this.testFrame, w ) )
		{
			final ItemStack testOutput = this.standardRecipe.getCraftingResult( this.testFrame );

			if( Platform.itemComparisons().isSameItem( this.correctOutput, testOutput ) )
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
	public List<IAEItemStack> getSubstituteInputs(int slot) {
		if (this.inputs[slot] == null) {
			return Collections.emptyList();
		}

		return this.substituteInputs.computeIfAbsent(slot, value -> {
			ItemStack[] matchingStacks = getRecipeIngredient(slot).getMatchingStacks();
			List<IAEItemStack> itemList = new ArrayList<>(matchingStacks.length + 1);
			for (ItemStack matchingStack : matchingStacks) {
				itemList.add(AEItemStack.fromItemStack(matchingStack));
			}

			// Ensure that the specific item put in by the user is at the beginning,
			// so that it takes precedence over substitutions
			itemList.add(0, this.inputs[slot]);
			return itemList;
		});
	}

	/**
	 * Gets the {@link Ingredient} from the actual used recipe for a given slot-index into {@link #getInputs()}.
	 * <p/>
	 * Conversion is needed for two reasons: our sparse ingredients are always organized in a 3x3 grid, while Vanilla's
	 * ingredient list will be condensed to the actual recipe's grid size. In addition, in our 3x3 grid, the user can
	 * shift the actual recipe input to the right and down.
	 */
	private Ingredient getRecipeIngredient(int slot) {

		if (standardRecipe instanceof IShapedRecipe ) {
			IShapedRecipe shapedRecipe = (IShapedRecipe) standardRecipe;

			return getShapedRecipeIngredient(slot, shapedRecipe.getRecipeWidth());
		} else {
			return getShapelessRecipeIngredient(slot);
		}
	}

	private Ingredient getShapedRecipeIngredient(int slot, int recipeWidth) {
		// Compute the offset of the user's input vs. crafting grid origin
		// Which is >0 if they have empty rows above or to the left of their input
		int topOffset = 0;
		if (inputs[0] == null && inputs[1] == null && inputs[2] == null) {
			topOffset++; // First row is fully empty
			if (inputs[3] == null && inputs[4] == null && inputs[5] == null) {
				topOffset++; // Second row is fully empty
			}
		}
		int leftOffset = 0;
		if (inputs[0] == null && inputs[3] == null && inputs[6] == null) {
			leftOffset++; // First column is fully empty
			if (inputs[1] == null && inputs[4] == null && inputs[7] == null) {
				leftOffset++; // Second column is fully empty
			}
		}

		// Compute the x,y of the slot, as-if the recipe was anchored to 0,0
		int slotX = slot % CRAFTING_GRID_DIMENSION - leftOffset;
		int slotY = slot / CRAFTING_GRID_DIMENSION - topOffset;

		// Compute the index into the recipe's ingredient list now
		int ingredientIndex = slotY * recipeWidth + slotX;

		NonNullList<Ingredient> ingredients = standardRecipe.getIngredients();

		if (ingredientIndex < 0 || ingredientIndex > ingredients.size()) {
			return Ingredient.EMPTY;
		}

		return ingredients.get(ingredientIndex);
	}

	private Ingredient getShapelessRecipeIngredient(int slot) {
		// We map the list of *filled* sparse inputs to the shapeless (ergo unordered)
		// ingredients. While these do not actually correspond to each other,
		// since both lists have the same length, the mapping is at least stable.
		int ingredientIndex = 0;
		for (int i = 0; i < slot; i++) {
			if (inputs[i] != null) {
				ingredientIndex++;
			}
		}

		NonNullList<Ingredient> ingredients = standardRecipe.getIngredients();
		if (ingredientIndex < ingredients.size()) {
			return ingredients.get(ingredientIndex);
		}

		return Ingredient.EMPTY;
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
				return ItemStack.EMPTY;
			}
		}

		if( this.outputs != null && this.outputs.length > 0 )
		{
			return this.outputs[0].createItemStack();
		}

		return ItemStack.EMPTY;
	}

	private TestStatus getStatus( final int slotIndex, final ItemStack i )
	{
		if( this.crafting.getStackInSlot( slotIndex ).isEmpty() )
		{
			return i.isEmpty() ? TestStatus.ACCEPT : TestStatus.DECLINE;
		}

		if( i.isEmpty() )
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
		return Integer.compare( o.priority, this.priority );
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
