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
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IParts;
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
import appeng.helpers.Reflected;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartTerminal;


public class GuiCraftAmount extends AEBaseGui
{
	private GuiNumberBox amountToCraft;
	private GuiTabButton originalGuiBtn;

	private GuiButton next;

	private GuiButton plus1;
	private GuiButton plus10;
	private GuiButton plus100;
	private GuiButton plus1000;
	private GuiButton minus1;
	private GuiButton minus10;
	private GuiButton minus100;
	private GuiButton minus1000;

	private GuiBridge originalGui;

	@Reflected
	public GuiCraftAmount( InventoryPlayer inventoryPlayer, ITerminalHost te )
	{
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
		Object target = ( (AEBaseContainer) this.inventorySlots ).getTarget();
		final IDefinitions definitions = AEApi.instance().definitions();
		final IParts parts = definitions.parts();

		if( target instanceof WirelessTerminalGuiObject )
		{
			for( ItemStack wirelessTerminalStack : definitions.items().wirelessTerminal().maybeStack( 1 ).asSet() )
			{
				myIcon = wirelessTerminalStack;
			}

			this.originalGui = GuiBridge.GUI_WIRELESS_TERM;
		}

		if( target instanceof PartTerminal )
		{
			for( ItemStack stack : parts.terminal().maybeStack( 1 ).asSet() )
			{
				myIcon = stack;
			}
			this.originalGui = GuiBridge.GUI_ME;
		}

		if( target instanceof PartCraftingTerminal )
		{
			for( ItemStack stack : parts.craftingTerminal().maybeStack( 1 ).asSet() )
			{
				myIcon = stack;
			}
			this.originalGui = GuiBridge.GUI_CRAFTING_TERMINAL;
		}

		if( target instanceof PartPatternTerminal )
		{
			for( ItemStack stack : parts.patternTerminal().maybeStack( 1 ).asSet() )
			{
				myIcon = stack;
			}
			this.originalGui = GuiBridge.GUI_PATTERN_TERMINAL;
		}

		if( this.originalGui != null && myIcon != null )
		{
			this.buttonList.add( this.originalGuiBtn = new GuiTabButton( this.guiLeft + 154, this.guiTop, myIcon, myIcon.getDisplayName(), itemRender ) );
		}

		this.amountToCraft = new GuiNumberBox( this.fontRendererObj, this.guiLeft + 62, this.guiTop + 57, 59, this.fontRendererObj.FONT_HEIGHT, Integer.class );
		this.amountToCraft.setEnableBackgroundDrawing( false );
		this.amountToCraft.setMaxStringLength( 16 );
		this.amountToCraft.setTextColor( 0xFFFFFF );
		this.amountToCraft.setVisible( true );
		this.amountToCraft.setFocused( true );
		this.amountToCraft.setText( "1" );
	}

	@Override
	public void drawFG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		this.fontRendererObj.drawString( GuiText.SelectAmount.getLocal(), 8, 6, 4210752 );
	}

	@Override
	public void drawBG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		this.next.displayString = isShiftKeyDown() ? GuiText.Start.getLocal() : GuiText.Next.getLocal();

		this.bindTexture( "guis/craftAmt.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize );

		try
		{
			Long.parseLong( this.amountToCraft.getText() );
			this.next.enabled = this.amountToCraft.getText().length() > 0;
		}
		catch( NumberFormatException e )
		{
			this.next.enabled = false;
		}

		this.amountToCraft.drawTextBox();
	}

	@Override
	protected void keyTyped( char character, int key )
	{
		if( !this.checkHotbarKeys( key ) )
		{
			if( key == 28 )
			{
				this.actionPerformed( this.next );
			}
			if( ( key == 211 || key == 205 || key == 203 || key == 14 || character == '-' || Character.isDigit( character ) ) && this.amountToCraft.textboxKeyTyped( character, key ) )
			{
				try
				{
					String out = this.amountToCraft.getText();

					boolean fixed = false;
					while( out.startsWith( "0" ) && out.length() > 1 )
					{
						out = out.substring( 1 );
						fixed = true;
					}

					if( fixed )
					{
						this.amountToCraft.setText( out );
					}

					if( out.isEmpty() )
					{
						out = "0";
					}

					long result = Long.parseLong( out );
					if( result < 0 )
					{
						this.amountToCraft.setText( "1" );
					}
				}
				catch( NumberFormatException e )
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
	protected void actionPerformed( GuiButton btn )
	{
		super.actionPerformed( btn );

		try
		{

			if( btn == this.originalGuiBtn )
			{
				NetworkHandler.instance.sendToServer( new PacketSwitchGuis( this.originalGui ) );
			}

			if( btn == this.next )
			{
				NetworkHandler.instance.sendToServer( new PacketCraftRequest( Integer.parseInt( this.amountToCraft.getText() ), isShiftKeyDown() ) );
			}
		}
		catch( NumberFormatException e )
		{
			// nope..
			this.amountToCraft.setText( "1" );
		}

		boolean isPlus = btn == this.plus1 || btn == this.plus10 || btn == this.plus100 || btn == this.plus1000;
		boolean isMinus = btn == this.minus1 || btn == this.minus10 || btn == this.minus100 || btn == this.minus1000;

		if( isPlus || isMinus )
		{
			this.addQty( this.getQty( btn ) );
		}
	}

	private void addQty( int i )
	{
		try
		{
			String out = this.amountToCraft.getText();

			boolean fixed = false;
			while( out.startsWith( "0" ) && out.length() > 1 )
			{
				out = out.substring( 1 );
				fixed = true;
			}

			if( fixed )
			{
				this.amountToCraft.setText( out );
			}

			if( out.isEmpty() )
			{
				out = "0";
			}

			long result = Integer.parseInt( out );

			if( result == 1 && i > 1 )
			{
				result = 0;
			}

			result += i;
			if( result < 1 )
			{
				result = 1;
			}

			out = Long.toString( result );
			Integer.parseInt( out );
			this.amountToCraft.setText( out );
		}
		catch( NumberFormatException e )
		{
			// :P
		}
	}

	protected String getBackground()
	{
		return "guis/craftAmt.png";
	}
}
