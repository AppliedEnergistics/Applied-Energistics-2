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

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.config.FullnessMode;
import appeng.api.config.OperationMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.menu.implementations.IOPortMenu;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

public class IOPortScreen extends UpgradeableScreen<IOPortMenu> {

    private final SettingToggleButton<FullnessMode> fullMode;
    private final SettingToggleButton<OperationMode> operationMode;
    private final SettingToggleButton<RedstoneMode> redstoneMode;

    public IOPortScreen(IOPortMenu container, Inventory playerInventory, Component title,
                        ScreenStyle style) {
        super(container, playerInventory, title, style);

        this.fullMode = new ServerSettingToggleButton<>(Settings.FULLNESS_MODE,
                FullnessMode.EMPTY);
        addToLeftToolbar(this.fullMode);
        this.redstoneMode = new ServerSettingToggleButton<>(
                Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        addToLeftToolbar(this.redstoneMode);

        this.operationMode = new ServerSettingToggleButton<>(Settings.OPERATION_MODE, OperationMode.EMPTY);
        widgets.add("operationMode", this.operationMode);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.redstoneMode.set(this.menu.getRedStoneMode());
        this.operationMode.set(this.menu.getOperationMode());
        this.fullMode.set(this.menu.getFullMode());
    }

    @Override
    public void drawBG(PoseStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        super.drawBG(matrixStack, offsetX, offsetY, mouseX, mouseY, partialTicks);

        this.drawItem(offsetX + 66 - 8, offsetY + 17, AEItems.CELL1K.stack());
        this.drawItem(offsetX + 94 + 8, offsetY + 17, AEBlocks.DRIVE.stack());
    }

}
