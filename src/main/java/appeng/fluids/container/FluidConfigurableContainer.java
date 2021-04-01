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

package appeng.fluids.container;

import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.container.implementations.UpgradeableContainer;
import appeng.fluids.helper.FluidSyncHelper;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidTank;

public abstract class FluidConfigurableContainer extends UpgradeableContainer implements IFluidSyncContainer {
    private FluidSyncHelper sync = null;

    public FluidConfigurableContainer(ContainerType<?> containerType, int id, PlayerInventory ip, IUpgradeableHost te) {
        super(containerType, id, ip, te);
    }

    public abstract IAEFluidTank getFluidConfigInventory();

    private FluidSyncHelper getSyncHelper() {
        if (this.sync == null) {
            this.sync = new FluidSyncHelper(this.getFluidConfigInventory(), 0);
        }
        return this.sync;
    }

    @Override
    protected ItemStack transferStackToContainer(ItemStack input) {
        FluidVolume fluid = FluidAttributes.EXTRACTABLE.get(input).attemptAnyExtraction(FluidAmount.MAX_VALUE,
                Simulation.ACTION);

        if (!fluid.isEmpty()) {
            final IAEFluidTank t = this.getFluidConfigInventory();
            final IAEFluidStack stack = AEFluidStack.fromFluidVolume(fluid, RoundingMode.DOWN);
            for (int i = 0; i < t.getSlots(); ++i) {
                if (t.getFluidInSlot(i) == null && this.isValidForConfig(i, stack)) {
                    t.setFluidInSlot(i, stack);
                    break;
                }
            }
        }
        return input;
    }

    protected boolean isValidForConfig(int slot, IAEFluidStack fs) {
        if (this.supportCapacity()) {
            // assumes 4 slots per upgrade
            final int upgrades = this.getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY);

            if (slot > 0 && upgrades < 1) {
                return false;
            }
            if (slot > 4 && upgrades < 2) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void standardDetectAndSendChanges() {
        if (isServer()) {
            this.getSyncHelper().sendDiff(this.listeners);

            // clear out config items that are no longer valid (eg capacity upgrade removed)
            final IAEFluidTank t = this.getFluidConfigInventory();
            for (int i = 0; i < t.getSlots(); ++i) {
                if (t.getFluidInSlot(i) != null && !this.isValidForConfig(i, t.getFluidInSlot(i))) {
                    t.setFluidInSlot(i, null);
                }
            }
        }
        super.standardDetectAndSendChanges();
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        this.getSyncHelper().sendFull(Collections.singleton(listener));
    }

    @Override
    public void receiveFluidSlots(Map<Integer, IAEFluidStack> fluids) {
        this.getSyncHelper().readPacket(fluids);
    }

}
