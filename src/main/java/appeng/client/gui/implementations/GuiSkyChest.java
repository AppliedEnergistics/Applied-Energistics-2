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


import net.minecraft.entity.player.InventoryPlayer;

import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerSkyChest;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.integration.IntegrationType;
import appeng.tile.storage.TileSkyChest;


public class GuiSkyChest extends AEBaseGui
{

	public GuiSkyChest( InventoryPlayer inventoryPlayer, TileSkyChest te )
	{
		super( new ContainerSkyChest( inventoryPlayer, te ) );
		this.ySize = 195;
	}

	@Override
	public void drawFG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		this.fontRendererObj.drawString( this.getGuiDisplayName( GuiText.SkyChest.getLocal() ), 8, 8, 4210752 );
		this.fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 2, 4210752 );
	}

	@Override
	public void drawBG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		this.bindTexture( "guis/skychest.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize );
	}

	@Override
	protected boolean enableSpaceClicking()
	{
		return !AppEng.instance.isIntegrationEnabled( IntegrationType.InvTweaks );
	}
}
