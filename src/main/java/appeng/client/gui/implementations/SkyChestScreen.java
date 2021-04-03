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

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import appeng.client.gui.AEBaseScreen;
import appeng.container.implementations.SkyChestContainer;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;

public class SkyChestScreen extends AEBaseScreen<SkyChestContainer> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(AppEng.MOD_ID, "textures/guis/skychest.png");

    public SkyChestScreen(SkyChestContainer container, PlayerInventory playerInv, ITextComponent title) {
        super(container, playerInv, title);
        this.ySize = 195;
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        this.font.drawString(matrixStack, this.getGuiDisplayName(GuiText.SkyChest.text()).getString(), 8, 8, COLOR_DARK_GRAY);
        this.font.drawString(matrixStack, GuiText.inventory.getLocal(), 8, this.ySize - 96 + 2, COLOR_DARK_GRAY);
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY,
            float partialTicks) {
        bindTexture(TEXTURE);
        blit(matrices, offsetX, offsetY, 0, 0, this.xSize, this.ySize);
    }

}
