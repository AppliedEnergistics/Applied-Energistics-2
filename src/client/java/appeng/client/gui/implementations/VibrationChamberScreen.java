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

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.SpriteRenderer;
import appeng.client.gui.widgets.CommonButtons;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ProgressBar.Direction;
import appeng.core.AppEng;
import appeng.menu.implementations.VibrationChamberMenu;
import appeng.util.Platform;

public class VibrationChamberScreen extends UpgradeableScreen<VibrationChamberMenu> {

    // Burn indicator similar to the "flame" in a vanilla furnace
    private static final Identifier BURN_PROGRESS = AppEng.makeId("vibration_chamber_burn");
    private static final int BURN_PROGRESS_WIDTH = 14;
    private static final int BURN_PROGRESS_HEIGHT = 13;

    // "Progress-bar" that indicates the energy generation rate
    private final ProgressBar generationRateBar;

    public VibrationChamberScreen(VibrationChamberMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.generationRateBar = new ProgressBar(this.menu, Direction.VERTICAL);
        widgets.add("generationRateBar", this.generationRateBar);

        addToLeftToolbar(CommonButtons.togglePowerUnit());
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        var powerPerTick = this.menu.getPowerPerTick();
        var efficiency = this.menu.getFuelEfficiency();
        this.generationRateBar.setFullMsg(
                Component.literal(Platform.formatPower(powerPerTick, true) + "\n" + "Eff: " + efficiency + "%"));
    }

    @Override
    public void drawFG(GuiGraphicsExtractor guiGraphics, int offsetX, int offsetY, int mouseX,
            int mouseY) {
        // Show the flame "burning down" as we burn through an item of fuel
        if (this.menu.getRemainingBurnTime() > 0) {
            int f = this.menu.getRemainingBurnTime() * BURN_PROGRESS_HEIGHT / 100;
            // Aligned with the empty flame in the background (ae2:background/vibration_chamber_burn_empty)
            SpriteRenderer.of(guiGraphics).blitRegion(BURN_PROGRESS,
                    0, BURN_PROGRESS_HEIGHT - f, BURN_PROGRESS_WIDTH, f,
                    80, 21 + BURN_PROGRESS_HEIGHT - f, BURN_PROGRESS_WIDTH, f,
                    false, -1);
        }
    }

}
