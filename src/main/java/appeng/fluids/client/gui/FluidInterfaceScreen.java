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
import appeng.client.gui.style.ScreenStyle;
import appeng.container.SlotSemantic;
import appeng.fluids.client.gui.widgets.FluidSlotWidget;
import appeng.fluids.client.gui.widgets.FluidTankWidget;
import appeng.fluids.container.FluidInterfaceContainer;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.util.IAEFluidTank;

public class FluidInterfaceScreen extends UpgradeableScreen<FluidInterfaceContainer> {

    public FluidInterfaceScreen(FluidInterfaceContainer container, PlayerInventory playerInventory,
            ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title, style);

        final IAEFluidTank configFluids = this.menu.getFluidConfigInventory();
        for (int i = 0; i < DualityFluidInterface.NUMBER_OF_TANKS; ++i) {
            addSlot(new FluidSlotWidget(configFluids, i), SlotSemantic.CONFIG);
        }

        final IAEFluidTank fluidTank = this.menu.getTanks();
        for (int i = 0; i < DualityFluidInterface.NUMBER_OF_TANKS; ++i) {
            widgets.add("tank" + (i + 1), new FluidTankWidget(fluidTank, i));
        }

        widgets.addOpenPriorityButton();
    }

}
