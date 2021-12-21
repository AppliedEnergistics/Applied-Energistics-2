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

package appeng.blockentity.misc;

import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;

class CondenserInventory implements MEStorage {
    private final CondenserBlockEntity target;

    CondenserInventory(CondenserBlockEntity te) {
        this.target = te;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        MEStorage.checkPreconditions(what, amount, mode, source);
        if (mode == Actionable.MODULATE) {
            this.target.addPower(amount);
        }
        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        MEStorage.checkPreconditions(what, amount, mode, source);
        var slotItem = this.target.getOutputSlot().getStackInSlot(0);

        if (what instanceof AEItemKey itemKey && itemKey.matches(slotItem)) {
            int count = (int) Math.min(amount, Integer.MAX_VALUE);
            return this.target.getOutputSlot().extractItem(0, count, mode == Actionable.SIMULATE)
                    .getCount();
        }

        return 0;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        var stack = this.target.getOutputSlot().getStackInSlot(0);
        if (!stack.isEmpty()) {
            out.add(AEItemKey.of(stack), stack.getCount());
        }
    }

    @Override
    public Component getDescription() {
        return target.getBlockState().getBlock().getName();
    }
}
