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


import appeng.api.config.CondenserOutput;
import appeng.client.gui.widgets.GuiServerSettingToggleButton;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import net.minecraft.entity.player.PlayerInventory;

import appeng.api.config.Settings;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiSettingToggleButton;
import appeng.client.gui.widgets.GuiProgressBar;
import appeng.client.gui.widgets.GuiProgressBar.Direction;
import appeng.container.implementations.ContainerCondenser;
import appeng.core.localization.GuiText;


public class GuiCondenser extends AEBaseGui<ContainerCondenser>
{

	private GuiSettingToggleButton<CondenserOutput> mode;

	public GuiCondenser(ContainerCondenser container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		this.ySize = 197;
	}

	@Override
	public void init()
	{
		super.init();

		this.mode = new GuiServerSettingToggleButton<>( 128 + this.guiLeft, 52 + this.guiTop, Settings.CONDENSER_OUTPUT, this.container.getOutput());

		this.addButton(new GuiProgressBar(this.container, "guis/condenser.png", 120 + this.guiLeft, 25 + this.guiTop, 178, 25, 6, 18, Direction.VERTICAL, GuiText.StoredEnergy
				.getLocal()));
		this.addButton( this.mode );
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.font.drawString( this.getGuiDisplayName( GuiText.Condenser.getLocal() ), 8, 6, 4210752 );
		this.font.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );

		this.mode.set( this.container.getOutput() );
		this.mode.setFillVar( String.valueOf( this.container.getOutput().requiredPower ) );
	}

	@Override
	public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks)
	{
		this.bindTexture( "guis/condenser.png" );

		GuiUtils.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize, 0 /* FIXME this.zlevel was used */ );
	}
}
