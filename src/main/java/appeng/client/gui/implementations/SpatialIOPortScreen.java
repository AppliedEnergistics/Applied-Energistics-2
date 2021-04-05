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

import appeng.client.gui.Blitter;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.CommonButtons;
import appeng.container.implementations.SpatialIOPortContainer;
import appeng.core.localization.GuiText;
import appeng.util.Platform;

public class SpatialIOPortScreen extends AEBaseScreen<SpatialIOPortContainer> {

    private static final Blitter BACKGROUND = Blitter.texture("guis/spatialio.png").src(0, 0, 176, 197);

    public SpatialIOPortScreen(SpatialIOPortContainer container, PlayerInventory playerInventory,
            ITextComponent title) {
        super(container, playerInventory, title, BACKGROUND);
    }

    @Override
    public void init() {
        super.init();
        this.addToLeftToolbar(CommonButtons.togglePowerUnit(this.guiLeft - 18, this.guiTop + 8));
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        this.font.drawString(matrixStack, GuiText.StoredPower.getLocal() + ": "
                + Platform.formatPowerLong(this.container.getCurrentPower(), false), 13, 21, COLOR_DARK_GRAY);
        this.font.drawString(matrixStack,
                GuiText.MaxPower.getLocal() + ": " + Platform.formatPowerLong(this.container.getMaxPower(), false), 13,
                31, COLOR_DARK_GRAY);
        this.font.drawString(matrixStack, GuiText.RequiredPower.getLocal() + ": "
                + Platform.formatPowerLong(this.container.getRequiredPower(), false), 13, 73, COLOR_DARK_GRAY);
        this.font.drawString(matrixStack,
                GuiText.Efficiency.getLocal() + ": " + (((float) this.container.getEfficency()) / 100) + '%', 13, 83,
                COLOR_DARK_GRAY);

        this.font.drawString(matrixStack, this.getGuiDisplayName(GuiText.SpatialIOPort.text()).getString(), 8, 6,
                COLOR_DARK_GRAY);
        this.font.drawString(matrixStack, GuiText.inventory.getLocal(), 8, this.ySize - 96, COLOR_DARK_GRAY);

        if (this.container.xSize != 0 && this.container.ySize != 0 && this.container.zSize != 0) {
            final String text = GuiText.SCSSize.getLocal() + ": " + this.container.xSize + "x" + this.container.ySize
                    + "x" + this.container.zSize;
            this.font.drawString(matrixStack, text, 13, 93, COLOR_DARK_GRAY);
        } else {
            this.font.drawString(matrixStack, GuiText.SCSSize.getLocal() + ": " + GuiText.SCSInvalid.getLocal(), 13, 93,
                    COLOR_DARK_GRAY);
        }

    }

}
