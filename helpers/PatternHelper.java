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
	private IAEItemStack aepattern;

	final InventoryCrafting crafting = new InventoryCrafting( new ContainerNull(), 3, 3 );
	final InventoryCrafting testFrame = new InventoryCrafting( new ContainerNull(), 3, 3 );

	final ItemStack correctOutput;
	final IRecipe standardRecipe;

	final IAEItemStack condencedInputs[];
	final IAEItemStack condencedOutputs[];
	final IAEItemStack inputs[];
	final IAEItemStack outputs[];

	final boolean isCrafting;
	public int priority = 0;

	class TestLookup
	{

		final int slot;
		final int ref;
		final int hash;

		public TestLookup(int slot, ItemStack i) {
			this( slot, i.getItem(), i.getItemDamage() );
		}

		public TestLookup(int slot, Item item, int dmg) {
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
			TestLookup b = (TestLookup) obj;
			return b.slot == slot && b.ref == ref;
		}

	};

	enum TestStatus
	{
		ACCEPT, DECLINE, TEST
	};

	HashSet<TestLookup> failCache = new HashSet();
	HashSet<TestLookup> passCache = new HashSet();

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

	public PatternHelper(ItemStack is, World w) {
		NBTTagCompound encodedValue = is.getTagCompound();

		if ( encodedValue == null )
			throw new RuntimeException( "No pattern here!" );

		NBTTagList inTag = encodedValue.getTagList( "in", 10 );
		NBTTagList outTag = encodedValue.getTagList( "out", 10 );
		isCrafting = encodedValue.getBoolean( "crafting" );
		patternItem = is;
		aepattern = AEItemStack.create( is );

		List<IAEItemStack> in = new ArrayList();
		List<IAEItemStack> out = new ArrayList();

		for (int x = 0; x < inTag.tagCount(); x++)
		{
			ItemStack gs = ItemStack.loadItemStackFromNBT( inTag.getCompoundTagAt( x ) );
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
				throw new RuntimeException( "No pattern here!" );
		}
		else
		{
			standardRecipe = null;
			correctOutput = null;

			for (int x = 0; x < outTag.tagCount(); x++)
			{
				ItemStack gs = ItemStack.loadItemStackFromNBT( outTag.getCompoundTagAt( x ) );
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
		
		if ( tmpOutputs.isEmpty() || tmpInputs.isEmpty() )
			throw new RuntimeException( "No pattern here!" );
		
		int offset = 0;
		condencedInputs = new IAEItemStack[tmpInputs.size()];
		for (IAEItemStack io : tmpInputs.values())
			condencedInputs[offset++] = io;

		offset = 0;
		condencedOutputs = new IAEItemStack[tmpOutputs.size()];
		for (IAEItemStack io : tmpOutputs.values())
			condencedOutputs[offset++] = io;
	}

	synchronized public boolean isValidItemForSlot(int slotIndex, ItemStack i, World w)
	{
		if ( isCrafting == false )
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

		if ( !isCrafting )
			return false;

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
		if ( isCrafting == false )
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
		// TODO Auto-generated method stub
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
	public IAEItemStack[] getCondencedInputs()
	{
		return condencedInputs;
	}

	@Override
	public IAEItemStack[] getCondensedOutputs()
	{
		return condencedOutputs;
	}

	@Override
	public int compareTo(PatternHelper o)
	{
		return ItemSorters.compareInt( o.priority, priority );
	}

	@Override
	public int hashCode()
	{
		return aepattern.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		return aepattern.equals( ((PatternHelper) obj).aepattern );
	}

	@Override
	public void setPriority(int priority)
	{
		this.priority = priority;
	}
}
