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


import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiProgressBar;
import appeng.client.gui.widgets.GuiProgressBar.Direction;
import appeng.container.implementations.ContainerMAC;
import appeng.core.localization.GuiText;
import appeng.tile.crafting.TileMolecularAssembler;
import net.minecraft.entity.player.InventoryPlayer;


public class GuiMAC extends GuiUpgradeable {

    private final ContainerMAC container;
    private GuiProgressBar pb;

    public GuiMAC(final InventoryPlayer inventoryPlayer, final TileMolecularAssembler te) {
        super(new ContainerMAC(inventoryPlayer, te));
        this.ySize = 197;
        this.container = (ContainerMAC) this.inventorySlots;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.pb = new GuiProgressBar(this.container, "guis/mac.png", 139, 36, 148, 201, 6, 18, Direction.VERTICAL);
        this.buttonList.add(this.pb);
    }

    @Override
    protected void addButtons() {
        this.redstoneMode = new GuiImgButton(this.guiLeft - 18, this.guiTop + 8, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        this.buttonList.add(this.redstoneMode);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.pb.setFullMsg(this.container.getCurrentProgress() + "%");
        super.drawFG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.pb.x = 148 + this.guiLeft;
        this.pb.y = 48 + this.guiTop;
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    protected String getBackground() {
        return "guis/mac.png";
    }

    @Override
    protected GuiText getName() {
        return GuiText.MolecularAssembler;
    }
}
