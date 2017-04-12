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


import appeng.api.config.Settings;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerSpatialIOPort;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.tile.spatial.TileSpatialIOPort;
import appeng.util.Platform;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.input.Mouse;


public class GuiSpatialIOPort extends AEBaseGui
{

	private final ContainerSpatialIOPort container;
	private GuiImgButton units;

	public GuiSpatialIOPort( final InventoryPlayer inventoryPlayer, final TileSpatialIOPort te )
	{
		super( new ContainerSpatialIOPort( inventoryPlayer, te ) );
		this.ySize = 199;
		this.container = (ContainerSpatialIOPort) this.inventorySlots;
	}

	@Override
	protected void actionPerformed( final GuiButton btn )
	{
		super.actionPerformed( btn );

		final boolean backwards = Mouse.isButtonDown( 1 );

		if( btn == this.units )
		{
			AEConfig.instance.nextPowerUnit( backwards );
			this.units.set( AEConfig.instance.selectedPowerUnit() );
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		this.units = new GuiImgButton( this.guiLeft - 18, this.guiTop + 8, Settings.POWER_UNITS, AEConfig.instance.selectedPowerUnit() );
		this.buttonList.add( this.units );
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.fontRendererObj.drawString( GuiText.StoredPower.getLocal() + ": " + Platform.formatPowerLong( this.container.getCurrentPower(), false ), 13, 21, 4210752 );
		this.fontRendererObj.drawString( GuiText.MaxPower.getLocal() + ": " + Platform.formatPowerLong( this.container.getMaxPower(), false ), 13, 31, 4210752 );
		this.fontRendererObj.drawString( GuiText.RequiredPower.getLocal() + ": " + Platform.formatPowerLong( this.container.getRequiredPower(), false ), 13, 78, 4210752 );
		this.fontRendererObj.drawString( GuiText.Efficiency.getLocal() + ": " + ( ( (float) this.container.getEfficency() ) / 100 ) + '%', 13, 88, 4210752 );

		this.fontRendererObj.drawString( this.getGuiDisplayName( GuiText.SpatialIOPort.getLocal() ), 8, 6, 4210752 );
		this.fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96, 4210752 );
	}

	@Override
	public void drawBG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.bindTexture( "guis/spatialio.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize );
	}
}
