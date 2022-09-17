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


import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.core.localization.GuiText;
import appeng.fluids.client.gui.widgets.GuiFluidSlot;
import appeng.fluids.client.gui.widgets.GuiOptionalFluidSlot;
import appeng.fluids.container.ContainerFluidIO;
import appeng.fluids.parts.PartFluidImportBus;
import appeng.fluids.parts.PartSharedFluidBus;
import appeng.fluids.util.IAEFluidTank;
import net.minecraft.entity.player.InventoryPlayer;


/**
 * @author BrockWS
 * @version rv5 - 1/05/2018
 * @since rv5 1/05/2018
 */
public class GuiFluidIO extends GuiUpgradeable {
    private final PartSharedFluidBus bus;

    public GuiFluidIO(InventoryPlayer inventoryPlayer, PartSharedFluidBus te) {
        super(new ContainerFluidIO(inventoryPlayer, te));
        this.bus = te;
    }

    @Override
    public void initGui() {
        super.initGui();

        final ContainerFluidIO container = (ContainerFluidIO) this.inventorySlots;
        final IAEFluidTank inv = this.bus.getConfig();
        final int y = 40;
        final int x = 80;

        this.guiSlots.add(new GuiFluidSlot(inv, 0, 0, x, y));
        this.guiSlots.add(new GuiOptionalFluidSlot(inv, container, 1, 1, 1, x, y, -1, 0));
        this.guiSlots.add(new GuiOptionalFluidSlot(inv, container, 2, 2, 1, x, y, 1, 0));
        this.guiSlots.add(new GuiOptionalFluidSlot(inv, container, 3, 3, 1, x, y, 0, -1));
        this.guiSlots.add(new GuiOptionalFluidSlot(inv, container, 4, 4, 1, x, y, 0, 1));

        this.guiSlots.add(new GuiOptionalFluidSlot(inv, container, 5, 5, 2, x, y, -1, -1));
        this.guiSlots.add(new GuiOptionalFluidSlot(inv, container, 6, 6, 2, x, y, 1, -1));
        this.guiSlots.add(new GuiOptionalFluidSlot(inv, container, 7, 7, 2, x, y, -1, 1));
        this.guiSlots.add(new GuiOptionalFluidSlot(inv, container, 8, 8, 2, x, y, 1, 1));
    }

    @Override
    protected GuiText getName() {
        return this.bc instanceof PartFluidImportBus ? GuiText.ImportBusFluids : GuiText.ExportBusFluids;
    }
}
