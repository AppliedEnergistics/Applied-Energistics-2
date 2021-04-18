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
import net.minecraft.util.text.StringTextComponent;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ProgressBar.Direction;
import appeng.container.implementations.MolecularAssemblerContainer;

public class MolecularAssemblerScreen extends UpgradeableScreen<MolecularAssemblerContainer> {

    private static final Blitter PROGRESS_BAR = Blitter.texture("guis/molecular_assembler.png").src(148, 201, 6, 18);

    private ProgressBar pb;

    public MolecularAssemblerScreen(MolecularAssemblerContainer container, PlayerInventory playerInventory,
            ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title, style);
    }

    @Override
    public void init() {
        super.init();

        this.pb = new ProgressBar(this.container, this.guiLeft + 148, this.guiTop + 48, PROGRESS_BAR,
                Direction.VERTICAL);
        this.addButton(this.pb);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.pb.setFullMsg(new StringTextComponent(this.container.getCurrentProgress() + "%"));
    }

}
