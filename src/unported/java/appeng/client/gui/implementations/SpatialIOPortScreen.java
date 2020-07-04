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

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;


import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.CommonButtons;
import appeng.container.implementations.SpatialIOPortContainer;
import appeng.core.localization.GuiText;
import appeng.util.Platform;

public class SpatialIOPortScreen extends AEBaseScreen<SpatialIOPortContainer> {

    public SpatialIOPortScreen(SpatialIOPortContainer container, PlayerInventory playerInventory,
            Text title) {
        super(container, playerInventory, title);
        this.backgroundHeight = 199;
    }

    @Override
    public void init() {
        super.init();
        this.addButton(CommonButtons.togglePowerUnit(this.x - 18, this.y + 8));
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.textRenderer.draw(matrices, GuiText.StoredPower.getLocal() + ": "
                + Platform.formatPowerLong(this.handler.getCurrentPower(), false), 13, 21, 4210752);
        this.textRenderer.draw(matrices,
                GuiText.MaxPower.getLocal() + ": " + Platform.formatPowerLong(this.handler.getMaxPower(), false), 13,
                31, 4210752);
        this.textRenderer.draw(matrices, GuiText.RequiredPower.getLocal() + ": "
                + Platform.formatPowerLong(this.handler.getRequiredPower(), false), 13, 73, 4210752);
        this.textRenderer.draw(matrices,
                GuiText.Efficiency.getLocal() + ": " + (((float) this.handler.getEfficency()) / 100) + '%', 13, 83,
                4210752);

        this.textRenderer.draw(matrices, this.getGuiDisplayName(GuiText.SpatialIOPort.getLocal()), 8, 6, 4210752);
        this.textRenderer.draw(matrices, GuiText.inventory.getLocal(), 8, this.backgroundHeight - 96, 4210752);

        if (this.handler.xSize != 0 && this.handler.ySize != 0 && this.handler.zSize != 0) {
            final String text = GuiText.SCSSize.getLocal() + ": " + this.handler.xSize + "x" + this.handler.ySize
                    + "x" + this.handler.zSize;
            this.textRenderer.draw(matrices, text, 13, 93, 4210752);
        } else {
            this.textRenderer.draw(matrices, GuiText.SCSSize.getLocal() + ": " + GuiText.SCSInvalid.getLocal(), 13, 93, 4210752);
        }

    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {
        this.bindTexture("guis/spatialio.png");
        drawTexture(matrices, offsetX, offsetY, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }
}
