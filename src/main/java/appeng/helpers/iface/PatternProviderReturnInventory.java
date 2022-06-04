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

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.capabilities.Capabilities;
import appeng.helpers.externalstorage.GenericStackInv;

public class PatternProviderReturnInventory extends GenericStackInv {
    public static int NUMBER_OF_SLOTS = 9;

    /**
     * Used to prevent injection through the handlers when we are pushing items out in the network. Otherwise, a storage
     * bus on the pattern provider could potentially void items.
     */
    private boolean injectingIntoNetwork = false;
    private final LazyOptional<GenericInternalInventory> genericInternalInventory = LazyOptional.of(() -> this);

    public PatternProviderReturnInventory(Runnable listener) {
        super(listener, NUMBER_OF_SLOTS);

        useRegisteredCapacities();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canInsert() {
        return !injectingIntoNetwork;
    }

    /**
     * Return true if something could be injected into the network.
     */
    public boolean injectIntoNetwork(MEStorage storage, IActionSource src) {
        var didSomething = false;
        injectingIntoNetwork = true;

        try {
            for (int i = 0; i < stacks.length; ++i) {
                GenericStack stack = stacks[i];
                if (stack != null) {
                    long sizeBefore = stack.amount();
                    var inserted = storage.insert(stack.what(), stack.amount(), Actionable.MODULATE, src);
                    if (inserted >= stack.amount()) {
                        stacks[i] = null;
                    } else {
                        stacks[i] = new GenericStack(stack.what(), stack.amount() - inserted);
                    }

                    if (GenericStack.getStackSizeOrZero(stacks[i]) != sizeBefore) {
                        didSomething = true;
                    }
                }
            }
        } finally {
            injectingIntoNetwork = false;
        }

        return didSomething;
    }

    public void addDrops(List<ItemStack> drops, Level level, BlockPos pos) {
        for (var stack : stacks) {
            if (stack != null) {
                stack.what().addDrops(stack.amount(), drops, level, pos);
            }
        }
    }

    public <T> LazyOptional<T> getCapability(Capability<T> capability) {
        if (capability == Capabilities.GENERIC_INTERNAL_INV) {
            return genericInternalInventory.cast();
        } else {
            return LazyOptional.empty();
        }
    }
}
