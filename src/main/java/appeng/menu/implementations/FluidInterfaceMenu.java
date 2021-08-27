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

package appeng.menu.implementations;

import java.util.Map;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.SecurityPermissions;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.IConfigManager;
import appeng.client.gui.implementations.FluidInterfaceScreen;
import appeng.helpers.DualityFluidInterface;
import appeng.helpers.FluidSyncHelper;
import appeng.helpers.IFluidInterfaceHost;
import appeng.util.fluid.IAEFluidTank;

/**
 * @see FluidInterfaceScreen
 */
public class FluidInterfaceMenu extends FluidConfigurableMenu<IFluidInterfaceHost> {

    public static final MenuType<FluidInterfaceMenu> TYPE = MenuTypeBuilder
            .create(FluidInterfaceMenu::new, IFluidInterfaceHost.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("fluid_interface");

    private final DualityFluidInterface myDuality;
    private final FluidSyncHelper tankSync;

    public FluidInterfaceMenu(int id, final Inventory ip, IFluidInterfaceHost host) {
        super(TYPE, id, ip, host);

        this.myDuality = host.getDualityFluidInterface();
        this.tankSync = new FluidSyncHelper(this.myDuality.getTanks(), DualityFluidInterface.NUMBER_OF_TANKS);
    }

    public IAEFluidTank getTanks() {
        return myDuality.getTanks();
    }

    @Override
    public IAEFluidTank getFluidConfigInventory() {
        return this.myDuality.getConfig();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (isServer()) {
            this.tankSync.sendDiff(getPlayer());
        }
    }

    @Override
    protected void setupConfig() {
    }

    @Override
    protected void loadSettingsFromHost(final IConfigManager cm) {
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        this.tankSync.sendFull(getPlayer());
    }

    @Override
    public void receiveFluidSlots(Map<Integer, IAEFluidStack> fluids) {
        super.receiveFluidSlots(fluids);
        this.tankSync.readPacket(fluids);
    }

    @Override
    protected boolean supportCapacity() {
        return false;
    }

    @Override
    public boolean hasToolbox() {
        return false;
    }
}
