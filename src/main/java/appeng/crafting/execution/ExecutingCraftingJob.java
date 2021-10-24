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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.crafting.CraftingLink;
import appeng.crafting.inv.ListCraftingInventory;
import appeng.me.service.CraftingService;
import appeng.util.item.AEItemStack;

public class ExecutingCraftingJob {
    private static final String NBT_LINK = "link";
    private static final String NBT_FINAL_OUTPUT = "finalOutput";
    private static final String NBT_WAITING_FOR = "waitingFor";
    private static final String NBT_TIME_TRACKER = "timeTracker";
    private static final String NBT_TASKS = "tasks";
    private static final String NBT_CRAFTING_PROGRESS = "craftingProgress";

    final CraftingLink link;
    final IAEStack finalOutput;
    final ListCraftingInventory waitingFor;
    final Map<IPatternDetails, TaskProgress> tasks = new HashMap<>();
    final ElapsedTimeTracker timeTracker;

    ExecutingCraftingJob(ICraftingPlan plan, Consumer<IAEStack> postCraftingDifference, CraftingLink link) {
        this.finalOutput = IAEStack.copy(plan.finalOutput());
        this.waitingFor = new ListCraftingInventory() {
            @Override
            public void postChange(IAEStack template, long delta) {
                postCraftingDifference.accept(IAEStack.copy(template, delta));
            }
        };

        // Fill waiting for and tasks
        long totalPending = 0;
        for (var stack : plan.emittedItems()) {
            waitingFor.injectItems(stack, Actionable.MODULATE);
            totalPending += stack.getStackSize();
        }
        for (var entry : plan.patternTimes().entrySet()) {
            tasks.computeIfAbsent(entry.getKey(), p -> new TaskProgress()).value += entry.getValue();
            for (var output : entry.getKey().getOutputs()) {
                totalPending += output.getStackSize() * entry.getValue();
            }
        }
        this.timeTracker = new ElapsedTimeTracker(totalPending);
        this.link = link;
    }

    ExecutingCraftingJob(CompoundTag data, Consumer<IAEStack> postCraftingDifference, CraftingCpuLogic cpu) {
        this.link = new CraftingLink(data.getCompound(NBT_LINK), cpu.cluster);
        IGrid grid = cpu.cluster.getGrid();
        if (grid != null) {
            ((CraftingService) grid.getCraftingService()).addLink(link);
        }

        this.finalOutput = GenericStackHelper.readGenericStack(data.getCompound(NBT_FINAL_OUTPUT));
        this.waitingFor = new ListCraftingInventory() {
            @Override
            public void postChange(IAEStack template, long delta) {
                postCraftingDifference.accept(IAEStack.copy(template, delta));
            }
        };
        this.waitingFor.readFromNBT(data.getList(NBT_WAITING_FOR, 10));
        this.timeTracker = new ElapsedTimeTracker(data.getCompound(NBT_TIME_TRACKER));

        ListTag tasksTag = data.getList(NBT_TASKS, 10);
        for (int i = 0; i < tasksTag.size(); ++i) {
            final CompoundTag item = tasksTag.getCompound(i);
            final IAEItemStack pattern = AEItemStack.fromNBT(item);
            var details = PatternDetailsHelper.decodePattern(pattern.createItemStack(),
                    cpu.cluster.getLevel());
            if (details != null) {
                final TaskProgress tp = new TaskProgress();
                tp.value = item.getLong(NBT_CRAFTING_PROGRESS);
                this.tasks.put(details, tp);
            }
        }
    }

    CompoundTag writeToNBT() {
        CompoundTag data = new CompoundTag();

        CompoundTag linkData = new CompoundTag();
        link.writeToNBT(linkData);
        data.put(NBT_LINK, linkData);

        data.put(NBT_FINAL_OUTPUT, GenericStackHelper.writeGenericStack(finalOutput));

        data.put(NBT_WAITING_FOR, waitingFor.writeToNBT());
        data.put(NBT_TIME_TRACKER, timeTracker.writeToNBT());

        final ListTag list = new ListTag();
        for (var e : this.tasks.entrySet()) {
            final CompoundTag item = this.writeItem(AEItemStack.fromItemStack(e.getKey().copyDefinition()));
            item.putLong(NBT_CRAFTING_PROGRESS, e.getValue().value);
            list.add(item);
        }
        data.put(NBT_TASKS, list);

        return data;
    }

    private CompoundTag writeItem(final IAEItemStack stack) {
        final CompoundTag out = new CompoundTag();
        if (stack != null) {
            stack.writeToNBT(out);
        }
        return out;
    }

    static class TaskProgress {
        long value = 0;
    }
}
