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
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ProgressBar.Direction;
import appeng.container.implementations.VibrationChamberContainer;
import appeng.core.localization.GuiText;
import appeng.tile.misc.VibrationChamberTileEntity;

public class VibrationChamberScreen extends AEBaseScreen<VibrationChamberContainer> {

    private ProgressBar pb;

    public VibrationChamberScreen(VibrationChamberContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.ySize = 166;
    }

    @Override
    public void init() {
        super.init();

        this.pb = new ProgressBar(this.container, "guis/vibchamber.png", 99, 36, 176, 14, 6, 18, Direction.VERTICAL);
        this.addButton(this.pb);
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.font.method_30883(matrices, this.getGuiDisplayName(GuiText.VibrationChamber.text()), 8, 6, 4210752);
        this.font.method_30883(matrices, GuiText.inventory.text(), 8, this.ySize - 96 + 3, 4210752);

        this.pb.setFullMsg(new StringTextComponent(VibrationChamberTileEntity.POWER_PER_TICK
                * this.container.getCurrentProgress() / VibrationChamberTileEntity.DILATION_SCALING + " AE/t"));

        if (this.container.getRemainingBurnTime() > 0) {
            final int i1 = this.container.getRemainingBurnTime() * 12 / 100;
            this.bindTexture("guis/vibchamber.png");
            RenderSystem.color3f(1, 1, 1);
            final int l = -15;
            final int k = 25;
            blit(matrices, k + 56, l + 36 + 12 - i1, 176, 12 - i1, 14, i1 + 2);
        }
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY,
            float partialTicks) {
        this.bindTexture("guis/vibchamber.png");
        this.pb.x = 99 + this.guiLeft;
        this.pb.y = 36 + this.guiTop;
        blit(matrices, offsetX, offsetY, 0, 0, this.xSize, this.ySize);
    }
}
