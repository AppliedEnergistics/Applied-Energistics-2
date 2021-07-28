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

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Upgrades;
import appeng.api.storage.data.IAEFluidStack;
import appeng.container.implementations.ContainerTypeBuilder;
import appeng.fluids.parts.FluidFormationPlanePart;
import appeng.fluids.util.IAEFluidTank;

public class FluidFormationPlaneContainer extends FluidConfigurableContainer {

    public static final MenuType<FluidFormationPlaneContainer> TYPE = ContainerTypeBuilder
            .create(FluidFormationPlaneContainer::new, FluidFormationPlanePart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("fluid_formation_plane");

    private final FluidFormationPlanePart plane;

    public FluidFormationPlaneContainer(int id, final Inventory ip, final FluidFormationPlanePart te) {
        super(TYPE, id, ip, te);
        this.plane = te;
    }

    @Override
    public IAEFluidTank getFluidConfigInventory() {
        return this.plane.getConfig();
    }

    @Override
    protected void setupConfig() {
        this.setupUpgrades();
    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);
        this.checkToolbox();
        this.standardDetectAndSendChanges();
    }

    @Override
    protected boolean isValidForConfig(int slot, IAEFluidStack fs) {
        if (this.supportCapacity()) {
            final int upgrades = this.getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY);

            final int y = slot / 9;

            if (y >= upgrades + 2) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        final int upgrades = this.getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY);

        return upgrades > idx;
    }

    @Override
    protected boolean supportCapacity() {
        return true;
    }

    @Override
    public int availableUpgrades() {
        return 5;
    }
}
