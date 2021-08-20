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

package appeng.helpers.iface;

import java.util.Objects;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEStack;
import appeng.crafting.execution.GenericStackHelper;
import appeng.me.storage.FluidHandlerAdapter;
import appeng.me.storage.ItemHandlerAdapter;

public interface IInterfaceTarget {
    @Nullable
    static IInterfaceTarget get(Level l, BlockPos pos, @Nullable BlockEntity be, Direction side, IActionSource src) {
        if (be == null)
            return null;

        // our capability first: allows any storage channel
        var accessor = IStorageMonitorableAccessor.SIDED.find(l, pos, null, be, side);
        if (accessor != null) {
            return wrapStorageMonitorable(accessor, src);
        }

        // otherwise fall back to the platform capability
        // TODO: look into exposing this for other storage channels
        var itemHandler = ItemStorage.SIDED.find(l, pos, null, be, side);
        var fluidHandler = FluidStorage.SIDED.find(l, pos, null, be, side);

        if (itemHandler != null || fluidHandler != null) {
            return wrapHandlers(
                    Objects.requireNonNullElse(itemHandler, Storage.empty()),
                    Objects.requireNonNullElse(fluidHandler, Storage.empty()),
                    src);
        }

        return null;
    }

    private static IInterfaceTarget wrapStorageMonitorable(IStorageMonitorableAccessor accessor, IActionSource src) {
        var monitorable = accessor.getInventory(src);
        if (monitorable == null) {
            return null;
        } else {
            return new IInterfaceTarget() {
                @Nullable
                @Override
                public IAEStack injectItems(IAEStack what, Actionable type) {
                    return GenericStackHelper.injectMonitorable(monitorable, what, type, src);
                }

                @Override
                public boolean isBusy() {
                    for (var channel : StorageChannels.getAll()) {
                        if (IInterfaceTarget.isChannelBusy(channel, monitorable, src)) {
                            return true;
                        }
                    }
                    return false;
                }
            };
        }
    }

    private static <T extends IAEStack> boolean isChannelBusy(IStorageChannel<T> channel,
            IStorageMonitorable monitorable, IActionSource src) {
        var inventory = monitorable.getInventory(channel);
        if (inventory != null) {
            for (var stack : inventory.getStorageList()) {
                if (inventory.extractItems(stack, Actionable.SIMULATE, src) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private static IInterfaceTarget wrapHandlers(Storage<ItemVariant> itemHandler, Storage<FluidVariant> fluidHandler,
            IActionSource src) {
        var itemAdapter = new ItemHandlerAdapter(itemHandler) {
            @Override
            protected void onInjectOrExtract() {
            }
        };
        var fluidAdapter = new FluidHandlerAdapter(fluidHandler, false) {
            @Override
            protected void onInjectOrExtract() {
            }
        };
        return new IInterfaceTarget() {
            @Nullable
            @Override
            public IAEStack injectItems(IAEStack what, Actionable type) {
                if (what != null && what.getChannel() == StorageChannels.items()) {
                    return itemAdapter.injectItems(what.cast(StorageChannels.items()), type, src);
                }
                if (what != null && what.getChannel() == StorageChannels.fluids()) {
                    return fluidAdapter.injectItems(what.cast(StorageChannels.fluids()), type, src);
                }
                return null;
            }

            @Override
            public boolean isBusy() {
                return IInterfaceTarget.canRemove(itemHandler) || IInterfaceTarget.canRemove(fluidHandler);
            }
        };
    }

    private static <T> boolean canRemove(Storage<T> storage) {
        try (Transaction tx = Transaction.openOuter()) {
            for (var view : storage.iterable(tx)) {
                if (!view.isResourceBlank() && view.extract(view.getResource(), Long.MAX_VALUE, tx) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    IAEStack injectItems(IAEStack what, Actionable type);

    boolean isBusy();
}
