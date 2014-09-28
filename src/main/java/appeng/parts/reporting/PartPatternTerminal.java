package appeng.parts.reporting;

import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.core.sync.GuiBridge;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;

public class PartPatternTerminal extends PartTerminal implements IAEAppEngInventory
{

	AppEngInternalInventory crafting = new AppEngInternalInventory( this, 9 );
	AppEngInternalInventory output = new AppEngInternalInventory( this, 3 );
	AppEngInternalInventory pattern = new AppEngInternalInventory( this, 2 );

	private boolean craftingMode = true;

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT( data );
		data.setBoolean( "craftingMode", isCraftingRecipe() );
		pattern.writeToNBT( data, "pattern" );
		output.writeToNBT( data, "outputList" );
		crafting.writeToNBT( data, "craftingGrid" );
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT( data );
		setCraftingRecipe( data.getBoolean( "craftingMode" ) );
		pattern.readFromNBT( data, "pattern" );
		output.readFromNBT( data, "outputList" );
		crafting.readFromNBT( data, "craftingGrid" );
	}

	@Override
	public void getDrops(List<ItemStack> drops, boolean wrenched)
	{
		for (ItemStack is : pattern)
			if ( is != null )
				drops.add( is );
	}

	public PartPatternTerminal(ItemStack is) {
		super( PartPatternTerminal.class, is );
		frontBright = CableBusTextures.PartPatternTerm_Bright;
		frontColored = CableBusTextures.PartPatternTerm_Colored;
		frontDark = CableBusTextures.PartPatternTerm_Dark;
	}

	public GuiBridge getGui()
	{
		return GuiBridge.GUI_PATTERN_TERMINAL;
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		if ( name.equals( "crafting" ) )
			return crafting;

		if ( name.equals( "output" ) )
			return output;

		if ( name.equals( "pattern" ) )
			return pattern;

		return super.getInventoryByName( name );
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{
		if ( inv == pattern && slot == 1 )
		{
			ItemStack is = pattern.getStackInSlot( 1 );
			if ( is != null && is.getItem() instanceof ICraftingPatternItem )
			{
				ICraftingPatternItem pattern = (ICraftingPatternItem) is.getItem();
				ICraftingPatternDetails details = pattern.getPatternForItem( is, this.getHost().getTile().getWorldObj() );
				if ( details != null )
				{
					setCraftingRecipe( details.isCraftable() );

					for (int x = 0; x < crafting.getSizeInventory() && x < details.getInputs().length; x++)
					{
						IAEItemStack item = details.getInputs()[x];
						crafting.setInventorySlotContents( x, item == null ? null : item.getItemStack() );
					}

					for (int x = 0; x < output.getSizeInventory() && x < details.getOutputs().length; x++)
					{
						IAEItemStack item = details.getOutputs()[x];
						output.setInventorySlotContents( x, item == null ? null : item.getItemStack() );
					}
				}
			}
		}
		else if ( inv == crafting )
		{
			fixCraftingRecipes();
		}

		host.markForSave();
	}

	public boolean isCraftingRecipe()
	{
		return craftingMode;
	}

	public void setCraftingRecipe(boolean craftingMode)
	{
		this.craftingMode = craftingMode;
		fixCraftingRecipes();
	}

	private void fixCraftingRecipes()
	{
		if ( isCraftingRecipe() )
		{
			for (int x = 0; x < crafting.getSizeInventory(); x++)
			{
				ItemStack is = crafting.getStackInSlot( x );
				if ( is != null )
					is.stackSize = 1;
			}
		}
	}
}
