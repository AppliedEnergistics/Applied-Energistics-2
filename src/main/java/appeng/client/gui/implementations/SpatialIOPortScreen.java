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

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.CommonButtons;
import appeng.container.implementations.SpatialIOPortContainer;
import appeng.core.localization.GuiText;
import appeng.util.Platform;

public class SpatialIOPortScreen extends AEBaseScreen<SpatialIOPortContainer> {

    public SpatialIOPortScreen(SpatialIOPortContainer container, PlayerInventory playerInventory,
            ITextComponent title) {
        super(container, playerInventory, title);
        this.imageHeight = 199;
    }

    @Override
    public void init() {
        super.init();
        this.addButton(CommonButtons.togglePowerUnit(this.leftPos - 18, this.topPos + 8));
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        this.font.draw(matrixStack, GuiText.StoredPower.getLocal() + ": "
                + Platform.formatPowerLong(this.menu.getCurrentPower(), false), 13, 21, 4210752);
        this.font.draw(matrixStack,
                GuiText.MaxPower.getLocal() + ": " + Platform.formatPowerLong(this.menu.getMaxPower(), false), 13,
                31, 4210752);
        this.font.draw(matrixStack, GuiText.RequiredPower.getLocal() + ": "
                + Platform.formatPowerLong(this.menu.getRequiredPower(), false), 13, 73, 4210752);
        this.font.draw(matrixStack,
                GuiText.Efficiency.getLocal() + ": " + (((float) this.menu.getEfficency()) / 100) + '%', 13, 83,
                4210752);

        this.font.draw(matrixStack, this.getGuiDisplayName(GuiText.SpatialIOPort.text()).getString(), 8, 6,
                4210752);
        this.font.draw(matrixStack, GuiText.inventory.getLocal(), 8, this.imageHeight - 96, 4210752);

        if (this.menu.xSize != 0 && this.menu.ySize != 0 && this.menu.zSize != 0) {
            final String text = GuiText.SCSSize.getLocal() + ": " + this.menu.xSize + "x" + this.menu.ySize
                    + "x" + this.menu.zSize;
            this.font.draw(matrixStack, text, 13, 93, 4210752);
        } else {
            this.font.draw(matrixStack, GuiText.SCSSize.getLocal() + ": " + GuiText.SCSInvalid.getLocal(), 13, 93,
                    4210752);
        }

    }

    @Override
    public void drawBG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        this.bindTexture("guis/spatialio.png");
        GuiUtils.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.imageWidth, this.imageHeight, getBlitOffset());
    }
}
