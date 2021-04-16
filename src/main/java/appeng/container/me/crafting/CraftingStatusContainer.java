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

package appeng.container.me.crafting;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.text.ITextComponent;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.storage.ITerminalHost;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerTypeBuilder;

/**
 * @see appeng.client.gui.me.crafting.CraftingStatusScreen
 */
public class CraftingStatusContainer extends CraftingCPUContainer implements CraftingCPUCyclingContainer {

    public static final ContainerType<CraftingStatusContainer> TYPE = ContainerTypeBuilder
            .create(CraftingStatusContainer::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .build("craftingstatus");

    private final CraftingCPUCycler cpuCycler = new CraftingCPUCycler(this::cpuMatches, this::onCPUSelectionChanged);

    @GuiSync(6)
    public boolean noCPU = true;

    @GuiSync(7)
    public ITextComponent cpuName;

    public CraftingStatusContainer(int id, final PlayerInventory ip, final ITerminalHost te) {
        super(TYPE, id, ip, te);
    }

    @Override
    public void detectAndSendChanges() {
        IGrid network = this.getGrid();
        if (isServer() && network != null) {
            cpuCycler.detectAndSendChanges(network);
        }

        super.detectAndSendChanges();
    }

    private boolean cpuMatches(final ICraftingCPU c) {
        return c.isBusy();
    }

    private void onCPUSelectionChanged(CraftingCPURecord cpuRecord, boolean cpusAvailable) {
        noCPU = !cpusAvailable;
        if (cpuRecord == null) {
            cpuName = null;
            setCPU(null);
        } else {
            cpuName = cpuRecord.getName();
            setCPU(cpuRecord.getCpu());
        }
    }

    @Override
    public void cycleSelectedCPU(boolean forward) {
        this.cpuCycler.cycleCpu(forward);
    }

}
