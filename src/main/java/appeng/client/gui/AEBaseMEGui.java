package appeng.client.gui;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.SlotME;
import appeng.core.AEConfig;

public abstract class AEBaseMEGui extends AEBaseGui
{

	public AEBaseMEGui(Container container) {
		super( container );
	}

	public List<String> handleItemTooltip(ItemStack stack, int mouseX, int mouseY, List<String> currentToolTip)
	{
		if ( stack != null )
		{
			Slot s = getSlot( mouseX, mouseY );
			if ( s instanceof SlotME )
			{
				int BigNumber = AEConfig.instance.useTerminalUseLargeFont() ? 999 : 9999;

				IAEItemStack myStack = null;

				try
				{
					SlotME theSlotField = (SlotME) s;
					myStack = theSlotField.getAEStack();
				}
				catch (Throwable ignore)
				{
				}

				if ( myStack != null )
				{
					if ( myStack.getStackSize() > BigNumber || (myStack.getStackSize() > 1 && stack.isItemDamaged()) )
						currentToolTip.add( "\u00a77Items Stored: " + NumberFormat.getNumberInstance( Locale.US ).format( myStack.getStackSize() ) );

					if ( myStack.getCountRequestable() > 0 )
						currentToolTip.add( "\u00a77Items Requestable: " + NumberFormat.getNumberInstance( Locale.US ).format( myStack.getCountRequestable() ) );
				}
				else if ( stack.stackSize > BigNumber || (stack.stackSize > 1 && stack.isItemDamaged()) )
				{
					currentToolTip.add( "\u00a77Items Stored: " + NumberFormat.getNumberInstance( Locale.US ).format( stack.stackSize ) );
				}
			}
		}
		return currentToolTip;
	}

	// Vanilla version...
	// protected void drawItemStackTooltip(ItemStack stack, int x, int y)
	@Override
	protected void renderToolTip(ItemStack stack, int x, int y)
	{
		Slot s = getSlot( x, y );
		if ( s instanceof SlotME && stack != null )
		{
			int BigNumber = AEConfig.instance.useTerminalUseLargeFont() ? 999 : 9999;

			IAEItemStack myStack = null;

			try
			{
				SlotME theSlotField = (SlotME) s;
				myStack = theSlotField.getAEStack();
			}
			catch (Throwable ignore)
			{
			}

			if ( myStack != null )
			{
				List currentToolTip = stack.getTooltip( this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips );

				if ( myStack.getStackSize() > BigNumber || (myStack.getStackSize() > 1 && stack.isItemDamaged()) )
					currentToolTip.add( "Items Stored: " + NumberFormat.getNumberInstance( Locale.US ).format( myStack.getStackSize() ) );

				if ( myStack.getCountRequestable() > 0 )
					currentToolTip.add( "Items Requestable: " + NumberFormat.getNumberInstance( Locale.US ).format( myStack.getCountRequestable() ) );

				drawTooltip( x, y, 0, join( currentToolTip, "\n" ) );
			}
			else if ( stack.stackSize > BigNumber )
			{
				List var4 = stack.getTooltip( this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips );
				var4.add( "Items Stored: " + NumberFormat.getNumberInstance( Locale.US ).format( stack.stackSize ) );
				drawTooltip( x, y, 0, join( var4, "\n" ) );
				return;
			}
		}
		super.renderToolTip( stack, x, y );
		// super.drawItemStackTooltip( stack, x, y );
	}

}