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

package appeng.crafting.execution;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Iterables;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.MixedStackList;
import appeng.crafting.inv.ICraftingInventory;
import appeng.crafting.inv.ListCraftingInventory;

/**
 * Helper functions used by the CPU.
 */
public class CraftingCpuHelper {
    public static boolean tryExtractInitialItems(ICraftingPlan plan, IGrid grid,
            ListCraftingInventory cpuInventory, IActionSource src) {
        var storageService = grid.getStorageService();

        for (var toExtract : plan.usedItems()) {
            var extracted = GenericStackHelper.extractMonitorable(storageService, toExtract, Actionable.MODULATE, src);
            cpuInventory.injectItems(extracted, Actionable.MODULATE);

            if (IAEStack.getStackSizeOrZero(extracted) < toExtract.getStackSize()) {
                // Failed to extract everything, reinject and hope for the best.
                // TODO: maybe voiding items that fail to re-insert is not the best thing to do?
                for (var stored : cpuInventory.list) {
                    GenericStackHelper.injectMonitorable(storageService, stored, Actionable.MODULATE, src);
                    stored.reset();
                }

                return false;
            }
        }

        return true;
    }

    public static CompoundTag generateLinkData(final String craftingID, final boolean standalone, final boolean req) {
        final CompoundTag tag = new CompoundTag();

        tag.putString("CraftID", craftingID);
        tag.putBoolean("canceled", false);
        tag.putBoolean("done", false);
        tag.putBoolean("standalone", standalone);
        tag.putBoolean("req", req);

        return tag;
    }

    public static boolean extractPatternPower(
            IPatternDetails details,
            IEnergyService energyService,
            Actionable type) {
        // Consume power.
        double sum = 0;

        for (var anInput : details.getInputs()) {
            if (anInput != null) {
                sum += anInput.getMultiplier();
            }
        }

        return energyService.extractAEPower(sum, type, PowerMultiplier.CONFIG) >= sum - 0.01;
    }

    @Nullable
    public static MixedStackList[] extractPatternInputs(
            IPatternDetails details,
            ICraftingInventory sourceInv,
            IEnergyService energyService,
            Level level,
            MixedStackList expectedOutputs) {
        // Check energy first.
        if (!extractPatternPower(details, energyService, Actionable.SIMULATE))
            return null;

        // Extract inputs into the container.
        var inputs = details.getInputs();
        MixedStackList[] inputHolder = new MixedStackList[inputs.length];
        boolean found = true;

        for (int x = 0; x < inputs.length; x++) {
            MixedStackList list = inputHolder[x] = new MixedStackList();
            long remainingMultiplier = inputs[x].getMultiplier();
            for (var template : getValidItemTemplates(sourceInv, inputs[x], level)) {
                long extracted = extractTemplates(sourceInv, template, remainingMultiplier);
                list.add(IAEStack.copy(template, template.getStackSize() * extracted));

                // Container items!
                var containerItem = inputs[x].getContainerItem(template);
                if (containerItem != null) {
                    expectedOutputs.add(IAEStack.copy(containerItem, containerItem.getStackSize() * extracted));
                }

                remainingMultiplier -= extracted;
                if (remainingMultiplier == 0)
                    break;
            }

            if (remainingMultiplier > 0) {
                found = false;
                break;
            }
        }

        // Failed to extract everything, put it back!
        if (!found) {
            // put stuff back..
            reinjectPatternInputs(sourceInv, inputHolder);
            return null;
        }

        // Add pattern outputs.
        for (var output : details.getOutputs()) {
            expectedOutputs.addStorage(output);
        }

        return inputHolder;
    }

    public static void reinjectPatternInputs(ICraftingInventory sourceInv,
            MixedStackList[] inputHolder) {
        for (var list : inputHolder) {
            // List may be null if we failed to extract some of the pattern's inputs.
            if (list != null) {
                for (var stack : list) {
                    sourceInv.injectItems(stack, Actionable.MODULATE);
                }
            }
        }
    }

    /**
     * Get all stack templates that can be used for this pattern's input.
     */
    public static Iterable<IAEStack> getValidItemTemplates(ICraftingInventory inv,
            IPatternDetails.IInput input, Level level) {
        IAEStack[] possibleInputs = input.getPossibleInputs();

        List<IAEStack> substitutes = new ArrayList<>(possibleInputs.length);

        for (var stack : possibleInputs) {
            for (var fuzz : inv.findFuzzyTemplates(stack)) {
                // Set the correct amount, it has to match that of the template!
                var fuzzCopy = IAEStack.copy(fuzz, stack.getStackSize());
                substitutes.add(fuzzCopy);
            }
        }

        return Iterables.filter(substitutes, stack -> input.isValid(stack, level));
    }

    /**
     * Extract a whole number of templates, and return how many were extracted.
     */
    public static long extractTemplates(ICraftingInventory inv, IAEStack template, long multiplier) {
        long maxTotal = template.getStackSize() * multiplier;
        // Extract as much as possible.
        var extracted = inv.extractItems(IAEStack.copy(template, maxTotal), Actionable.SIMULATE);
        if (extracted == null)
            return 0;
        // Adjust to have a whole number of templates.
        multiplier = extracted.getStackSize() / template.getStackSize();
        maxTotal = template.getStackSize() * multiplier;
        if (maxTotal == 0)
            return 0;
        extracted = inv.extractItems(IAEStack.copy(template, maxTotal), Actionable.MODULATE);
        if (extracted == null || extracted.getStackSize() != maxTotal) {
            throw new IllegalStateException("Failed to correctly extract whole number. Invalid simulation!");
        }
        return multiplier;
    }

    private CraftingCpuHelper() {
    }
}
