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

package appeng.menu.implementations;

import java.util.Map;
import java.util.Optional;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.helpers.FluidSyncHelper;
import appeng.util.fluid.AEFluidStack;
import appeng.util.fluid.IAEFluidTank;

public abstract class FluidConfigurableMenu<T extends IUpgradeableHost> extends UpgradeableMenu<T>
        implements IFluidSyncMenu {
    private FluidSyncHelper sync = null;

    public FluidConfigurableMenu(MenuType<?> menuType, int id, Inventory ip, T te) {
        super(menuType, id, ip, te);
    }

    public abstract IAEFluidTank getFluidConfigInventory();

    private FluidSyncHelper getSyncHelper() {
        if (this.sync == null) {
            this.sync = new FluidSyncHelper(this.getFluidConfigInventory(), 0);
        }
        return this.sync;
    }

    @Override
    protected ItemStack transferStackToMenu(ItemStack input) {
        Optional<FluidStack> fsOpt = FluidUtil.getFluidContained(input);
        if (fsOpt.isPresent()) {
            final IAEFluidTank t = this.getFluidConfigInventory();
            final IAEFluidStack stack = AEFluidStack.fromFluidStack(fsOpt.orElse(null));
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
            final int upgrades = this.getHost().getInstalledUpgrades(Upgrades.CAPACITY);

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
            this.getSyncHelper().sendDiff(getPlayer());

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
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        this.getSyncHelper().sendFull(getPlayer());
    }

    @Override
    public void receiveFluidSlots(Map<Integer, IAEFluidStack> fluids) {
        this.getSyncHelper().readPacket(fluids);
    }

}
