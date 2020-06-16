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

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiProgressBar;
import appeng.client.gui.widgets.GuiProgressBar.Direction;
import appeng.container.implementations.ContainerInscriber;
import appeng.core.localization.GuiText;

public class GuiInscriber extends AEBaseGui<ContainerInscriber> {

    private GuiProgressBar pb;

    public GuiInscriber(ContainerInscriber container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.ySize = 176;
        this.xSize = this.hasToolbox() ? 246 : 211;
    }

    private boolean hasToolbox() {
        return this.container.hasToolbox();
    }

    @Override
    public void init() {
        super.init();

        this.pb = new GuiProgressBar(this.container, "guis/inscriber.png", 135, 39, 135, 177, 6, 18,
                Direction.VERTICAL);
        this.addButton(this.pb);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.pb.setFullMsg(this.container.getCurrentProgress() * 100 / this.container.getMaxProgress() + "%");

        this.font.drawString(this.getGuiDisplayName(GuiText.Inscriber.getLocal()), 8, 6, 4210752);
        this.font.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {
        this.bindTexture("guis/inscriber.png");
        this.pb.x = 135 + this.guiLeft;
        this.pb.y = 39 + this.guiTop;

        GuiUtils.drawTexturedModalRect(offsetX, offsetY, 0, 0, 211 - 34, this.ySize, getBlitOffset());

        if (this.drawUpgrades()) {
            GuiUtils.drawTexturedModalRect(offsetX + 177, offsetY, 177, 0, 35,
                    14 + this.container.availableUpgrades() * 18, getBlitOffset());
        }
        if (this.hasToolbox()) {
            GuiUtils.drawTexturedModalRect(offsetX + 178, offsetY + this.ySize - 90, 178, this.ySize - 90, 68, 68,
                    getBlitOffset());
        }
    }

    private boolean drawUpgrades() {
        return true;
    }
}
