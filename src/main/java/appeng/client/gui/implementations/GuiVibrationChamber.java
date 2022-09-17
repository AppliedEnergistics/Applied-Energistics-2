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
import appeng.container.implementations.ContainerVibrationChamber;
import appeng.core.localization.GuiText;
import appeng.tile.misc.TileVibrationChamber;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;


public class GuiVibrationChamber extends AEBaseGui {

    private final ContainerVibrationChamber cvc;
    private GuiProgressBar pb;

    public GuiVibrationChamber(final InventoryPlayer inventoryPlayer, final TileVibrationChamber te) {
        super(new ContainerVibrationChamber(inventoryPlayer, te));
        this.cvc = (ContainerVibrationChamber) this.inventorySlots;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.pb = new GuiProgressBar(this.cvc, "guis/vibchamber.png", 99, 36, 176, 14, 6, 18, Direction.VERTICAL);
        this.buttonList.add(this.pb);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(this.getGuiDisplayName(GuiText.VibrationChamber.getLocal()), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);

        this.pb.setFullMsg(TileVibrationChamber.POWER_PER_TICK * this.cvc.getCurrentProgress() / TileVibrationChamber.DILATION_SCALING + " AE/t");

        if (this.cvc.getRemainingBurnTime() > 0) {
            final int i1 = this.cvc.getRemainingBurnTime() * 12 / 100;
            this.bindTexture("guis/vibchamber.png");
            GlStateManager.color(1, 1, 1);
            final int l = -15;
            final int k = 25;
            this.drawTexturedModalRect(k + 56, l + 36 + 12 - i1, 176, 12 - i1, 14, i1 + 2);
        }
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.bindTexture("guis/vibchamber.png");
        this.pb.x = 99 + this.guiLeft;
        this.pb.y = 36 + this.guiTop;
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
    }
}
