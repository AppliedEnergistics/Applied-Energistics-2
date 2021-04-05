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
import net.minecraft.util.text.StringTextComponent;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Blitter;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ProgressBar.Direction;
import appeng.container.implementations.InscriberContainer;
import appeng.core.localization.GuiText;

public class InscriberScreen extends UpgradeableScreen<InscriberContainer> {

    private static final Blitter BACKGROUND = Blitter.texture("guis/inscriber.png").src(0, 0, 176, 176);

    private ProgressBar pb;

    public InscriberScreen(InscriberContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title, BACKGROUND);
    }

    @Override
    public void init() {
        super.init();

        this.pb = new ProgressBar(this.container, "guis/inscriber.png", 135, 39, 135, 177, 6, 18, Direction.VERTICAL);
        this.addButton(this.pb);
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        this.pb.setFullMsg(new StringTextComponent(
                this.container.getCurrentProgress() * 100 / this.container.getMaxProgress() + "%"));

        this.font.drawString(matrixStack, this.getGuiDisplayName(GuiText.Inscriber.text()).getString(), 8, 6,
                COLOR_DARK_GRAY);
        this.font.drawString(matrixStack, GuiText.inventory.text().getString(), 8, this.ySize - 96 + 3,
                COLOR_DARK_GRAY);
    }

}
