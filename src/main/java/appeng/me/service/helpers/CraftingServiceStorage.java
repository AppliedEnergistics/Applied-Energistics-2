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

package appeng.me.service.helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellProvider;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
import appeng.me.service.CraftingService;

/**
 * The storage exposed by the crafting service. It does two things:
 * <ul>
 * <li>Report craftable items as craftable to the network, and thus the terminals.</li>
 * <li>Intercept crafted item injections and forward them to the CPUs.</li>
 * </ul>
 */
public class CraftingServiceStorage implements ICellProvider {
    private final CraftingService craftingService;
    private final Map<IStorageChannel<?>, IMEInventoryHandler<?>> inventories = new HashMap<>();

    private <T extends IAEStack> IMEInventoryHandler<T> getOrComputeInventory(IStorageChannel<T> channel) {
        return inventories.computeIfAbsent(channel, ignored -> new IMEInventoryHandler<T>() {
            @Override
            public AccessRestriction getAccess() {
                return AccessRestriction.WRITE;
            }

            @Override
            public boolean isPrioritized(IAEStack input) {
                return true;
            }

            @Override
            public boolean canAccept(IAEStack input) {
                return craftingService.isRequesting(input);
            }

            @Override
            public int getPriority() {
                return Integer.MAX_VALUE;
            }

            @Override
            public boolean validForPass(int pass) {
                return pass == 1;
            }

            @Override
            public T injectItems(T input, Actionable type, IActionSource src) {
                // Item interception logic
                var result = craftingService.injectItemsIntoCpus(input, type);
                if (result != null) {
                    return result.cast(channel);
                }
                return null;
            }

            @Override
            public T extractItems(T request, Actionable mode, IActionSource src) {
                return null;
            }

            @Override
            public IAEStackList<T> getAvailableStacks(IAEStackList<T> out) {
                // Add craftable items so the terminals can see them.
                return craftingService.addCrafting(channel, out);
            }

            @Override
            public IStorageChannel<T> getChannel() {
                return channel;
            }
        }).cast(channel);
    }

    public CraftingServiceStorage(CraftingService craftingService) {
        this.craftingService = craftingService;
    }

    @Nonnull
    @Override
    public <T extends IAEStack> List<IMEInventoryHandler<T>> getCellArray(IStorageChannel<T> channel) {
        return List.of(getOrComputeInventory(channel));
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }
}
