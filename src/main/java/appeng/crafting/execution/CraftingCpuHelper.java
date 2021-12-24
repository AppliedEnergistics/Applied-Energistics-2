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

import javax.annotation.Nullable;

import com.google.common.collect.Iterables;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.inv.ICraftingInventory;
import appeng.crafting.inv.ListCraftingInventory;

/**
 * Helper functions used by the CPU.
 */
public class CraftingCpuHelper {
    public static boolean tryExtractInitialItems(ICraftingPlan plan, IGrid grid,
            ListCraftingInventory cpuInventory, IActionSource src) {
        var storage = grid.getStorageService().getInventory();

        for (var entry : plan.usedItems()) {
            var what = entry.getKey();
            var toExtract = entry.getLongValue();
            var extracted = storage.extract(what, toExtract, Actionable.MODULATE, src);
            cpuInventory.insert(what, extracted, Actionable.MODULATE);

            if (extracted < toExtract) {
                // Failed to extract everything, reinject and hope for the best.
                // TODO: maybe voiding items that fail to re-insert is not the best thing to do?
                for (var stored : cpuInventory.list) {
                    storage.insert(stored.getKey(), stored.getLongValue(), Actionable.MODULATE, src);
                }
                cpuInventory.clear();

                return false;
            }
        }

        return true;
    }

    public static CompoundTag generateLinkData(String craftingID, boolean standalone, boolean req) {
        final CompoundTag tag = new CompoundTag();

        tag.putString("CraftID", craftingID);
        tag.putBoolean("canceled", false);
        tag.putBoolean("done", false);
        tag.putBoolean("standalone", standalone);
        tag.putBoolean("req", req);

        return tag;
    }

    public static double calculatePatternPower(KeyCounter[] craftingContainer) {
        // Calculate power.
        double sum = 0;

        for (var itemHolder : craftingContainer) {
            for (var anInput : itemHolder) {
                sum += ((double) anInput.getLongValue()) / ((double) anInput.getKey().getAmountPerOperation());
            }
        }

        return sum;
    }

    @Nullable
    public static KeyCounter[] extractPatternInputs(
            IPatternDetails details,
            ICraftingInventory sourceInv,
            Level level,
            KeyCounter expectedOutputs) {

        // Extract inputs into the container.
        var inputs = details.getInputs();
        @SuppressWarnings("unchecked")
        KeyCounter[] inputHolder = new KeyCounter[inputs.length];
        boolean found = true;

        for (int x = 0; x < inputs.length; x++) {
            var list = inputHolder[x] = new KeyCounter();
            long remainingMultiplier = inputs[x].getMultiplier();
            for (var template : getValidItemTemplates(sourceInv, inputs[x], level)) {
                long extracted = extractTemplates(sourceInv, template, remainingMultiplier);
                list.add(template.key(), extracted * template.amount());

                // Container items!
                var containerItem = inputs[x].getContainerItem(template.key());
                if (containerItem != null) {
                    expectedOutputs.add(containerItem, extracted);
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
            expectedOutputs.add(output.what(), output.amount());
        }

        return inputHolder;
    }

    public static void reinjectPatternInputs(ICraftingInventory sourceInv,
            KeyCounter[] inputHolder) {
        for (var list : inputHolder) {
            // List may be null if we failed to extract some of the pattern's inputs.
            if (list != null) {
                for (var entry : list) {
                    sourceInv.insert(entry.getKey(), entry.getLongValue(), Actionable.MODULATE);
                }
            }
        }
    }

    /**
     * Get all potential input templates that count as "1" ingredient according to the given inputs for a pattern slot,
     * and which are available.
     */
    public static Iterable<InputTemplate> getValidItemTemplates(ICraftingInventory inv,
            IPatternDetails.IInput input, Level level) {
        var possibleInputs = input.getPossibleInputs();

        var substitutes = new ArrayList<InputTemplate>(possibleInputs.length);

        for (var stack : possibleInputs) {
            for (var fuzz : inv.findFuzzyTemplates(stack.what())) {
                substitutes.add(new InputTemplate(fuzz, stack.amount()));
            }
        }

        return Iterables.filter(substitutes, stack -> input.isValid(stack.key(), level));
    }

    /**
     * Extract a whole number of templates, and return how many were extracted.
     */
    public static long extractTemplates(ICraftingInventory inv, InputTemplate template, long multiplier) {
        long maxTotal = template.amount() * multiplier;
        // Extract as much as possible.
        var extracted = inv.extract(template.key(), maxTotal, Actionable.SIMULATE);
        if (extracted == 0)
            return 0;
        // Adjust to have a whole number of templates.
        multiplier = extracted / template.amount();
        maxTotal = template.amount() * multiplier;
        if (maxTotal == 0)
            return 0;
        extracted = inv.extract(template.key(), maxTotal, Actionable.MODULATE);
        if (extracted == 0 || extracted != maxTotal) {
            throw new IllegalStateException("Failed to correctly extract whole number. Invalid simulation!");
        }
        return multiplier;
    }

    private CraftingCpuHelper() {
    }
}
