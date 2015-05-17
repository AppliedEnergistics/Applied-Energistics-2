/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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


import java.io.IOException;

import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.config.FuzzyMode;
import appeng.api.config.LevelType;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.container.implementations.ContainerLevelEmitter;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.parts.automation.PartLevelEmitter;


public final class GuiLevelEmitter extends GuiUpgradeable
{

	GuiNumberBox level;

	GuiButton plus1;
	GuiButton plus10;
	GuiButton plus100;
	GuiButton plus1000;
	GuiButton minus1;
	GuiButton minus10;
	GuiButton minus100;
	GuiButton minus1000;

	GuiImgButton levelMode;
	GuiImgButton craftingMode;

	public GuiLevelEmitter( InventoryPlayer inventoryPlayer, PartLevelEmitter te )
	{
		super( new ContainerLevelEmitter( inventoryPlayer, te ) );
	}

	@Override
	public final void initGui()
	{
		super.initGui();

		this.level = new GuiNumberBox( this.fontRendererObj, this.guiLeft + 24, this.guiTop + 43, 79, this.fontRendererObj.FONT_HEIGHT, Long.class );
		this.level.setEnableBackgroundDrawing( false );
		this.level.setMaxStringLength( 16 );
		this.level.setTextColor( 0xFFFFFF );
		this.level.setVisible( true );
		this.level.setFocused( true );
		( (ContainerLevelEmitter) this.inventorySlots ).setTextField( this.level );
	}

