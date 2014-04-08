package appeng.parts.reporting;

import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.client.texture.CableBusTextures;
import appeng.core.sync.GuiBridge;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;

public class PartPatternTerminal extends PartTerminal implements IAEAppEngInventory
{

	AppEngInternalInventory craftingGrid = new AppEngInternalInventory( this, 9 + 3 );
	AppEngInternalInventory pattern = new AppEngInternalInventory( this, 1 );

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT( data );
		pattern.writeToNBT( data, "pattern" );
		craftingGrid.writeToNBT( data, "craftingGrid" );
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT( data );
		pattern.readFromNBT( data, "pattern" );
		craftingGrid.readFromNBT( data, "craftingGrid" );
	}

	@Override
	public void getDrops(List<ItemStack> drops, boolean wrenched)
	{
		for (ItemStack is : craftingGrid)
			if ( is != null )
				drops.add( is );

		for (ItemStack is : pattern)
			if ( is != null )
				drops.add( is );
	}

	public PartPatternTerminal(ItemStack is) {
		super( PartPatternTerminal.class, is );
		frontBright = CableBusTextures.PartPatternTerm_Bright;
		frontColored = CableBusTextures.PartPatternTerm_Colored;
		frontDark = CableBusTextures.PartPatternTerm_Dark;
		// frontSolid = CableBusTextures.PartPatternTerm_Solid;
	}

	public GuiBridge getGui()
	{
		return GuiBridge.GUI_PATTERN_TERMINAL;
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		if ( name.equals( "crafting" ) )
			return craftingGrid;
		if ( name.equals( "pattern" ) )
			return pattern;
		return super.getInventoryByName( name );
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{
		host.markForSave();
	}
}
