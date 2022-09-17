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


import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerQuartzKnife;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.items.contents.QuartzKnifeObj;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;

import java.io.IOException;


public class GuiQuartzKnife extends AEBaseGui {

    private GuiTextField name;

    public GuiQuartzKnife(final InventoryPlayer inventoryPlayer, final QuartzKnifeObj te) {
        super(new ContainerQuartzKnife(inventoryPlayer, te));
        this.ySize = 184;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.name = new GuiTextField(0, this.fontRenderer, this.guiLeft + 24, this.guiTop + 32, 79, this.fontRenderer.FONT_HEIGHT);
        this.name.setEnableBackgroundDrawing(false);
        this.name.setMaxStringLength(32);
        this.name.setTextColor(0xFFFFFF);
        this.name.setVisible(true);
        this.name.setFocused(true);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(this.getGuiDisplayName(GuiText.QuartzCuttingKnife.getLocal()), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.bindTexture("guis/quartzknife.png");
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
        this.name.drawTextBox();
    }

    @Override
    protected void keyTyped(final char character, final int key) throws IOException {
        if (this.name.textboxKeyTyped(character, key)) {
            try {
                final String Out = this.name.getText();
                ((ContainerQuartzKnife) this.inventorySlots).setName(Out);
                NetworkHandler.instance().sendToServer(new PacketValueConfig("QuartzKnife.Name", Out));
            } catch (final IOException e) {
                AELog.debug(e);
            }
        } else {
            super.keyTyped(character, key);
        }
    }
}
