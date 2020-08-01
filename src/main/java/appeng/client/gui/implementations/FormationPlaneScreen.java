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

import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.TabButton;
import appeng.container.implementations.FormationPlaneContainer;
import appeng.container.implementations.PriorityContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.SwitchGuisPacket;

public class FormationPlaneScreen extends UpgradeableScreen<FormationPlaneContainer> {

    private SettingToggleButton<YesNo> placeMode;

    public FormationPlaneScreen(FormationPlaneContainer container, PlayerInventory playerInventory, Text title) {
        super(container, playerInventory, title);
        this.backgroundHeight = 251;
    }

    @Override
    protected void addButtons() {
        this.placeMode = new ServerSettingToggleButton<>(this.x - 18, this.y + 28, Settings.PLACE_BLOCK, YesNo.YES);
        this.fuzzyMode = new ServerSettingToggleButton<>(this.x - 18, this.y + 48, Settings.FUZZY_MODE,
                FuzzyMode.IGNORE_ALL);

        this.addButton(new TabButton(this.x + 154, this.y, 2 + 4 * 16, GuiText.Priority.text(), this.itemRenderer,
                btn -> openPriorityGui()));

        this.addButton(this.placeMode);
        this.addButton(this.fuzzyMode);
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.textRenderer.method_30883(matrices, this.getGuiDisplayName(GuiText.FormationPlane.text()), 8, 6, 4210752);
        this.textRenderer.method_30883(matrices, GuiText.inventory.text(), 8, this.backgroundHeight - 96 + 3, 4210752);

        if (this.fuzzyMode != null) {
            this.fuzzyMode.set(this.cvb.getFuzzyMode());
        }

        if (this.placeMode != null) {
            this.placeMode.set(((FormationPlaneContainer) this.cvb).getPlaceMode());
        }
    }

    @Override
    protected String getBackground() {
        return "guis/storagebus.png";
    }

    private void openPriorityGui() {
        NetworkHandler.instance().sendToServer(new SwitchGuisPacket(PriorityContainer.TYPE));
    }

}
