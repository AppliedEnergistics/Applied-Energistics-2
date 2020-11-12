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

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import appeng.api.config.ActionItems;
import appeng.api.config.ItemSubstitution;
import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.slot.AppEngSlot;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;


public class GuiPatternTerm extends GuiMEMonitorable
{

	private static final String BACKGROUND_CRAFTING_MODE = "guis/pattern.png";
	private static final String BACKGROUND_PROCESSING_MODE = "guis/pattern2.png";

	private static final String SUBSITUTION_DISABLE = "0";
	private static final String SUBSITUTION_ENABLE = "1";

	private static final String CRAFTMODE_CRFTING = "1";
	private static final String CRAFTMODE_PROCESSING = "0";

	private final ContainerPatternTerm container;

	private GuiTabButton tabCraftButton;
	private GuiTabButton tabProcessButton;
	private GuiImgButton substitutionsEnabledBtn;
	private GuiImgButton substitutionsDisabledBtn;
	private GuiImgButton encodeBtn;
	private GuiImgButton clearBtn;
	private GuiImgButton x2Btn;
	private GuiImgButton x3Btn;
	private GuiImgButton plusOneBtn;
	private GuiImgButton divTwoBtn;
	private GuiImgButton divThreeBtn;
	private GuiImgButton minusOneBtn;
	private GuiImgButton maxCountBtn;

	public GuiPatternTerm( final InventoryPlayer inventoryPlayer, final ITerminalHost te )
	{
		super( inventoryPlayer, te, new ContainerPatternTerm( inventoryPlayer, te ) );
		this.container = (ContainerPatternTerm) this.inventorySlots;
		this.setReservedSpace( 81 );
	}

