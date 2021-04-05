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
import appeng.container.implementations.GrinderContainer;
import appeng.core.localization.GuiText;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GrinderScreen extends AEBaseScreen<GrinderContainer> {

    private static final Blitter BACKGROUND = Blitter.texture("guis/grinder.png").src(0, 0, 176, 176);

    public GrinderScreen(GrinderContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title, BACKGROUND);
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX,
                       final int mouseY) {
        this.font.drawString(matrices, this.getGuiDisplayName(GuiText.GrindStone.text()).getString(), 8, 6,
                COLOR_DARK_GRAY);
        this.font.drawString(matrices, GuiText.inventory.text().getString(), 8, this.ySize - 96 + 3, COLOR_DARK_GRAY);
    }

}
