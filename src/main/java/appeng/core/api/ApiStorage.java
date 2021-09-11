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

package appeng.core.api;

import com.google.common.base.Preconditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageHelper;
import appeng.api.storage.data.IAEStack;
import appeng.crafting.CraftingLink;
import appeng.util.Platform;

public class ApiStorage implements IStorageHelper {

    @Override
    public ICraftingLink loadCraftingLink(final CompoundTag data, final ICraftingRequester req) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(req);

        return new CraftingLink(data, req);
    }

    @Override
    public <T extends IAEStack> T poweredInsert(IEnergySource energy, IMEInventory<T> inv, T input,
            IActionSource src, Actionable mode) {
        return Platform.poweredInsert(energy, inv, input, src, mode);
    }

    @Override
    public <T extends IAEStack> T poweredExtraction(IEnergySource energy, IMEInventory<T> inv, T request,
            IActionSource src, Actionable mode) {
        return Platform.poweredExtraction(energy, inv, request, src, mode);
    }

    @Override
    public void postChanges(IStorageService gs, ItemStack removedCell, ItemStack addedCell, IActionSource src) {
        Preconditions.checkNotNull(gs);
        Preconditions.checkNotNull(removedCell);
        Preconditions.checkNotNull(addedCell);
        Preconditions.checkNotNull(src);

        Platform.postWholeCellChanges(gs, removedCell, addedCell, src);
    }

}
