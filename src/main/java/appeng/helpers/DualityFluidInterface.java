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

package appeng.helpers;

import java.util.Map;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.AEKeySpace;
import appeng.api.storage.MEMonitorStorage;
import appeng.api.storage.data.AEFluidKey;
import appeng.api.storage.data.AEKey;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalBlockPos;
import appeng.helpers.iface.GenericStackInvStorage;
import appeng.me.storage.CompositeStorage;
import appeng.me.storage.StorageAdapter;
import appeng.util.IVariantConversion;

public class DualityFluidInterface extends DualityInterface {
    public static final int NUMBER_OF_TANKS = 6;
    public static final long TANK_CAPACITY = 4 * AEFluidKey.AMOUNT_BUCKET;

    @Nullable
    private InterfaceInventory localInvHandler;

    private final GenericStackInvStorage<FluidVariant> localStorage;

    public DualityFluidInterface(IManagedGridNode gridNode, IFluidInterfaceHost ih, ItemStack is) {
        super(gridNode, ih, is, AEFluidKey::is);
        getConfig().setCapacity(TANK_CAPACITY);
        getStorage().setCapacity(TANK_CAPACITY);
        this.localStorage = GenericStackInvStorage.fluids(getStorage());
    }

    /**
     * Returns an ME compatible monitor for the interfaces local storage.
     */
    @Override
    protected MEMonitorStorage getLocalInventory() {
        if (localInvHandler == null) {
            localInvHandler = new InterfaceInventory();
        }
        return localInvHandler;
    }

    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    public DimensionalBlockPos getLocation() {
        return new DimensionalBlockPos(this.host.getBlockEntity());
    }

    @Override
    protected int getStorageSlots() {
        return NUMBER_OF_TANKS;
    }

    public GenericStackInvStorage<FluidVariant> getLocalStorage() {
        return localStorage;
    }

    private class InterfaceInventory extends CompositeStorage {
        public InterfaceInventory() {
            super(Map.of(
                    AEKeySpace.fluids(), new StorageAdapter<>(IVariantConversion.FLUID, localStorage)));
            this.setActionSource(actionSource);
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (getRequestInterfacePriority(source).isPresent()) {
                return 0;
            }

            var inserted = super.insert(what, amount, mode, source);
            onTick(); // Immediately clear cache
            return inserted;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            var requestPriority = getRequestInterfacePriority(source);
            if (requestPriority.isPresent() && requestPriority.getAsInt() <= getPriority()) {
                return 0;
            }

            var extracted = super.extract(what, amount, mode, source);
            onTick(); // Immediately clear cache
            return extracted;
        }

        @Override
        public Component getDescription() {
            return host.getMainMenuIcon().getHoverName();
        }
    }

}
