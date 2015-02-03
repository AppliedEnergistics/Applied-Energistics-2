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

		this.buttonList.add( this.plus1 = new GuiButton( 0, this.guiLeft + 20, this.guiTop + 26, 22, 20, "+" + a ) );
		this.buttonList.add( this.plus10 = new GuiButton( 0, this.guiLeft + 48, this.guiTop + 26, 28, 20, "+" + b ) );
		this.buttonList.add( this.plus100 = new GuiButton( 0, this.guiLeft + 82, this.guiTop + 26, 32, 20, "+" + c ) );
		this.buttonList.add( this.plus1000 = new GuiButton( 0, this.guiLeft + 120, this.guiTop + 26, 38, 20, "+" + d ) );

		this.buttonList.add( this.minus1 = new GuiButton( 0, this.guiLeft + 20, this.guiTop + 75, 22, 20, "-" + a ) );
		this.buttonList.add( this.minus10 = new GuiButton( 0, this.guiLeft + 48, this.guiTop + 75, 28, 20, "-" + b ) );
		this.buttonList.add( this.minus100 = new GuiButton( 0, this.guiLeft + 82, this.guiTop + 75, 32, 20, "-" + c ) );
		this.buttonList.add( this.minus1000 = new GuiButton( 0, this.guiLeft + 120, this.guiTop + 75, 38, 20, "-" + d ) );

		this.buttonList.add( this.next = new GuiButton( 0, this.guiLeft + 128, this.guiTop + 51, 38, 20, GuiText.Next.getLocal() ) );

		ItemStack myIcon = null;
		Object target = ((AEBaseContainer) this.inventorySlots).getTarget();

		if ( target instanceof WirelessTerminalGuiObject )
		{
			myIcon = AEApi.instance().definitions().items().wirelessTerminal().get().stack( 1 );
			this.OriginalGui = GuiBridge.GUI_WIRELESS_TERM;
		}

		if ( target instanceof PartTerminal )
		{
			myIcon = AEApi.instance().definitions().parts().terminal().get().stack( 1 );
			this.OriginalGui = GuiBridge.GUI_ME;
		}

		if ( target instanceof PartCraftingTerminal )
		{
			myIcon = AEApi.instance().definitions().parts().craftingTerminal().get().stack( 1 );
			this.OriginalGui = GuiBridge.GUI_CRAFTING_TERMINAL;
		}

		if ( target instanceof PartPatternTerminal )
		{
			myIcon = AEApi.instance().definitions().parts().patternTerminal().get().stack( 1 );
			this.OriginalGui = GuiBridge.GUI_PATTERN_TERMINAL;
		}

		if ( this.OriginalGui != null )
			this.buttonList.add( this.originalGuiBtn = new GuiTabButton( this.guiLeft + 154, this.guiTop, myIcon, myIcon.getDisplayName(), itemRender ) );

		this.amountToCraft = new GuiNumberBox( this.fontRendererObj, this.guiLeft + 62, this.guiTop + 57, 59, this.fontRendererObj.FONT_HEIGHT, Integer.class );
		this.amountToCraft.setEnableBackgroundDrawing( false );
		this.amountToCraft.setMaxStringLength( 16 );
		this.amountToCraft.setTextColor( 0xFFFFFF );
		this.amountToCraft.setVisible( true );
		this.amountToCraft.setFocused( true );
		this.amountToCraft.setText( "1" );
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		try
		{

			if ( btn == this.originalGuiBtn )
			{
				NetworkHandler.instance.sendToServer( new PacketSwitchGuis( this.OriginalGui ) );
			}

			if ( btn == this.next )
			{
				NetworkHandler.instance.sendToServer( new PacketCraftRequest( Integer.parseInt( this.amountToCraft.getText() ), isShiftKeyDown() ) );
			}

		}
		catch (NumberFormatException e)
		{
			// nope..
			this.amountToCraft.setText( "1" );
		}

		boolean isPlus = btn == this.plus1 || btn == this.plus10 || btn == this.plus100 || btn == this.plus1000;
		boolean isMinus = btn == this.minus1 || btn == this.minus10 || btn == this.minus100 || btn == this.minus1000;

		if ( isPlus || isMinus )
			this.addQty( this.getQty( btn ) );
	}

	private void addQty(int i)
	{
		try
		{
			String Out = this.amountToCraft.getText();

			boolean Fixed = false;
			while (Out.startsWith( "0" ) && Out.length() > 1)
			{
				Out = Out.substring( 1 );
				Fixed = true;
			}

			if ( Fixed )
				this.amountToCraft.setText( Out );

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
			this.amountToCraft.setText( Out );
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
				this.actionPerformed( this.next );
			}
			if ( (key == 211 || key == 205 || key == 203 || key == 14 || character == '-' || Character.isDigit( character ))
					&& this.amountToCraft.textboxKeyTyped( character, key ) )
			{
				try
				{
					String Out = this.amountToCraft.getText();

					boolean Fixed = false;
					while (Out.startsWith( "0" ) && Out.length() > 1)
					{
						Out = Out.substring( 1 );
						Fixed = true;
					}

					if ( Fixed )
						this.amountToCraft.setText( Out );

					if ( Out.length() == 0 )
						Out = "0";

					long result = Long.parseLong( Out );
					if ( result < 0 )
					{
						this.amountToCraft.setText( "1" );
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
		this.next.displayString = isShiftKeyDown() ? GuiText.Start.getLocal() : GuiText.Next.getLocal();

		this.bindTexture( "guis/craftAmt.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize );

		try
		{
			Long.parseLong( this.amountToCraft.getText() );
			this.next.enabled = this.amountToCraft.getText().length() > 0;
		}
		catch (NumberFormatException e)
		{
			this.next.enabled = false;
		}

		this.amountToCraft.drawTextBox();
	}

	protected String getBackground()
	{
		return "guis/craftAmt.png";
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		this.fontRendererObj.drawString( GuiText.SelectAmount.getLocal(), 8, 6, 4210752 );
	}
}
