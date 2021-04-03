/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.CommonButtons;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.container.implementations.SpatialAnchorContainer;
import appeng.core.localization.GuiText;
import appeng.util.Platform;

public class SpatialAnchorScreen extends AEBaseScreen<SpatialAnchorContainer> {

    private SettingToggleButton<YesNo> overlayToggle;

    public SpatialAnchorScreen(SpatialAnchorContainer container, PlayerInventory playerInventory,
            ITextComponent title) {
        super(container, playerInventory, title);
        this.ySize = 100;
        this.xSize = 195;
    }

    @Override
    public void init() {
        super.init();
        this.addButton(CommonButtons.togglePowerUnit(this.guiLeft - 18, this.guiTop + 8));
        this.addButton(this.overlayToggle = new ServerSettingToggleButton<>(this.guiLeft - 18, this.guiTop + 28,
                Settings.OVERLAY_MODE, YesNo.NO));
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {

        if (this.overlayToggle != null) {
            this.overlayToggle.set(this.container.getOverlayMode());
        }

        this.font.drawString(matrixStack, this.getGuiDisplayName(GuiText.SpatialAnchor.text()).getString(), 8, 6,
                COLOR_DARK_GRAY);

        String usedPower = GuiText.SpatialAnchorUsedPower
                .text(Platform.formatPowerLong(this.container.powerConsumption * 100, true)).getString();
        this.font.drawString(matrixStack, usedPower, 13, 21, COLOR_DARK_GRAY);
        this.font.drawString(matrixStack,
                GuiText.SpatialAnchorLoadedChunks.text(this.container.loadedChunks).getString(), 13, 31,
                COLOR_DARK_GRAY);

        this.font.drawString(matrixStack, this.getGuiDisplayName(GuiText.SpatialAnchorStatistics.text()).getString(), 8,
                56, COLOR_DARK_GRAY);

        this.font.drawString(matrixStack,
                GuiText.SpatialAnchorAllLoaded.text(this.container.allLoadedChunks, this.container.allLoadedWorlds)
                        .getString(),
                13, 71, COLOR_DARK_GRAY);

        this.font.drawString(matrixStack,
                GuiText.SpatialAnchorAll.text(this.container.allChunks, this.container.allWorlds).getString(), 13,
                81, COLOR_DARK_GRAY);
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        this.bindTexture("guis/spatialanchor.png");
        blit(matrices, offsetX, offsetY, 0, 0, this.xSize, this.ySize);
    }
}
