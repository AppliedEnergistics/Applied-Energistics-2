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


import appeng.api.storage.ISpatialDimension;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import com.jaquadro.minecraft.storagedrawers.api.capabilities.IItemRepository;
import gregtech.api.capability.IEnergyContainer;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.energy.IEnergyStorage;


/**
 * Utility class that holds various capabilities, both by AE2 and other Mods.
 */
public final class Capabilities {

    private Capabilities() {
    }

    public static Capability<IStorageMonitorableAccessor> STORAGE_MONITORABLE_ACCESSOR;

    public static Capability<ISpatialDimension> SPATIAL_DIMENSION;

    public static Capability<ITeslaConsumer> TESLA_CONSUMER;

    public static Capability<ITeslaHolder> TESLA_HOLDER;

    public static Capability<IEnergyStorage> FORGE_ENERGY;

    public static Capability<IItemRepository> ITEM_REPOSITORY_CAPABILITY;

    public static Capability<IEnergyContainer> GTCE_ENERGY;

    /**
     * Register AE2 provided capabilities.
     */
    public static void register() {
        CapabilityManager.INSTANCE.register(IStorageMonitorableAccessor.class, createNullStorage(), NullMENetworkAccessor::new);
        CapabilityManager.INSTANCE.register(ISpatialDimension.class, createNullStorage(), NullSpatialDimension::new);
    }

    @CapabilityInject(IStorageMonitorableAccessor.class)
    private static void capIStorageMonitorableAccessorRegistered(Capability<IStorageMonitorableAccessor> cap) {
        STORAGE_MONITORABLE_ACCESSOR = cap;
    }

    @CapabilityInject(ISpatialDimension.class)
    private static void capISpatialDimensionRegistered(Capability<ISpatialDimension> cap) {
        SPATIAL_DIMENSION = cap;
    }

    @CapabilityInject(ITeslaConsumer.class)
    private static void capITeslaConsumerRegistered(Capability<ITeslaConsumer> cap) {
        if (IntegrationRegistry.INSTANCE.isEnabled(IntegrationType.TESLA)) {
            TESLA_CONSUMER = cap;
        }
    }

    @CapabilityInject(ITeslaHolder.class)
    private static void capITeslaHolderRegistered(Capability<ITeslaHolder> cap) {
        if (IntegrationRegistry.INSTANCE.isEnabled(IntegrationType.TESLA)) {
            TESLA_HOLDER = cap;
        }
    }

    @CapabilityInject(IEnergyStorage.class)
    private static void capIEnergyStorageRegistered(Capability<IEnergyStorage> cap) {
        FORGE_ENERGY = cap;
    }

    @CapabilityInject(IItemRepository.class)
    private static void capIItemRepositoryRegistered(Capability<IItemRepository> cap) {
        ITEM_REPOSITORY_CAPABILITY = cap;
    }

    @CapabilityInject(IEnergyContainer.class)
    private static void capIEnergyContainerRegistered(Capability<IEnergyContainer> cap) {
        if (IntegrationRegistry.INSTANCE.isEnabled(IntegrationType.GTCE)) {
            GTCE_ENERGY = cap;
        }
    }

    // Create a storage implementation that does not do anything
    private static <T> Capability.IStorage<T> createNullStorage() {
        return new Capability.IStorage<T>() {
            @Override
            public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
                return null;
            }

            @Override
            public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {

            }
        };
    }
}
