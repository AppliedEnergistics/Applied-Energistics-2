/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.client.gui;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.core.localization.GuiText;
import appeng.fluids.client.gui.widgets.FluidSlotWidget;
import appeng.fluids.client.gui.widgets.OptionalFluidSlotWidget;
import appeng.fluids.container.FluidIOContainer;
import appeng.fluids.parts.FluidImportBusPart;
import appeng.fluids.util.IAEFluidTank;

/**
 * @author BrockWS
 * @version rv5 - 1/05/2018
 * @since rv5 1/05/2018
 */
public class FluidIOScreen extends UpgradeableScreen<FluidIOContainer> {

    public FluidIOScreen(FluidIOContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
    }

    @Override
    public void init() {
        super.init();

        final IAEFluidTank inv = this.container.getFluidConfigInventory();
        final int y = 40;
        final int x = 80;

        this.guiSlots.add(new FluidSlotWidget(inv, 0, 0, x, y));
        this.guiSlots.add(new OptionalFluidSlotWidget(inv, container, 1, 1, 1, x, y, -1, 0));
        this.guiSlots.add(new OptionalFluidSlotWidget(inv, container, 2, 2, 1, x, y, 1, 0));
        this.guiSlots.add(new OptionalFluidSlotWidget(inv, container, 3, 3, 1, x, y, 0, -1));
        this.guiSlots.add(new OptionalFluidSlotWidget(inv, container, 4, 4, 1, x, y, 0, 1));

        this.guiSlots.add(new OptionalFluidSlotWidget(inv, container, 5, 5, 2, x, y, -1, -1));
        this.guiSlots.add(new OptionalFluidSlotWidget(inv, container, 6, 6, 2, x, y, 1, -1));
        this.guiSlots.add(new OptionalFluidSlotWidget(inv, container, 7, 7, 2, x, y, -1, 1));
        this.guiSlots.add(new OptionalFluidSlotWidget(inv, container, 8, 8, 2, x, y, 1, 1));
    }

    @Override
    protected GuiText getName() {
        return this.bc instanceof FluidImportBusPart ? GuiText.ImportBusFluids : GuiText.ExportBusFluids;
    }
}
