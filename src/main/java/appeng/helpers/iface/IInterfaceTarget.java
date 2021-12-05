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
import java.util.Set;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.me.storage.StorageAdapter;
import appeng.util.IVariantConversion;

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
        var storage = accessor.getInventory(src);
        if (storage == null) {
            return null;
        } else {
            return new IInterfaceTarget() {
                @Override
                public long insert(AEKey what, long amount, Actionable type) {
                    return storage.insert(what, amount, type, src);
                }

                @Override
                public boolean containsPatternInput(Set<AEKey> patternInputs) {
                    for (var stack : storage.getCachedAvailableStacks()) {
                        if (patternInputs.contains(stack.getKey().dropSecondary())) {
                            return true;
                        }
                    }
                    return false;
                }
            };
        }
    }

    private static IInterfaceTarget wrapHandlers(Storage<ItemVariant> itemHandler, Storage<FluidVariant> fluidHandler,
            IActionSource src) {
        var itemAdapter = new StorageAdapter<>(IVariantConversion.ITEM, itemHandler);
        var fluidAdapter = new StorageAdapter<>(IVariantConversion.FLUID, fluidHandler);
        return new IInterfaceTarget() {
            @Override
            public long insert(AEKey what, long amount, Actionable type) {
                if (what instanceof AEItemKey itemKey) {
                    return itemAdapter.insert(itemKey, amount, type, src);
                }
                if (what instanceof AEFluidKey fluidKey) {
                    return fluidAdapter.insert(fluidKey, amount, type, src);
                }
                return 0;
            }

            @Override
            public boolean containsPatternInput(Set<AEKey> patternInputs) {
                return IInterfaceTarget.canRemove(IVariantConversion.ITEM, itemHandler, patternInputs)
                        || IInterfaceTarget.canRemove(IVariantConversion.FLUID, fluidHandler, patternInputs);
            }
        };
    }

    private static <V extends TransferVariant<?>> boolean canRemove(
            IVariantConversion<V> conversion, Storage<V> storage, Set<AEKey> patternInputs) {
        try (Transaction tx = Transaction.openOuter()) {
            for (var view : storage.iterable(tx)) {
                if (!view.isResourceBlank() && view.getAmount() > 0) {
                    var key = conversion.getKey(view.getResource()).dropSecondary();
                    if (patternInputs.contains(key)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    long insert(AEKey what, long amount, Actionable type);

    boolean containsPatternInput(Set<AEKey> patternInputs);
}
