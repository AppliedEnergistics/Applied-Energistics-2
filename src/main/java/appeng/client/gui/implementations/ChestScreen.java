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

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.TabButton;
import appeng.container.implementations.ChestContainer;
import appeng.container.implementations.PriorityContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.SwitchGuisPacket;

public class ChestScreen extends AEBaseScreen<ChestContainer> {

    public ChestScreen(ChestContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.ySize = 166;
    }

    @Override
    public void init() {
        super.init();

        this.addButton(new TabButton(this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.text(),
                this.itemRenderer, btn -> openPriority()));
    }

    private void openPriority() {
        NetworkHandler.instance().sendToServer(new SwitchGuisPacket(PriorityContainer.TYPE));
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        this.font.drawString(matrixStack, this.getGuiDisplayName(GuiText.Chest.text()).getString(), 8, 6, 4210752);
        this.font.drawString(matrixStack, GuiText.inventory.text().getString(), 8, this.ySize - 96 + 3, 4210752);
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY,
            float partialTicks) {
        this.bindTexture("guis/chest.png");
        blit(matrices, offsetX, offsetY, 0, 0, this.xSize, this.ySize);
    }
}
