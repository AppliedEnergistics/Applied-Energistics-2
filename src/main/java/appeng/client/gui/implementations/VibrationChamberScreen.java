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

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.CommonButtons;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ProgressBar.Direction;
import appeng.menu.implementations.VibrationChamberMenu;
import appeng.util.Platform;

public class VibrationChamberScreen extends AEBaseScreen<VibrationChamberMenu> {

    // Burn indicator similar to the "flame" in a vanilla furnace
    private static final Blitter BURN_PROGRESS = Blitter.texture("guis/vibchamber.png").src(176, 0, 14, 13);

    // "Progress-bar" that indicates the energy generation rate
    private final ProgressBar generationRateBar;

    public VibrationChamberScreen(VibrationChamberMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.generationRateBar = new ProgressBar(this.menu, style.getImage("generationRateBar"),
                Direction.VERTICAL);
        widgets.add("generationRateBar", this.generationRateBar);

        addToLeftToolbar(CommonButtons.togglePowerUnit());
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        var powerPerTick = this.menu.getPowerPerTick();
        this.generationRateBar.setFullMsg(Component.literal(Platform.formatPower(powerPerTick, true)));
    }

    @Override
    public void drawFG(PoseStack poseStack, int offsetX, int offsetY, int mouseX,
            int mouseY) {
        // Show the flame "burning down" as we burn through an item of fuel
        if (this.menu.getRemainingBurnTime() > 0) {
            int f = this.menu.getRemainingBurnTime() * BURN_PROGRESS.getSrcHeight() / 100;
            BURN_PROGRESS.copy()
                    .src(
                            BURN_PROGRESS.getSrcX(),
                            BURN_PROGRESS.getSrcY() + BURN_PROGRESS.getSrcHeight() - f,
                            BURN_PROGRESS.getSrcWidth(),
                            f)
                    .dest(80, 20 + BURN_PROGRESS.getSrcHeight() - f)
                    .blit(poseStack, getBlitOffset());
        }
    }

}
