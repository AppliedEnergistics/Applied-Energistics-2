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

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import appeng.api.config.Actionable;
import appeng.api.inventories.PlatformInventoryWrapper;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEStack;
import appeng.capabilities.Capabilities;
import appeng.crafting.execution.GenericStackHelper;
import appeng.me.storage.FluidHandlerAdapter;
import appeng.me.storage.ItemHandlerAdapter;

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

    private static IInterfaceTarget wrapHandlers(IItemHandler itemHandler, IFluidHandler fluidHandler,
            IActionSource src) {
        var itemAdapter = new ItemHandlerAdapter(itemHandler) {
            @Override
            protected void onInjectOrExtract() {
            }
        };
        var fluidAdapter = new FluidHandlerAdapter(fluidHandler) {
            @Override
            protected void onInjectOrExtract() {
            }
        };
        var adaptor = new PlatformInventoryWrapper(itemHandler);
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
                return !adaptor.simulateRemove(1, ItemStack.EMPTY, null).isEmpty() ||
                        !fluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE).isEmpty();
            }
        };
    }

    @Nullable
    IAEStack injectItems(IAEStack what, Actionable type);

    boolean isBusy();
}
