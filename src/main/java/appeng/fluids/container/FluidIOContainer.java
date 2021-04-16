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

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;

import appeng.api.config.SecurityPermissions;
import appeng.container.implementations.ContainerTypeBuilder;
import appeng.fluids.parts.FluidExportBusPart;
import appeng.fluids.parts.FluidImportBusPart;
import appeng.fluids.parts.SharedFluidBusPart;
import appeng.fluids.util.IAEFluidTank;

/**
 * @author BrockWS
 * @version rv5 - 1/05/2018
 * @since rv5 1/05/2018
 */
public class FluidIOContainer extends FluidConfigurableContainer {

    public static final ContainerType<FluidIOContainer> EXPORT_TYPE = ContainerTypeBuilder
            .create(FluidIOContainer::new, FluidExportBusPart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("fluid_import_bus");

    public static final ContainerType<FluidIOContainer> IMPORT_TYPE = ContainerTypeBuilder
            .create(FluidIOContainer::new, FluidImportBusPart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("fluid_export_bus");

    private final SharedFluidBusPart bus;

    public FluidIOContainer(ContainerType<?> containerType, int id, PlayerInventory ip, SharedFluidBusPart bus) {
        super(containerType, id, ip, bus);
        this.bus = bus;
    }

    @Override
    public IAEFluidTank getFluidConfigInventory() {
        return this.bus.getConfig();
    }

    @Override
    protected void setupConfig() {
        this.setupUpgrades();
    }
}
