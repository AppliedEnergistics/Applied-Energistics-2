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


import java.io.IOException;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.ITextComponent;

import net.minecraft.entity.player.PlayerInventory;

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


public class GuiLevelEmitter extends GuiUpgradeable<ContainerLevelEmitter>
{

	private GuiNumberBox level;

	private Button plus1;
	private Button plus10;
	private Button plus100;
	private Button plus1000;
	private Button minus1;
	private Button minus10;
	private Button minus100;
	private Button minus1000;

	private GuiImgButton levelMode;
	private GuiImgButton craftingMode;

	public GuiLevelEmitter(ContainerLevelEmitter container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
	}

	@Override
	public void init()
	{
		super.init();

		this.level = new GuiNumberBox( this.font, this.guiLeft + 24, this.guiTop + 43, 79, this.font.FONT_HEIGHT, Long.class );
		this.level.setEnableBackgroundDrawing( false );
		this.level.setMaxStringLength( 16 );
		this.level.setTextColor( 0xFFFFFF );
		this.level.setVisible( true );
		this.level.setFocused2( true );
		container.setTextField( this.level );
	}

	@Override
	protected void addButtons()
	{
		this.levelMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 8, Settings.LEVEL_TYPE, LevelType.ITEM_LEVEL, btn -> selectNextLevelMode() );
		this.redstoneMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 28, Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL, this::actionPerformed );
		this.fuzzyMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 48, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL, this::actionPerformed );
		this.craftingMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 48, Settings.CRAFT_VIA_REDSTONE, YesNo.NO, btn -> selectNextCraftingMode() );

		final int a = AEConfig.instance().levelByStackAmounts( 0 );
		final int b = AEConfig.instance().levelByStackAmounts( 1 );
		final int c = AEConfig.instance().levelByStackAmounts( 2 );
		final int d = AEConfig.instance().levelByStackAmounts( 3 );

		this.addButton( this.plus1 = new Button(this.guiLeft + 20, this.guiTop + 17, 22, 20, "+" + a, btn -> addQty(a) ) );
		this.addButton( this.plus10 = new Button(this.guiLeft + 48, this.guiTop + 17, 28, 20, "+" + b, btn -> addQty(b) ) );
		this.addButton( this.plus100 = new Button(this.guiLeft + 82, this.guiTop + 17, 32, 20, "+" + c, btn -> addQty(c) ) );
		this.addButton( this.plus1000 = new Button(this.guiLeft + 120, this.guiTop + 17, 38, 20, "+" + d, btn -> addQty(d) ) );

		this.addButton( this.minus1 = new Button(this.guiLeft + 20, this.guiTop + 59, 22, 20, "-" + a, btn -> addQty(-a) ) );
		this.addButton( this.minus10 = new Button(this.guiLeft + 48, this.guiTop + 59, 28, 20, "-" + b, btn -> addQty(-b) ) );
		this.addButton( this.minus100 = new Button(this.guiLeft + 82, this.guiTop + 59, 32, 20, "-" + c, btn -> addQty(-c) ) );
		this.addButton( this.minus1000 = new Button(this.guiLeft + 120, this.guiTop + 59, 38, 20, "-" + d, btn -> addQty(-d) ) );

		this.addButton( this.levelMode );
		this.addButton( this.redstoneMode );
		this.addButton( this.fuzzyMode );
		this.addButton( this.craftingMode );
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		final boolean notCraftingMode = this.bc.getInstalledUpgrades( Upgrades.CRAFTING ) == 0;

		// configure enabled status...
		this.level.setEnabled( notCraftingMode );
		this.plus1.active = notCraftingMode;
		this.plus10.active = notCraftingMode;
		this.plus100.active = notCraftingMode;
		this.plus1000.active = notCraftingMode;
		this.minus1.active = notCraftingMode;
		this.minus10.active = notCraftingMode;
		this.minus100.active = notCraftingMode;
		this.minus1000.active = notCraftingMode;
		this.levelMode.active = notCraftingMode;
		this.redstoneMode.active = notCraftingMode;

		super.drawFG( offsetX, offsetY, mouseX, mouseY );

		if( this.craftingMode != null )
		{
			this.craftingMode.set( this.cvb.getCraftingMode() );
		}

		if( this.levelMode != null )
		{
			this.levelMode.set( ( (ContainerLevelEmitter) this.cvb ).getLevelMode() );
		}
	}

	@Override
	public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks)
	{
		super.drawBG( offsetX, offsetY, mouseX, mouseY, partialTicks);
		this.level.render(mouseX, mouseY, partialTicks);
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

	private void selectNextCraftingMode() {
		final boolean backwards = minecraft.mouseHelper.isRightDown();
		NetworkHandler.instance().sendToServer( new PacketConfigButton( this.craftingMode.getSetting(), backwards ) );
	}

	private void selectNextLevelMode() {
		final boolean backwards = minecraft.mouseHelper.isRightDown();
		NetworkHandler.instance().sendToServer( new PacketConfigButton( this.levelMode.getSetting(), backwards ) );
	}

	private void addQty( final long i )
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

			if( Out.isEmpty() )
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

			NetworkHandler.instance().sendToServer( new PacketValueConfig( "LevelEmitter.Value", Out ) );
		}
		catch( final NumberFormatException e )
		{
			// nope..
			this.level.setText( "0" );
		}
	}

	@Override
	public boolean charTyped(char character, int key) {
		// Forward entered characters to the number-text-field
		return level.charTyped(character, key);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_)
	{
		if( !this.checkHotbarKeys( InputMappings.getInputByCode(keyCode, scanCode) ) )
		{
			if( keyCode == 211 || keyCode == 205 || keyCode == 203 || keyCode == 14 )
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

				if( Out.isEmpty() )
				{
					Out = "0";
				}

				NetworkHandler.instance().sendToServer( new PacketValueConfig( "LevelEmitter.Value", Out ) );
				return true;
			}
		}

		return super.keyPressed( keyCode, scanCode, p_keyPressed_3_ );
	}
}