	@Override
	protected void addButtons()
	{
		this.levelMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 8, Settings.LEVEL_TYPE, LevelType.ITEM_LEVEL );
		this.redstoneMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 28, Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL );
		this.fuzzyMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 48, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		this.craftingMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 48, Settings.CRAFT_VIA_REDSTONE, YesNo.NO );

		int a = AEConfig.instance.levelByStackAmounts( 0 );
		int b = AEConfig.instance.levelByStackAmounts( 1 );
		int c = AEConfig.instance.levelByStackAmounts( 2 );
		int d = AEConfig.instance.levelByStackAmounts( 3 );

		this.buttonList.add( this.plus1 = new GuiButton( 0, this.guiLeft + 20, this.guiTop + 17, 22, 20, "+" + a ) );
		this.buttonList.add( this.plus10 = new GuiButton( 0, this.guiLeft + 48, this.guiTop + 17, 28, 20, "+" + b ) );
		this.buttonList.add( this.plus100 = new GuiButton( 0, this.guiLeft + 82, this.guiTop + 17, 32, 20, "+" + c ) );
		this.buttonList.add( this.plus1000 = new GuiButton( 0, this.guiLeft + 120, this.guiTop + 17, 38, 20, "+" + d ) );

		this.buttonList.add( this.minus1 = new GuiButton( 0, this.guiLeft + 20, this.guiTop + 59, 22, 20, "-" + a ) );
		this.buttonList.add( this.minus10 = new GuiButton( 0, this.guiLeft + 48, this.guiTop + 59, 28, 20, "-" + b ) );
		this.buttonList.add( this.minus100 = new GuiButton( 0, this.guiLeft + 82, this.guiTop + 59, 32, 20, "-" + c ) );
		this.buttonList.add( this.minus1000 = new GuiButton( 0, this.guiLeft + 120, this.guiTop + 59, 38, 20, "-" + d ) );

		this.buttonList.add( this.levelMode );
		this.buttonList.add( this.redstoneMode );
		this.buttonList.add( this.fuzzyMode );
		this.buttonList.add( this.craftingMode );
	}

	@Override
	public void drawFG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		boolean notCraftingMode = this.bc.getInstalledUpgrades( Upgrades.CRAFTING ) == 0;

		// configure enabled status...
		this.level.setEnabled( notCraftingMode );
		this.plus1.enabled = notCraftingMode;
		this.plus10.enabled = notCraftingMode;
		this.plus100.enabled = notCraftingMode;
		this.plus1000.enabled = notCraftingMode;
		this.minus1.enabled = notCraftingMode;
		this.minus10.enabled = notCraftingMode;
		this.minus100.enabled = notCraftingMode;
		this.minus1000.enabled = notCraftingMode;
		this.levelMode.enabled = notCraftingMode;
		this.redstoneMode.enabled = notCraftingMode;

		super.drawFG( offsetX, offsetY, mouseX, mouseY );

		if( this.craftingMode != null )
		{
			this.craftingMode.set( ( (ContainerLevelEmitter) this.cvb ).cmType );
		}

		if( this.levelMode != null )
		{
			this.levelMode.set( ( (ContainerLevelEmitter) this.cvb ).lvType );
		}
	}

	@Override
	public void drawBG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		super.drawBG( offsetX, offsetY, mouseX, mouseY );
		this.level.drawTextBox();
	}

	@Override
	protected void handleButtonVisibility()
	{
		this.craftingMode.setVisibility( this.bc.getInstalledUpgrades( Upgrades.CRAFTING ) > 0 );
		this.fuzzyMode.setVisibility( this.bc.getInstalledUpgrades( Upgrades.FUZZY ) > 0 );
	}

	@Override
	protected String getBackground()
	{
		return "guis/lvlemitter.png";
	}

	@Override
	protected GuiText getName()
	{
		return GuiText.LevelEmitter;
	}

	@Override
	protected final void actionPerformed( GuiButton btn )
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		if( btn == this.craftingMode )
		{
			NetworkHandler.instance.sendToServer( new PacketConfigButton( this.craftingMode.getSetting(), backwards ) );
		}

		if( btn == this.levelMode )
		{
			NetworkHandler.instance.sendToServer( new PacketConfigButton( this.levelMode.getSetting(), backwards ) );
		}

		boolean isPlus = btn == this.plus1 || btn == this.plus10 || btn == this.plus100 || btn == this.plus1000;
		boolean isMinus = btn == this.minus1 || btn == this.minus10 || btn == this.minus100 || btn == this.minus1000;

		if( isPlus || isMinus )
		{
			this.addQty( this.getQty( btn ) );
		}
	}

	private void addQty( long i )
	{
		try
		{
			String Out = this.level.getText();

			boolean Fixed = false;
			while( Out.startsWith( "0" ) && Out.length() > 1 )
			{
				Out = Out.substring( 1 );
				Fixed = true;
			}

			if( Fixed )
			{
				this.level.setText( Out );
			}

			if( Out.length() == 0 )
			{
				Out = "0";
			}

			long result = Long.parseLong( Out );
			result += i;
			if( result < 0 )
			{
				result = 0;
			}

			this.level.setText( Out = Long.toString( result ) );

			NetworkHandler.instance.sendToServer( new PacketValueConfig( "LevelEmitter.Value", Out ) );
		}
		catch( NumberFormatException e )
		{
			// nope..
			this.level.setText( "0" );
		}
		catch( IOException e )
		{
			AELog.error( e );
		}
	}

	@Override
	protected final void keyTyped( char character, int key )
	{
		if( !this.checkHotbarKeys( key ) )
		{
			if( ( key == 211 || key == 205 || key == 203 || key == 14 || Character.isDigit( character ) ) && this.level.textboxKeyTyped( character, key ) )
			{
				try
				{
					String Out = this.level.getText();

					boolean Fixed = false;
					while( Out.startsWith( "0" ) && Out.length() > 1 )
					{
						Out = Out.substring( 1 );
						Fixed = true;
					}

					if( Fixed )
					{
						this.level.setText( Out );
					}

					if( Out.length() == 0 )
					{
						Out = "0";
					}

					NetworkHandler.instance.sendToServer( new PacketValueConfig( "LevelEmitter.Value", Out ) );
				}
				catch( IOException e )
				{
					AELog.error( e );
				}
			}
			else
			{
				super.keyTyped( character, key );
			}
		}
	}
}
