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

package appeng.menu.me.crafting;

import net.minecraft.network.FriendlyByteBuf;

import appeng.api.storage.data.IAEStack;
import appeng.crafting.execution.GenericStackHelper;

/**
 * Describes an entry in the crafting plan which describes how many items of one type are missing, already stored in the
 * network, or have to be crafted.
 */
public class CraftingPlanSummaryEntry {
    private final IAEStack stack;
    private final long missingAmount;
    private final long storedAmount;
    private final long craftAmount;

    public CraftingPlanSummaryEntry(IAEStack stack, long missingAmount, long storedAmount, long craftAmount) {
        this.stack = stack;
        this.missingAmount = missingAmount;
        this.storedAmount = storedAmount;
        this.craftAmount = craftAmount;
    }

    public IAEStack getStack() {
        return stack;
    }

    public long getMissingAmount() {
        return missingAmount;
    }

    public long getStoredAmount() {
        return storedAmount;
    }

    public long getCraftAmount() {
        return craftAmount;
    }

    public void write(FriendlyByteBuf buffer) {
        GenericStackHelper.writeGenericStack(buffer, stack);
        buffer.writeVarLong(missingAmount);
        buffer.writeVarLong(storedAmount);
        buffer.writeVarLong(craftAmount);
    }

    public static CraftingPlanSummaryEntry read(FriendlyByteBuf buffer) {
        IAEStack stack = GenericStackHelper.readGenericStack(buffer);
        long missingAmount = buffer.readVarLong();
        long storedAmount = buffer.readVarLong();
        long craftAmount = buffer.readVarLong();
        return new CraftingPlanSummaryEntry(stack, missingAmount, storedAmount, craftAmount);
    }

}
