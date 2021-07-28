/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.container.SlotSemantic;
import appeng.fluids.client.gui.widgets.FluidSlotWidget;
import appeng.fluids.client.gui.widgets.OptionalFluidSlotWidget;
import appeng.fluids.container.FluidFormationPlaneContainer;
import appeng.fluids.util.IAEFluidTank;

public class FluidFormationPlaneScreen extends UpgradeableScreen<FluidFormationPlaneContainer> {

    public FluidFormationPlaneScreen(FluidFormationPlaneContainer container, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(container, playerInventory, title, style);

        final IAEFluidTank config = container.getFluidConfigInventory();

        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 9; x++) {
                final int idx = y * 9 + x;
                if (y < 2) {
                    addSlot(new FluidSlotWidget(config, idx), SlotSemantic.CONFIG);
                } else {
                    addSlot(new OptionalFluidSlotWidget(config, container, idx, y - 2), SlotSemantic.CONFIG);
                }
            }
        }

        widgets.addOpenPriorityButton();
    }

}
