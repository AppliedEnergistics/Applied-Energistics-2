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
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;


public class GuiCraftingTerm extends GuiMEMonitorable
{

	GuiImgButton clearBtn;

	public GuiCraftingTerm( InventoryPlayer inventoryPlayer, ITerminalHost te )
	{
		super( inventoryPlayer, te, new ContainerCraftingTerm( inventoryPlayer, te ) );
		this.reservedSpace = 73;
	}

	@Override
	protected void actionPerformed( GuiButton btn )
	{
		super.actionPerformed( btn );

		if( this.clearBtn == btn )
		{
			Slot s = null;
			Container c = this.inventorySlots;
			for( Object j : c.inventorySlots )
			{
				if( j instanceof SlotCraftingMatrix )
				{
					s = (Slot) j;
				}
			}

			if( s != null )
			{
				PacketInventoryAction p;
				p = new PacketInventoryAction( InventoryAction.MOVE_REGION, s.slotNumber, 0 );
				NetworkHandler.instance.sendToServer( p );
			}
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.add( this.clearBtn = new GuiImgButton( this.guiLeft + 92, this.guiTop + this.ySize - 156, Settings.ACTIONS, ActionItems.STASH ) );
		this.clearBtn.halfSize = true;
	}

	@Override
	public void drawFG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		super.drawFG( offsetX, offsetY, mouseX, mouseY );
		this.fontRendererObj.drawString( GuiText.CraftingTerminal.getLocal(), 8, this.ySize - 96 + 1 - this.reservedSpace, 4210752 );
	}

	@Override
	protected String getBackground()
	{
		return "guis/crafting.png";
	}
}
