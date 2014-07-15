package appeng.client.gui;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.SlotME;
import appeng.core.AEConfig;
import codechicken.nei.guihook.IContainerTooltipHandler;

public abstract class AEBaseMEGui extends AEBaseGui
{

	public AEBaseMEGui(Container container) {
		super( container );
	}

	public List<String> handleItemTooltip(ItemStack stack, int mousex, int mousey, List<String> currenttip)
	{
		if ( stack != null )
		{
			Slot s = getSlot( mousex, mousey );
			if ( s instanceof SlotME )
			{
				int BigNumber = AEConfig.instance.useTerminalUseLargeFont() ? 999 : 9999;

				IAEItemStack myStack = null;

				try
				{
					SlotME theSlotField = (SlotME) s;
					myStack = theSlotField.getAEStack();
				}
				catch (Throwable _)
				{
				}

				if ( myStack != null )
				{
					if ( myStack.getStackSize() > BigNumber || (myStack.getStackSize() > 1 && stack.isItemDamaged()) )
						currenttip.add( "\u00a77Items Stored: " + NumberFormat.getNumberInstance( Locale.US ).format( myStack.getStackSize() ) );

					if ( myStack.getCountRequestable() > 0 )
						currenttip.add( "\u00a77Items Requestable: " + NumberFormat.getNumberInstance( Locale.US ).format( myStack.getCountRequestable() ) );
				}
				else if ( stack.stackSize > BigNumber || (stack.stackSize > 1 && stack.isItemDamaged()) )
				{
					currenttip.add( "\u00a77Items Stored: " + NumberFormat.getNumberInstance( Locale.US ).format( stack.stackSize ) );
				}
			}
		}
		return currenttip;
	}

	// Vanillia version...
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
			catch (Throwable _)
			{
			}

			if ( myStack != null )
			{
				List currenttip = stack.getTooltip( this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips );

				if ( myStack.getStackSize() > BigNumber || (myStack.getStackSize() > 1 && stack.isItemDamaged()) )
					currenttip.add( "Items Stored: " + NumberFormat.getNumberInstance( Locale.US ).format( myStack.getStackSize() ) );

				if ( myStack.getCountRequestable() > 0 )
					currenttip.add( "Items Requestable: " + NumberFormat.getNumberInstance( Locale.US ).format( myStack.getCountRequestable() ) );

				drawTooltip( x, y, 0, join( currenttip, "\n" ) );
			}
			else if ( stack != null && stack.stackSize > BigNumber )
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