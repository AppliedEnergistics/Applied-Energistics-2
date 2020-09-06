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

import appeng.api.config.FullnessMode;
import appeng.api.config.OperationMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.definitions.IDefinitions;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.theme.ThemeColor;
import appeng.container.implementations.IOPortContainer;
import appeng.core.Api;
import appeng.core.localization.GuiText;

public class IOPortScreen extends UpgradeableScreen<IOPortContainer> {

    private SettingToggleButton<FullnessMode> fullMode;
    private SettingToggleButton<OperationMode> operationMode;

    public IOPortScreen(IOPortContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.ySize = 166;
    }

    @Override
    protected void addButtons() {
        this.fullMode = new ServerSettingToggleButton<>(this.guiLeft - 18, this.guiTop + 8, Settings.FULLNESS_MODE,
                FullnessMode.EMPTY);
        this.operationMode = new ServerSettingToggleButton<>(this.guiLeft + 80, this.guiTop + 17,
                Settings.OPERATION_MODE, OperationMode.EMPTY);

        this.addButton(this.operationMode);
        this.addButton(this.fullMode);
        this.redstoneMode = new ServerSettingToggleButton<>(this.guiLeft - 18, this.guiTop + 28,
                Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        addButton(this.redstoneMode);
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        this.font.drawString(matrixStack, this.getGuiDisplayName(GuiText.IOPort.text()).getString(), 8, 6,
                ThemeColor.TEXT_TITLE.argb());
        this.font.drawString(matrixStack, GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3,
                ThemeColor.TEXT_TITLE.argb());

        if (this.redstoneMode != null) {
            this.redstoneMode.set(this.cvb.getRedStoneMode());
        }

        if (this.operationMode != null) {
            this.operationMode.set(((IOPortContainer) this.cvb).getOperationMode());
        }

        if (this.fullMode != null) {
            this.fullMode.set(((IOPortContainer) this.cvb).getFullMode());
        }
    }

    @Override
    public void drawBG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        super.drawBG(matrixStack, offsetX, offsetY, mouseX, mouseY, partialTicks);

        final IDefinitions definitions = Api.instance().definitions();

        definitions.items().cell1k().maybeStack(1)
                .ifPresent(cell1kStack -> this.drawItem(offsetX + 66 - 8, offsetY + 17, cell1kStack));

        definitions.blocks().drive().maybeStack(1)
                .ifPresent(driveStack -> this.drawItem(offsetX + 94 + 8, offsetY + 17, driveStack));
    }

    @Override
    protected String getBackground() {
        return "guis/io_port.png";
    }

}
