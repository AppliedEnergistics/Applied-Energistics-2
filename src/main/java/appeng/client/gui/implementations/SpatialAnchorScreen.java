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

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Blitter;
import appeng.client.gui.widgets.CommonButtons;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.container.implementations.SpatialAnchorContainer;
import appeng.core.localization.GuiText;
import appeng.util.Platform;

public class SpatialAnchorScreen extends AEBaseScreen<SpatialAnchorContainer> {

    private static final Blitter BACKGROUND = Blitter.texture("guis/spatialanchor.png").src(0, 0, 195, 100);

    private SettingToggleButton<YesNo> overlayToggle;

    public SpatialAnchorScreen(SpatialAnchorContainer container, PlayerInventory playerInventory,
            ITextComponent title) {
        super(container, playerInventory, title, BACKGROUND);
    }

    @Override
    public void init() {
        super.init();
        this.addToLeftToolbar(CommonButtons.togglePowerUnit(0, 0));
        this.addToLeftToolbar(this.overlayToggle = new ServerSettingToggleButton<>(0, 0,
                Settings.OVERLAY_MODE, YesNo.NO));
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.overlayToggle.set(this.container.getOverlayMode());

        setTextContent("used_power", GuiText.SpatialAnchorUsedPower
                .text(Platform.formatPowerLong(this.container.powerConsumption * 100, true)));
        setTextContent("loaded_chunks", GuiText.SpatialAnchorLoadedChunks.text(this.container.loadedChunks));
        setTextContent("statistics_loaded",
                GuiText.SpatialAnchorAllLoaded.text(this.container.allLoadedChunks, this.container.allLoadedWorlds));
        setTextContent("statistics_total",
                GuiText.SpatialAnchorAll.text(this.container.allChunks, this.container.allWorlds));
    }

}
