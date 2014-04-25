package appeng.helpers;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import appeng.api.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.ContainerNull;
import appeng.util.Platform;

public class PatternHelper implements ICraftingPatternDetails
{

	InventoryCrafting crafting = new InventoryCrafting( new ContainerNull(), 3, 3 );
	InventoryCrafting testFrame = new InventoryCrafting( new ContainerNull(), 3, 3 );

	ItemStack correctOutput;
	IRecipe standardRecipe;

	boolean isCrafting = false;

	public PatternHelper(ItemStack is, World w) {
		NBTTagCompound encodedValue = is.getTagCompound();

		if ( encodedValue == null )
			return;

		NBTTagList inTag = encodedValue.getTagList( "in", 10 );
		NBTTagList outTag = encodedValue.getTagList( "out", 10 );
		isCrafting = encodedValue.getBoolean( "crafting" );

		if ( isCrafting == false )
			throw new RuntimeException( "Only crafting recipes supported." );

		for (int x = 0; x < inTag.tagCount(); x++)
		{
			ItemStack gs = ItemStack.loadItemStackFromNBT( inTag.getCompoundTagAt( x ) );
			crafting.setInventorySlotContents( x, gs );
			testFrame.setInventorySlotContents( x, gs );
		}

		standardRecipe = Platform.findMatchingRecipe( crafting, w );
		correctOutput = standardRecipe.getCraftingResult( crafting );
	}

	public boolean isValidItemForSlot(int slotIndex, ItemStack i, World w)
	{
		testFrame.setInventorySlotContents( slotIndex, i );
		boolean result = standardRecipe.matches( testFrame, w );

		if ( result )
		{
			ItemStack testOutput = standardRecipe.getCraftingResult( testFrame );

			if ( Platform.isSameItemPrecise( correctOutput, testOutput ) )
			{
				testFrame.setInventorySlotContents( slotIndex, crafting.getStackInSlot( slotIndex ) );
				return true;
			}
		}
		else
		{
			ItemStack testOutput = CraftingManager.getInstance().findMatchingRecipe( testFrame, w );

			if ( Platform.isSameItemPrecise( correctOutput, testOutput ) )
			{
				testFrame.setInventorySlotContents( slotIndex, crafting.getStackInSlot( slotIndex ) );
				return true;
			}
		}

		return false;
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IAEItemStack[] getInputs()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAEItemStack[] getOutputs()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
