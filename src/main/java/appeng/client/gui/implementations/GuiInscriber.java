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
import appeng.client.gui.widgets.GuiProgressBar;
import appeng.client.gui.widgets.GuiProgressBar.Direction;
import appeng.container.implementations.ContainerInscriber;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.core.localization.GuiText;
import appeng.tile.misc.TileInscriber;
import net.minecraft.entity.player.InventoryPlayer;


public class GuiInscriber extends AEBaseGui {

    private final ContainerInscriber cvc;
    private GuiProgressBar pb;

    public GuiInscriber(final InventoryPlayer inventoryPlayer, final TileInscriber te) {
        super(new ContainerInscriber(inventoryPlayer, te));
        this.cvc = (ContainerInscriber) this.inventorySlots;
        this.ySize = 176;
        this.xSize = this.hasToolbox() ? 246 : 211;
    }

    private boolean hasToolbox() {
        return ((ContainerUpgradeable) this.inventorySlots).hasToolbox();
    }

    @Override
    public void initGui() {
        super.initGui();

        this.pb = new GuiProgressBar(this.cvc, "guis/inscriber.png", 135, 39, 135, 177, 6, 18, Direction.VERTICAL);
        this.buttonList.add(this.pb);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.pb.setFullMsg(this.cvc.getCurrentProgress() * 100 / this.cvc.getMaxProgress() + "%");

        this.fontRenderer.drawString(this.getGuiDisplayName(GuiText.Inscriber.getLocal()), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.bindTexture("guis/inscriber.png");
        this.pb.x = 135 + this.guiLeft;
        this.pb.y = 39 + this.guiTop;

        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, 211 - 34, this.ySize);

        if (this.drawUpgrades()) {
            this.drawTexturedModalRect(offsetX + 177, offsetY, 177, 0, 35, 14 + this.cvc.availableUpgrades() * 18);
        }
        if (this.hasToolbox()) {
            this.drawTexturedModalRect(offsetX + 178, offsetY + this.ySize - 90, 178, this.ySize - 90, 68, 68);
        }
    }

    private boolean drawUpgrades() {
        return true;
    }
}
