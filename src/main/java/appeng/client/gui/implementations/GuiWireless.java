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


import appeng.api.config.Settings;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerWireless;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.tile.networking.TileWireless;
import appeng.util.Platform;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.input.Mouse;

import java.io.IOException;


public class GuiWireless extends AEBaseGui {

    private GuiImgButton units;

    public GuiWireless(final InventoryPlayer inventoryPlayer, final TileWireless te) {
        super(new ContainerWireless(inventoryPlayer, te));
        this.ySize = 166;
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        super.actionPerformed(btn);

        final boolean backwards = Mouse.isButtonDown(1);

        if (btn == this.units) {
            AEConfig.instance().nextPowerUnit(backwards);
            this.units.set(AEConfig.instance().selectedPowerUnit());
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        this.units = new GuiImgButton(this.guiLeft - 18, this.guiTop + 8, Settings.POWER_UNITS, AEConfig.instance().selectedPowerUnit());
        this.buttonList.add(this.units);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(this.getGuiDisplayName(GuiText.Wireless.getLocal()), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);

        final ContainerWireless cw = (ContainerWireless) this.inventorySlots;

        if (cw.getRange() > 0) {
            final String firstMessage = GuiText.Range.getLocal() + ": " + (cw.getRange() / 10.0) + " m";
            final String secondMessage = GuiText.PowerUsageRate.getLocal() + ": " + Platform.formatPowerLong(cw.getDrain(), true);

            final int strWidth = Math.max(this.fontRenderer.getStringWidth(firstMessage), this.fontRenderer.getStringWidth(secondMessage));
            final int cOffset = (this.xSize / 2) - (strWidth / 2);
            this.fontRenderer.drawString(firstMessage, cOffset, 20, 4210752);
            this.fontRenderer.drawString(secondMessage, cOffset, 20 + 12, 4210752);
        }
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.bindTexture("guis/wireless.png");
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
    }
}
