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

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.CommonButtons;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.container.implementations.SpatialAnchorContainer;
import appeng.core.localization.GuiText;
import appeng.util.Platform;

public class SpatialAnchorScreen extends AEBaseScreen<SpatialAnchorContainer> {

    private final SettingToggleButton<YesNo> overlayToggle;

    public SpatialAnchorScreen(SpatialAnchorContainer container, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(container, playerInventory, title, style);
        this.addToLeftToolbar(CommonButtons.togglePowerUnit());
        this.addToLeftToolbar(this.overlayToggle = new ServerSettingToggleButton<>(
                Settings.OVERLAY_MODE, YesNo.NO));
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.overlayToggle.set(this.menu.getOverlayMode());

        setTextContent("used_power", GuiText.SpatialAnchorUsedPower
                .text(Platform.formatPowerLong(this.menu.powerConsumption * 100, true)));
        setTextContent("loaded_chunks", GuiText.SpatialAnchorLoadedChunks.text(this.menu.loadedChunks));
        setTextContent("statistics_loaded",
                GuiText.SpatialAnchorAllLoaded.text(this.menu.allLoadedChunks, this.menu.allLoadedWorlds));
        setTextContent("statistics_total",
                GuiText.SpatialAnchorAll.text(this.menu.allChunks, this.menu.allWorlds));
    }

}
