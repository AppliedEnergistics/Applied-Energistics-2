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

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerQuartzKnife;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.items.contents.QuartzKnifeObj;

public class GuiQuartzKnife extends AEBaseGui
{

	GuiTextField name;

	public GuiQuartzKnife(InventoryPlayer inventoryPlayer, QuartzKnifeObj te) {
		super( new ContainerQuartzKnife( inventoryPlayer, te ) );
		this.ySize = 184;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		name = new GuiTextField( fontRendererObj, this.guiLeft + 24, this.guiTop + 32, 79, fontRendererObj.FONT_HEIGHT );
		name.setEnableBackgroundDrawing( false );
		name.setMaxStringLength( 32 );
		name.setTextColor( 0xFFFFFF );
		name.setVisible( true );
		name.setFocused( true );
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/quartzknife.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
		name.drawTextBox();
	}

	@Override
	protected void keyTyped(char character, int key)
	{
		if ( name.textboxKeyTyped( character, key ) )
		{
			try
			{
				String Out = name.getText();
				((ContainerQuartzKnife) inventorySlots).setName( Out );
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "QuartzKnife.Name", Out ) );
			}
			catch (IOException e)
			{
				AELog.error( e );
			}
		}
		else
		{
			super.keyTyped( character, key );
		}
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( getGuiDisplayName( GuiText.QuartzCuttingKnife.getLocal() ), 8, 6, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );
	}

}
