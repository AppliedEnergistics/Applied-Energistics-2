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

package appeng.container.me.crafting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableSet;

import net.minecraft.util.text.StringTextComponent;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;

/**
 * Utility class for dialogs that can cycle through crafting CPUs
 */
class CraftingCPUCycler {

    @FunctionalInterface
    public interface ChangeListener {
        void onChange(CraftingCPURecord selectedCpu, boolean cpusAvailable);
    }

    private final Predicate<ICraftingCPU> cpuFilter;
    private final ChangeListener changeListener;
    private final List<CraftingCPURecord> cpus = new ArrayList<>();
    private int selectedCpu = -1;
    private boolean initialDataSent = false;
    private boolean allowNoSelection;

    public CraftingCPUCycler(Predicate<ICraftingCPU> cpuFilter, ChangeListener changeListener) {
        this.cpuFilter = cpuFilter;
        this.changeListener = changeListener;
    }

    public void detectAndSendChanges(IGrid network) {
        final ICraftingGrid cc = network.getService(ICraftingGrid.class);
        final ImmutableSet<ICraftingCPU> cpuSet = cc.getCpus();

        int matches = 0;
        boolean changed = !initialDataSent;
        initialDataSent = true;
        for (final ICraftingCPU c : cpuSet) {
            boolean found = false;
            for (final CraftingCPURecord ccr : this.cpus) {
                if (ccr.getCpu() == c) {
                    found = true;
                    break;
                }
            }

            final boolean matched = this.cpuFilter.test(c);

            if (matched) {
                matches++;
            }

            if (found != matched) {
                changed = true;
            }
        }

        if (changed || this.cpus.size() != matches) {
            this.cpus.clear();
            for (final ICraftingCPU c : cpuSet) {
                if (this.cpuFilter.test(c)) {
                    this.cpus.add(new CraftingCPURecord(c.getAvailableStorage(), c.getCoProcessors(), c));
                }
            }

            // Sort and assign numeric IDs in case they have no names
            Collections.sort(this.cpus);
            for (int i = 0; i < this.cpus.size(); i++) {
                CraftingCPURecord cpu = cpus.get(i);
                if (cpu.getName() == null) {
                    cpu.setName(new StringTextComponent("#" + (i + 1)));
                }
            }

            this.notifyListener();
        }
    }

    public void cycleCpu(final boolean next) {
        if (next) {
            this.selectedCpu++;
        } else {
            this.selectedCpu--;
        }

        // If "no CPU" is a valid selection, then -1 is the first potential item
        int lowerLimit = this.allowNoSelection ? -1 : 0;

        if (this.selectedCpu < lowerLimit) {
            this.selectedCpu = this.cpus.size() - 1;
        } else if (this.selectedCpu >= this.cpus.size()) {
            this.selectedCpu = lowerLimit;
        }

        this.notifyListener();
    }

    public boolean isAllowNoSelection() {
        return allowNoSelection;
    }

    public void setAllowNoSelection(boolean allowNoSelection) {
        this.allowNoSelection = allowNoSelection;
    }

    private void notifyListener() {
        if (this.selectedCpu >= this.cpus.size()) {
            this.selectedCpu = -1;
        }

        // Force the selected CPU to the first available CPU unless no-selection is
        // explicitly allowed
        if (!this.allowNoSelection && this.selectedCpu == -1 && !this.cpus.isEmpty()) {
            this.selectedCpu = 0;
        }

        if (this.selectedCpu != -1) {
            this.changeListener.onChange(this.cpus.get(this.selectedCpu), true);
        } else {
            this.changeListener.onChange(null, !this.cpus.isEmpty());
        }
    }

}
