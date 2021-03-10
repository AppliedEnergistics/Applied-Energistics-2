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
import net.minecraftforge.fml.client.gui.GuiUtils;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.ToggleButton;
import appeng.container.implementations.NetworkToolContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

public class NetworkToolScreen extends AEBaseScreen<NetworkToolContainer> {

    private ToggleButton tFacades;

    public NetworkToolScreen(NetworkToolContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.imageHeight = 166;
    }

    @Override
    public void init() {
        super.init();

        this.tFacades = new ToggleButton(this.leftPos - 18, this.topPos + 8, 23, 22,
                GuiText.TransparentFacades.getLocal(), GuiText.TransparentFacadesHint.getLocal(),
                btn -> toggleFacades());

        this.addButton(this.tFacades);
    }

    private void toggleFacades() {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("NetworkTool", "Toggle"));
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        if (this.tFacades != null) {
            this.tFacades.setState(menu.isFacadeMode());
        }

        this.font.draw(matrixStack, this.getGuiDisplayName(GuiText.NetworkTool.text()).getString(), 8, 6,
                4210752);
        this.font.draw(matrixStack, GuiText.inventory.getLocal(), 8, this.imageHeight - 96 + 3, 4210752);
    }

    @Override
    public void drawBG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        this.bindTexture("guis/toolbox.png");
        GuiUtils.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.imageWidth, this.imageHeight, getBlitOffset());
    }
}
