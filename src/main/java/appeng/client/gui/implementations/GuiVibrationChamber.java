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


import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiProgressBar;
import appeng.client.gui.widgets.GuiProgressBar.Direction;
import appeng.container.implementations.ContainerVibrationChamber;
import appeng.core.localization.GuiText;
import appeng.tile.misc.TileVibrationChamber;


public class GuiVibrationChamber extends AEBaseGui
{

	final ContainerVibrationChamber cvc;
	GuiProgressBar pb;

	public GuiVibrationChamber( InventoryPlayer inventoryPlayer, TileVibrationChamber te )
	{
		super( new ContainerVibrationChamber( inventoryPlayer, te ) );
		this.cvc = (ContainerVibrationChamber) this.inventorySlots;
		this.ySize = 166;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		this.pb = new GuiProgressBar( this.cvc, "guis/vibchamber.png", 99, 36, 176, 14, 6, 18, Direction.VERTICAL );
		this.buttonList.add( this.pb );
	}

	@Override
	public void drawFG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		this.fontRendererObj.drawString( this.getGuiDisplayName( GuiText.VibrationChamber.getLocal() ), 8, 6, 4210752 );
		this.fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );

		int k = 25;
		int l = -15;

		this.pb.setFullMsg( this.cvc.aePerTick * this.cvc.getCurrentProgress() / 100 + " ae/t" );

		if( this.cvc.getCurrentProgress() > 0 )
		{
			int i1 = this.cvc.getCurrentProgress();
			this.bindTexture( "guis/vibchamber.png" );
			GL11.glColor3f( 1, 1, 1 );
			this.drawTexturedModalRect( k + 56, l + 36 + 12 - i1, 176, 12 - i1, 14, i1 + 2 );
		}
	}

	@Override
	public void drawBG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		this.bindTexture( "guis/vibchamber.png" );
		this.pb.xPosition = 99 + this.guiLeft;
		this.pb.yPosition = 36 + this.guiTop;
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize );
	}
}
