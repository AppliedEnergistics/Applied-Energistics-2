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


import appeng.core.AppEng;
import net.minecraft.entity.player.PlayerInventory;

import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerSkyChest;
import appeng.core.localization.GuiText;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;


public class GuiSkyChest extends AEBaseGui<ContainerSkyChest>
{

	private static final ResourceLocation TEXTURE = new ResourceLocation(AppEng.MOD_ID, "textures/guis/skychest.png");

	public GuiSkyChest( ContainerSkyChest container, PlayerInventory playerInv, ITextComponent title )
	{
		super( container, playerInv, title );
		this.ySize = 195;
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.font.drawString( this.getGuiDisplayName( GuiText.SkyChest.getLocal() ), 8, 8, 4210752 );
		this.font.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 2, 4210752 );
	}

	@Override
	public void drawBG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		bindTexture(TEXTURE);
		GuiUtils.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize, 0 );
	}

	@Override
	protected boolean enableSpaceClicking()
	{
		// FIXME return !Integrations.invTweaks().isEnabled();
		return true;
	}
}
