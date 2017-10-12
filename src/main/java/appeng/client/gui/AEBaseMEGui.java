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

package appeng.client.gui;


import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.SlotME;
import appeng.core.AEConfig;
import appeng.core.localization.ButtonToolTips;


public abstract class AEBaseMEGui extends AEBaseGui
{

	public AEBaseMEGui( final Container container )
	{
		super( container );
	}

	public List<String> handleItemTooltip( final ItemStack stack, final int mouseX, final int mouseY, final List<String> currentToolTip )
	{
		if( !stack.isEmpty() )
		{
			final Slot s = this.getSlot( mouseX, mouseY );
			if( s instanceof SlotME )
			{
				final int BigNumber = AEConfig.instance().useTerminalUseLargeFont() ? 999 : 9999;

				IAEItemStack myStack = null;

				try
				{
					final SlotME theSlotField = (SlotME) s;
					myStack = theSlotField.getAEStack();
				}
				catch( final Throwable ignore )
				{
				}

				if( myStack != null )
				{
					if( myStack.getStackSize() > BigNumber || ( myStack.getStackSize() > 1 && stack.isItemDamaged() ) )
					{
						final String local = ButtonToolTips.ItemsStored.getLocal();
						final String formattedAmount = NumberFormat.getNumberInstance( Locale.US ).format( myStack.getStackSize() );
						final String format = String.format( local, formattedAmount );

						currentToolTip.add( TextFormatting.GRAY + format );
					}

					if( myStack.getCountRequestable() > 0 )
					{
						final String local = ButtonToolTips.ItemsRequestable.getLocal();
						final String formattedAmount = NumberFormat.getNumberInstance( Locale.US ).format( myStack.getCountRequestable() );
						final String format = String.format( local, formattedAmount );

						currentToolTip.add( TextFormatting.GRAY + format );
					}
				}
				else if( stack.getCount() > BigNumber || ( stack.getCount() > 1 && stack.isItemDamaged() ) )
				{
					final String local = ButtonToolTips.ItemsStored.getLocal();
					final String formattedAmount = NumberFormat.getNumberInstance( Locale.US ).format( stack.getCount() );
					final String format = String.format( local, formattedAmount );

					currentToolTip.add( TextFormatting.GRAY + format );
				}
			}
		}
		return currentToolTip;
	}

	// Vanilla version...
	// protected void drawItemStackTooltip(ItemStack stack, int x, int y)
	@Override
	protected void renderToolTip( final ItemStack stack, final int x, final int y )
	{
		final Slot s = this.getSlot( x, y );
		if( s instanceof SlotME && !stack.isEmpty() )
		{
			final int BigNumber = AEConfig.instance().useTerminalUseLargeFont() ? 999 : 9999;

			IAEItemStack myStack = null;

			try
			{
				final SlotME theSlotField = (SlotME) s;
				myStack = theSlotField.getAEStack();
			}
			catch( final Throwable ignore )
			{
			}

			ITooltipFlag.TooltipFlags tooltipFlag = this.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
			if( myStack != null )
			{
				final List<String> currentToolTip = stack.getTooltip( this.mc.player, tooltipFlag );

				if( myStack.getStackSize() > BigNumber || ( myStack.getStackSize() > 1 && stack.isItemDamaged() ) )
				{
					currentToolTip.add( "Items Stored: " + NumberFormat.getNumberInstance( Locale.US ).format( myStack.getStackSize() ) );
				}

				if( myStack.getCountRequestable() > 0 )
				{
					currentToolTip.add( "Items Requestable: " + NumberFormat.getNumberInstance( Locale.US ).format( myStack.getCountRequestable() ) );
				}

				this.drawTooltip( x, y, currentToolTip );
				return;
			}
			else if( stack.getCount() > BigNumber )
			{
				List<String> var4 = stack.getTooltip( this.mc.player, tooltipFlag );
				var4.add( "Items Stored: " + NumberFormat.getNumberInstance( Locale.US ).format( stack.getCount() ) );
				this.drawTooltip( x, y, var4 );
				return;
			}
		}
		super.renderToolTip( stack, x, y );
		// super.drawItemStackTooltip( stack, x, y );
	}
}