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
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ProgressBar.Direction;
import appeng.container.implementations.InscriberContainer;
import appeng.core.localization.GuiText;

public class InscriberScreen extends AEBaseScreen<InscriberContainer> {

    private ProgressBar pb;

    public InscriberScreen(InscriberContainer container, PlayerInventory playerInventory, Text title) {
        super(container, playerInventory, title);
        this.backgroundHeight = 176;
        this.backgroundWidth = this.hasToolbox() ? 246 : 211;
    }

    private boolean hasToolbox() {
        return this.handler.hasToolbox();
    }

    @Override
    public void init() {
        super.init();

        this.pb = new ProgressBar(this.handler, "guis/inscriber.png", 135, 39, 135, 177, 6, 18, Direction.VERTICAL);
        this.addButton(this.pb);
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.pb.setFullMsg(this.handler.getCurrentProgress() * 100 / this.handler.getMaxProgress() + "%");

        this.textRenderer.draw(matrices, this.getGuiDisplayName(GuiText.Inscriber.getLocal()), 8, 6, 4210752);
        this.textRenderer.draw(matrices, GuiText.inventory.getLocal(), 8, this.backgroundHeight - 96 + 3, 4210752);
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {
        this.bindTexture("guis/inscriber.png");
        this.pb.x = 135 + this.x;
        this.pb.y = 39 + this.y;

        drawTexture(matrices, offsetX, offsetY, 0, 0, 211 - 34, this.backgroundHeight);

        if (this.drawUpgrades()) {
            drawTexture(matrices, offsetX + 177, offsetY, 177, 0, 35,
                    14 + this.handler.availableUpgrades() * 18, getZOffset());
        }
        if (this.hasToolbox()) {
            drawTexture(matrices, offsetX + 178, offsetY + this.backgroundHeight - 90, 178, this.backgroundHeight - 90, 68, 68);
        }
    }

    private boolean drawUpgrades() {
        return true;
    }
}