	@Override
	protected void actionPerformed( final GuiButton btn )
	{
		super.actionPerformed( btn );

		try
		{

			if( this.tabCraftButton == btn || this.tabProcessButton == btn )
			{
				NetworkHandler.instance()
						.sendToServer(
								new PacketValueConfig( "PatternTerminal.CraftMode", this.tabProcessButton == btn ? CRAFTMODE_CRFTING : CRAFTMODE_PROCESSING ) );
			}

			if( this.encodeBtn == btn )
			{
				NetworkHandler.instance().sendToServer( new PacketValueConfig( "PatternTerminal.Encode", "1" ) );
			}

			if( this.clearBtn == btn )
			{
				NetworkHandler.instance().sendToServer( new PacketValueConfig( "PatternTerminal.Clear", "1" ) );
			}

			if( this.substitutionsEnabledBtn == btn || this.substitutionsDisabledBtn == btn )
			{
				NetworkHandler.instance()
						.sendToServer(
								new PacketValueConfig( "PatternTerminal.Substitute", this.substitutionsEnabledBtn == btn ? SUBSITUTION_DISABLE : SUBSITUTION_ENABLE ) );
			}
		}
		catch( final IOException e )
		{
			AELog.error( e );
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		this.tabCraftButton = new GuiTabButton( this.guiLeft + 173, this.guiTop + this.ySize - 177, new ItemStack( Blocks.CRAFTING_TABLE ), GuiText.CraftingPattern
				.getLocal(), this.itemRender );
		this.buttonList.add( this.tabCraftButton );

		this.tabProcessButton = new GuiTabButton( this.guiLeft + 173, this.guiTop + this.ySize - 177, new ItemStack( Blocks.FURNACE ), GuiText.ProcessingPattern
				.getLocal(), this.itemRender );
		this.buttonList.add( this.tabProcessButton );

		this.substitutionsEnabledBtn = new GuiImgButton( this.guiLeft + 84, this.guiTop + this.ySize - 163, Settings.ACTIONS, ItemSubstitution.ENABLED );
		this.substitutionsEnabledBtn.setHalfSize( true );
		this.buttonList.add( this.substitutionsEnabledBtn );

		this.substitutionsDisabledBtn = new GuiImgButton( this.guiLeft + 84, this.guiTop + this.ySize - 163, Settings.ACTIONS, ItemSubstitution.DISABLED );
		this.substitutionsDisabledBtn.setHalfSize( true );
		this.buttonList.add( this.substitutionsDisabledBtn );

		this.clearBtn = new GuiImgButton( this.guiLeft + 74, this.guiTop + this.ySize - 163, Settings.ACTIONS, ActionItems.CLOSE );
		this.clearBtn.setHalfSize( true );
		this.buttonList.add( this.clearBtn );

		this.x2Btn = new GuiImgButton( this.guiLeft + 106, this.guiTop + this.ySize - 152, Settings.ACTIONS, ActionItems.MULTIPLY_BY_TWO );
		this.x2Btn.setHalfSize( true );
		this.buttonList.add( this.x2Btn );

		this.clearBtn = new GuiImgButton( this.guiLeft + 74, this.guiTop + this.ySize - 163, Settings.ACTIONS, ActionItems.CLOSE );
		this.clearBtn.setHalfSize( true );
		this.buttonList.add( this.clearBtn );

		this.clearBtn = new GuiImgButton( this.guiLeft + 74, this.guiTop + this.ySize - 163, Settings.ACTIONS, ActionItems.CLOSE );
		this.clearBtn.setHalfSize( true );
		this.buttonList.add( this.clearBtn );

		this.clearBtn = new GuiImgButton( this.guiLeft + 74, this.guiTop + this.ySize - 163, Settings.ACTIONS, ActionItems.CLOSE );
		this.clearBtn.setHalfSize( true );
		this.buttonList.add( this.clearBtn );

		this.clearBtn = new GuiImgButton( this.guiLeft + 74, this.guiTop + this.ySize - 163, Settings.ACTIONS, ActionItems.CLOSE );
		this.clearBtn.setHalfSize( true );
		this.buttonList.add( this.clearBtn );

		this.clearBtn = new GuiImgButton( this.guiLeft + 74, this.guiTop + this.ySize - 163, Settings.ACTIONS, ActionItems.CLOSE );
		this.clearBtn.setHalfSize( true );
		this.buttonList.add( this.clearBtn );

		this.clearBtn = new GuiImgButton( this.guiLeft + 74, this.guiTop + this.ySize - 163, Settings.ACTIONS, ActionItems.CLOSE );
		this.clearBtn.setHalfSize( true );
		this.buttonList.add( this.clearBtn );

		this.encodeBtn = new GuiImgButton( this.guiLeft + 147, this.guiTop + this.ySize - 142, Settings.ACTIONS, ActionItems.ENCODE );
		this.buttonList.add( this.encodeBtn );
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		if( this.container.isCraftingMode() )
		{
			this.tabCraftButton.visible = true;
			this.tabProcessButton.visible = false;

			if( this.container.substitute )
			{
				this.substitutionsEnabledBtn.visible = true;
				this.substitutionsDisabledBtn.visible = false;
			}
			else
			{
				this.substitutionsEnabledBtn.visible = false;
				this.substitutionsDisabledBtn.visible = true;
			}
		}
		else
		{
			this.tabCraftButton.visible = false;
			this.tabProcessButton.visible = true;
			this.substitutionsEnabledBtn.visible = false;
			this.substitutionsDisabledBtn.visible = false;
		}

		super.drawFG( offsetX, offsetY, mouseX, mouseY );
		this.fontRenderer.drawString( GuiText.PatternTerminal.getLocal(), 8, this.ySize - 96 + 2 - this.getReservedSpace(), 4210752 );
	}

	@Override
	protected String getBackground()
	{
		if( this.container.isCraftingMode() )
		{
			return BACKGROUND_CRAFTING_MODE;
		}

		return BACKGROUND_PROCESSING_MODE;
	}

	@Override
	protected void repositionSlot( final AppEngSlot s )
	{
		final int offsetPlayerSide = s.isPlayerSide() ? 5 : 3;

		s.yPos = s.getY() + this.ySize - 78 - offsetPlayerSide;
	}
}
