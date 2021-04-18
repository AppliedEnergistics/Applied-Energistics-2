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

import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.client.gui.implementations.IOBusScreen;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.fluids.client.gui.widgets.FluidSlotWidget;
import appeng.fluids.client.gui.widgets.OptionalFluidSlotWidget;
import appeng.fluids.container.FluidIOBusContainer;
import appeng.fluids.util.IAEFluidTank;

/**
 * @see appeng.fluids.parts.FluidImportBusPart
 * @see appeng.fluids.parts.FluidExportBusPart
 */
public class FluidIOBusScreen extends UpgradeableScreen<FluidIOBusContainer> {

    private SettingToggleButton<RedstoneMode> redstoneMode;

    public FluidIOBusScreen(FluidIOBusContainer container, PlayerInventory playerInventory, ITextComponent title,
            ScreenStyle style) {
        super(container, playerInventory, title, style);
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

        this.redstoneMode = new ServerSettingToggleButton<>(0, 0, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        addToLeftToolbar(this.redstoneMode);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.redstoneMode.set(this.container.getRedStoneMode());
        this.redstoneMode.setVisibility(container.hasUpgrade(Upgrades.REDSTONE));
    }

}
