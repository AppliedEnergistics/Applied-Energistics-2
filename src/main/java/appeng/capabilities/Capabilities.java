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

package appeng.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.energy.IEnergyStorage;

import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.storage.IStorageMonitorableAccessor;

/**
 * Utility class that holds various capabilities, both by AE2 and other Mods.
 */
public final class Capabilities {
    private Capabilities() {
    }

    public static Capability<IStorageMonitorableAccessor> STORAGE_MONITORABLE_ACCESSOR;

    public static Capability<ICraftingMachine> CRAFTING_MACHINE;

    public static Capability<IEnergyStorage> FORGE_ENERGY;

    @CapabilityInject(IStorageMonitorableAccessor.class)
    private static void capIStorageMonitorableAccessorRegistered(Capability<IStorageMonitorableAccessor> cap) {
        STORAGE_MONITORABLE_ACCESSOR = cap;
    }

    @CapabilityInject(ICraftingMachine.class)
    private static void capICraftingMachine(Capability<ICraftingMachine> cap) {
        CRAFTING_MACHINE = cap;
    }

    @CapabilityInject(IEnergyStorage.class)
    private static void capIEnergyStorageRegistered(Capability<IEnergyStorage> cap) {
        FORGE_ENERGY = cap;
    }
}
