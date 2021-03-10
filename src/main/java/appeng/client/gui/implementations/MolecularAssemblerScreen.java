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

import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ProgressBar.Direction;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.container.implementations.MolecularAssemblerContainer;
import appeng.core.localization.GuiText;

public class MolecularAssemblerScreen extends UpgradeableScreen<MolecularAssemblerContainer> {

    private ProgressBar pb;

    public MolecularAssemblerScreen(MolecularAssemblerContainer container, PlayerInventory playerInventory,
            ITextComponent title) {
        super(container, playerInventory, title);
        this.imageHeight = 197;
    }

    @Override
    public void init() {
        super.init();

        this.pb = new ProgressBar(this.menu, "guis/molecular_assembler.png", 139, 36, 148, 201, 6, 18,
                Direction.VERTICAL);
        this.addButton(this.pb);
    }

    @Override
    protected void addButtons() {
        this.redstoneMode = new ServerSettingToggleButton<>(this.leftPos - 18, this.topPos + 8,
                Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        addButton(this.redstoneMode);
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        this.pb.setFullMsg(new StringTextComponent(this.menu.getCurrentProgress() + "%"));
        super.drawFG(matrixStack, offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    public void drawBG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        this.pb.x = 148 + this.leftPos;
        this.pb.y = 48 + this.topPos;
        super.drawBG(matrixStack, offsetX, offsetY, mouseX, mouseY, partialTicks);
    }

    @Override
    protected String getBackground() {
        return "guis/molecular_assembler.png";
    }

    @Override
    protected GuiText getName() {
        return GuiText.MolecularAssembler;
    }
}
