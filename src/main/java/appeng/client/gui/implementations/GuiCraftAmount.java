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

package appeng.client.gui.implementations;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketCraftRequest;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartTerminal;

public class GuiCraftAmount extends AEBaseGui
{

	GuiNumberBox amountToCraft;
	GuiTabButton originalGuiBtn;

	GuiButton next;

	GuiButton plus1, plus10, plus100, plus1000;
	GuiButton minus1, minus10, minus100, minus1000;

	GuiBridge OriginalGui;

	public GuiCraftAmount(InventoryPlayer inventoryPlayer, ITerminalHost te) {
		super( new ContainerCraftAmount( inventoryPlayer, te ) );
	}

	@Override
	public void initGui()
	{
		super.initGui();

		int a = AEConfig.instance.craftItemsByStackAmounts( 0 );
		int b = AEConfig.instance.craftItemsByStackAmounts( 1 );
		int c = AEConfig.instance.craftItemsByStackAmounts( 2 );
		int d = AEConfig.instance.craftItemsByStackAmounts( 3 );

		buttonList.add( plus1 = new GuiButton( 0, this.guiLeft + 20, this.guiTop + 26, 22, 20, "+" + a ) );
		buttonList.add( plus10 = new GuiButton( 0, this.guiLeft + 48, this.guiTop + 26, 28, 20, "+" + b ) );
		buttonList.add( plus100 = new GuiButton( 0, this.guiLeft + 82, this.guiTop + 26, 32, 20, "+" + c ) );
		buttonList.add( plus1000 = new GuiButton( 0, this.guiLeft + 120, this.guiTop + 26, 38, 20, "+" + d ) );

		buttonList.add( minus1 = new GuiButton( 0, this.guiLeft + 20, this.guiTop + 75, 22, 20, "-" + a ) );
		buttonList.add( minus10 = new GuiButton( 0, this.guiLeft + 48, this.guiTop + 75, 28, 20, "-" + b ) );
		buttonList.add( minus100 = new GuiButton( 0, this.guiLeft + 82, this.guiTop + 75, 32, 20, "-" + c ) );
		buttonList.add( minus1000 = new GuiButton( 0, this.guiLeft + 120, this.guiTop + 75, 38, 20, "-" + d ) );

		buttonList.add( next = new GuiButton( 0, this.guiLeft + 128, this.guiTop + 51, 38, 20, GuiText.Next.getLocal() ) );

		ItemStack myIcon = null;
		Object target = ((AEBaseContainer) inventorySlots).getTarget();

		if ( target instanceof WirelessTerminalGuiObject )
		{
			myIcon = AEApi.instance().items().itemWirelessTerminal.stack( 1 );
			OriginalGui = GuiBridge.GUI_WIRELESS_TERM;
		}

		if ( target instanceof PartTerminal )
		{
			myIcon = AEApi.instance().parts().partTerminal.stack( 1 );
			OriginalGui = GuiBridge.GUI_ME;
		}

		if ( target instanceof PartCraftingTerminal )
		{
			myIcon = AEApi.instance().parts().partCraftingTerminal.stack( 1 );
			OriginalGui = GuiBridge.GUI_CRAFTING_TERMINAL;
		}

		if ( target instanceof PartPatternTerminal )
		{
			myIcon = AEApi.instance().parts().partPatternTerminal.stack( 1 );
			OriginalGui = GuiBridge.GUI_PATTERN_TERMINAL;
		}

		if ( OriginalGui != null )
			buttonList.add( originalGuiBtn = new GuiTabButton( this.guiLeft + 154, this.guiTop, myIcon, myIcon.getDisplayName(), itemRender ) );

		amountToCraft = new GuiNumberBox( fontRendererObj, this.guiLeft + 62, this.guiTop + 57, 59, fontRendererObj.FONT_HEIGHT, Integer.class );
		amountToCraft.setEnableBackgroundDrawing( false );
		amountToCraft.setMaxStringLength( 16 );
		amountToCraft.setTextColor( 0xFFFFFF );
		amountToCraft.setVisible( true );
		amountToCraft.setFocused( true );
		amountToCraft.setText( "1" );
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		try
		{

			if ( btn == originalGuiBtn )
			{
				NetworkHandler.instance.sendToServer( new PacketSwitchGuis( OriginalGui ) );
			}

			if ( btn == next )
			{
				NetworkHandler.instance.sendToServer( new PacketCraftRequest( Integer.parseInt( this.amountToCraft.getText() ), isShiftKeyDown() ) );
			}

		}
		catch (NumberFormatException e)
		{
			// nope..
			amountToCraft.setText( "1" );
		}

		boolean isPlus = btn == plus1 || btn == plus10 || btn == plus100 || btn == plus1000;
		boolean isMinus = btn == minus1 || btn == minus10 || btn == minus100 || btn == minus1000;

		if ( isPlus || isMinus )
			addQty( getQty( btn ) );
	}

	private void addQty(int i)
	{
		try
		{
			String Out = amountToCraft.getText();

			boolean Fixed = false;
			while (Out.startsWith( "0" ) && Out.length() > 1)
			{
				Out = Out.substring( 1 );
				Fixed = true;
			}

			if ( Fixed )
				amountToCraft.setText( Out );

			if ( Out.length() == 0 )
				Out = "0";

			long result = Integer.parseInt( Out );

			if ( result == 1 && i > 1 )
				result = 0;

			result += i;
			if ( result < 1 )
				result = 1;

			Out = Long.toString( result );
			Integer.parseInt( Out );
			amountToCraft.setText( Out );
		}
		catch (NumberFormatException e)
		{
			// :P
		}
	}

	@Override
	protected void keyTyped(char character, int key)
	{
		if ( !this.checkHotbarKeys( key ) )
		{
			if ( key == 28 )
			{
				actionPerformed( next );
			}
			if ( (key == 211 || key == 205 || key == 203 || key == 14 || character == '-' || Character.isDigit( character ))
					&& amountToCraft.textboxKeyTyped( character, key ) )
			{
				try
				{
					String Out = amountToCraft.getText();

					boolean Fixed = false;
					while (Out.startsWith( "0" ) && Out.length() > 1)
					{
						Out = Out.substring( 1 );
						Fixed = true;
					}

					if ( Fixed )
						amountToCraft.setText( Out );

					if ( Out.length() == 0 )
						Out = "0";

					long result = Long.parseLong( Out );
					if ( result < 0 )
					{
						amountToCraft.setText( "1" );
					}
				}
				catch (NumberFormatException e)
				{
					// :P
				}
			}
			else
			{
				super.keyTyped( character, key );
			}
		}
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		next.displayString = isShiftKeyDown() ? GuiText.Start.getLocal() : GuiText.Next.getLocal();

		bindTexture( "guis/craftAmt.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );

		try
		{
			Long.parseLong( amountToCraft.getText() );
			next.enabled = amountToCraft.getText().length() > 0;
		}
		catch (NumberFormatException e)
		{
			next.enabled = false;
		}

		amountToCraft.drawTextBox();
	}

	protected String getBackground()
	{
		return "guis/craftAmt.png";
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( GuiText.SelectAmount.getLocal(), 8, 6, 4210752 );
	}
}
