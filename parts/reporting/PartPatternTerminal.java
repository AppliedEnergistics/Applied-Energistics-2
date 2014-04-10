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

	AppEngInternalInventory crafting = new AppEngInternalInventory( this, 9 );
	AppEngInternalInventory output = new AppEngInternalInventory( this, 3 );
	AppEngInternalInventory pattern = new AppEngInternalInventory( this, 2 );

	public boolean craftingMode = true;

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT( data );
		data.setBoolean( "craftingMode", craftingMode );
		pattern.writeToNBT( data, "pattern" );
		output.writeToNBT( data, "outputList" );
		crafting.writeToNBT( data, "craftingGrid" );
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT( data );
		craftingMode = data.getBoolean( "craftingMode" );
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
		host.markForSave();
	}
}
