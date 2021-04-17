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

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Blitter;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ProgressBar.Direction;
import appeng.container.implementations.VibrationChamberContainer;
import appeng.tile.misc.VibrationChamberTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class VibrationChamberScreen extends AEBaseScreen<VibrationChamberContainer> {

    private static final Blitter BACKGROUND = Blitter.texture("guis/vibchamber.png").src(0, 0, 176, 166);
    // "Progress-bar" that indicates the energy generation rate
    private static final Blitter GENERATION_RATE = BACKGROUND.copy().src(176, 14, 6, 18);
    // Burn indicator similar to the "flame" in a vanilla furnace
    private static final Blitter BURN_PROGRESS = BACKGROUND.copy().src(176, 0, 14, 13);

    private ProgressBar pb;

    public VibrationChamberScreen(VibrationChamberContainer container, PlayerInventory playerInventory,
                                  ITextComponent title) {
        super(container, playerInventory, title, BACKGROUND);
    }

    @Override
    public void init() {
        super.init();

        this.pb = new ProgressBar(this.container, this.guiLeft + 99, this.guiTop + 36, GENERATION_RATE,
                Direction.VERTICAL);
        this.addButton(this.pb);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.pb.setFullMsg(new StringTextComponent(VibrationChamberTileEntity.POWER_PER_TICK
                * this.container.getCurrentProgress() / VibrationChamberTileEntity.DILATION_SCALING + " AE/t"));
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX,
                       final int mouseY) {
        // Show the flame "burning down" as we burn through an item of fuel
        if (this.container.getRemainingBurnTime() > 0) {
            int f = this.container.getRemainingBurnTime() * BURN_PROGRESS.getSrcHeight() / 100;
            BURN_PROGRESS.copy()
                    .src(
                            BURN_PROGRESS.getSrcX(),
                            BURN_PROGRESS.getSrcY() + BURN_PROGRESS.getSrcHeight() - f,
                            BURN_PROGRESS.getSrcWidth(),
                            f)
                    .dest(80, 20 + BURN_PROGRESS.getSrcHeight() - f)
                    .blit(matrices, getBlitOffset());
        }
    }

}
