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
import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.slot.AppEngSlot;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;

public class GuiPatternTerm extends GuiMEMonitorable
{

	final ContainerPatternTerm container;

	GuiTabButton tabCraftButton;
	GuiTabButton tabProcessButton;
	// GuiImgButton substitutionsBtn;
	GuiImgButton encodeBtn;
	GuiImgButton clearBtn;

	@Override
	public void initGui()
	{
		super.initGui();
		buttonList.add( tabCraftButton = new GuiTabButton( this.guiLeft + 173, this.guiTop + this.ySize - 177, new ItemStack( Blocks.crafting_table ),
				GuiText.CraftingPattern.getLocal(), itemRender ) );
		buttonList.add( tabProcessButton = new GuiTabButton( this.guiLeft + 173, this.guiTop + this.ySize - 177, new ItemStack( Blocks.furnace ),
				GuiText.ProcessingPattern.getLocal(), itemRender ) );

		// buttonList.add( substitutionsBtn = new GuiImgButton( this.guiLeft + 84, this.guiTop + this.ySize - 163,
		// Settings.ACTIONS, ActionItems.SUBSTITUTION ) );
		// substitutionsBtn.halfSize = true;

		buttonList.add( clearBtn = new GuiImgButton( this.guiLeft + 74, this.guiTop + this.ySize - 163, Settings.ACTIONS, ActionItems.CLOSE ) );
		clearBtn.halfSize = true;

		buttonList.add( encodeBtn = new GuiImgButton( this.guiLeft + 147, this.guiTop + this.ySize - 142, Settings.ACTIONS, ActionItems.ENCODE ) );
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		try
		{

			if ( tabCraftButton == btn || tabProcessButton == btn )
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "PatternTerminal.CraftMode", tabProcessButton == btn ? "1" : "0" ) );
			}

			if ( encodeBtn == btn )
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "PatternTerminal.Encode", "1" ) );
			}

			if ( clearBtn == btn )
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "PatternTerminal.Clear", "1" ) );
			}

		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// if ( substitutionsBtn == btn )
		// {

		// }
	}

	@Override
	protected void repositionSlot(AppEngSlot s)
	{
		if ( s.isPlayerSide() )
			s.yDisplayPosition = s.defY + ySize - 78 - 5;
		else
			s.yDisplayPosition = s.defY + ySize - 78 - 3;
	}

	public GuiPatternTerm(InventoryPlayer inventoryPlayer, ITerminalHost te) {
		super( inventoryPlayer, te, new ContainerPatternTerm( inventoryPlayer, te ) );
		container = (ContainerPatternTerm) this.inventorySlots;
		reservedSpace = 81;
	}

	@Override
	protected String getBackground()
	{
		if ( container.craftingMode )
			return "guis/pattern.png";
		return "guis/pattern2.png";
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		if ( !container.craftingMode )
		{
			tabCraftButton.visible = false;
			tabProcessButton.visible = true;
		}
		else
		{
			tabCraftButton.visible = true;
			tabProcessButton.visible = false;
		}

		super.drawFG( offsetX, offsetY, mouseX, mouseY );
		fontRendererObj.drawString( GuiText.PatternTerminal.getLocal(), 8, ySize - 96 + 2 - reservedSpace, 4210752 );
	}

}
