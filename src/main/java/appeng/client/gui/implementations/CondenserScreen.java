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

import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ProgressBar.Direction;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.container.implementations.CondenserContainer;
import appeng.core.localization.GuiText;

public class CondenserScreen extends AEBaseScreen<CondenserContainer> {

    private SettingToggleButton<CondenserOutput> mode;

    public CondenserScreen(CondenserContainer container, PlayerInventory playerInventory, Text title) {
        super(container, playerInventory, title);
        this.backgroundHeight = 197;
    }

    @Override
    public void init() {
        super.init();

        this.mode = new ServerSettingToggleButton<>(128 + this.x, 52 + this.y, Settings.CONDENSER_OUTPUT,
                this.handler.getOutput());

        this.addButton(new ProgressBar(this.handler, "guis/condenser.png", 120 + this.x, 25 + this.y, 178, 25, 6, 18,
                Direction.VERTICAL, GuiText.StoredEnergy.text()));
        this.addButton(this.mode);
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.textRenderer.method_30883(matrices, this.getGuiDisplayName(GuiText.Condenser.text()), 8, 6, 4210752);
        this.textRenderer.method_30883(matrices, GuiText.inventory.text(), 8, this.backgroundHeight - 96 + 3, 4210752);

        this.mode.set(this.handler.getOutput());
        this.mode.setFillVar(String.valueOf(this.handler.getOutput().requiredPower));
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY,
            float partialTicks) {
        this.bindTexture("guis/condenser.png");

        drawTexture(matrices, offsetX, offsetY, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }
}
