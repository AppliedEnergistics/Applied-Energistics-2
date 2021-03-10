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

package appeng.fluids.container;

import java.util.Collections;
import java.util.Map;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.network.PacketBuffer;

import appeng.api.config.SecurityPermissions;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.IConfigManager;
import appeng.container.ContainerLocator;
import appeng.container.implementations.ContainerHelper;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.FluidSyncHelper;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.fluids.util.IAEFluidTank;

public class FluidInterfaceContainer extends FluidConfigurableContainer {

    public static ContainerType<FluidInterfaceContainer> TYPE;

    private static final ContainerHelper<FluidInterfaceContainer, IFluidInterfaceHost> helper = new ContainerHelper<>(
            FluidInterfaceContainer::new, IFluidInterfaceHost.class, SecurityPermissions.BUILD);

    public static FluidInterfaceContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private final DualityFluidInterface myDuality;
    private final FluidSyncHelper tankSync;

    public FluidInterfaceContainer(int id, final PlayerInventory ip, final IFluidInterfaceHost te) {
        super(TYPE, id, ip, te.getDualityFluidInterface().getHost());

        this.myDuality = te.getDualityFluidInterface();
        this.tankSync = new FluidSyncHelper(this.myDuality.getTanks(), DualityFluidInterface.NUMBER_OF_TANKS);
    }

    @Override
    protected int getHeight() {
        return 231;
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
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (isServer()) {
            this.tankSync.sendDiff(this.containerListeners);
        }

        super.broadcastChanges();
    }

    @Override
    protected void setupConfig() {
    }

    @Override
    protected void loadSettingsFromHost(final IConfigManager cm) {
    }

    @Override
    public void addSlotListener(IContainerListener listener) {
        super.addSlotListener(listener);
        this.tankSync.sendFull(Collections.singleton(listener));
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
    public int availableUpgrades() {
        return 0;
    }

    @Override
    public boolean hasToolbox() {
        return false;
    }
}
