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

import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.capabilities.Capabilities;
import appeng.me.storage.ExternalStorageFacade;

public interface IInterfaceTarget {
    @Nullable
    static IInterfaceTarget get(Level l, BlockPos pos, @Nullable BlockEntity be, Direction side, IActionSource src) {
        if (be == null)
            return null;

        // our capability first: allows any storage channel
        var accessor = be.getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, side).orElse(null);
        if (accessor != null) {
            return wrapStorageMonitorable(accessor, src);
        }

        // otherwise fall back to the platform capability
        // TODO: look into exposing this for other storage channels
        var itemHandler = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
        var fluidHandler = be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);

        if (itemHandler.isPresent() || fluidHandler.isPresent()) {
            return wrapHandlers(
                    itemHandler.orElse(EmptyHandler.INSTANCE),
                    fluidHandler.orElse(EmptyFluidHandler.INSTANCE),
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
                    for (var stack : storage.getAvailableStacks()) {
                        if (patternInputs.contains(stack.getKey().dropSecondary())) {
                            return true;
                        }
                    }
                    return false;
                }
            };
        }
    }

    private static IInterfaceTarget wrapHandlers(IItemHandler itemHandler, IFluidHandler fluidHandler,
            IActionSource src) {
        var itemAdapter = ExternalStorageFacade.of(itemHandler);
        var fluidAdapter = ExternalStorageFacade.of(fluidHandler);
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
                return itemAdapter.containsAnyFuzzy(patternInputs) || fluidAdapter.containsAnyFuzzy(patternInputs);
            }
        };
    }

    long insert(AEKey what, long amount, Actionable type);

    boolean containsPatternInput(Set<AEKey> patternInputs);
}
