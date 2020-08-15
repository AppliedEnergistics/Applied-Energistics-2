package appeng.fluids.helper;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alexiil.mc.lib.attributes.fluid.GroupedFluidInv;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.fluids.util.AEFluidStack;

public class GroupedFluidInvCache {

    private final GroupedFluidInv inventory;

    private final Map<FluidKey, IAEFluidStack> memory = new HashMap<>();

    public GroupedFluidInvCache(GroupedFluidInv inventory) {
        this.inventory = inventory;
    }

    public IItemList<IAEFluidStack> getAvailable(IItemList<IAEFluidStack> out) {
        for (Map.Entry<FluidKey, IAEFluidStack> entry : memory.entrySet()) {
            out.add(entry.getValue());
        }
        return out;
    }

    public List<IAEFluidStack> detectChanges() {
        final List<IAEFluidStack> changes = new ArrayList<>();

        Set<FluidKey> storedFluids = inventory.getStoredFluids();
        for (FluidKey storedFluid : storedFluids) {
            IAEFluidStack old = this.memory.get(storedFluid);

            FluidAmount newAmount = inventory.getAmount_F(storedFluid);
            FluidAmount oldAmount = old == null ? FluidAmount.ZERO : old.getAmount();

            if (!newAmount.equals(oldAmount)) {
                AEFluidStack newStack = AEFluidStack.fromFluidVolume(storedFluid.withAmount(newAmount),
                        RoundingMode.DOWN);

                if (old != null) {
                    old = old.copy();
                    old.setStackSize(-old.getStackSize());
                    changes.add(old);
                }

                if (newStack != null) {
                    this.memory.put(storedFluid, newStack);
                    changes.add(newStack);
                } else {
                    this.memory.remove(storedFluid);
                }
            }
        }

        // detect dropped items; should fix non IISided Inventory Changes.
        Set<FluidKey> toRemove = null;
        for (final Map.Entry<FluidKey, IAEFluidStack> entry : memory.entrySet()) {
            if (storedFluids.contains(entry.getKey())) {
                continue; // Still stored
            }

            if (toRemove == null) {
                toRemove = new HashSet<>();
            }
            toRemove.add(entry.getKey());

            final IAEFluidStack a = entry.getValue().copy();
            a.setStackSize(-a.getStackSize());
            changes.add(a);
        }
        // Now clean up if any removed entries were found
        if (toRemove != null) {
            for (FluidKey fluidKey : toRemove) {
                memory.remove(fluidKey);
            }
        }

        return changes;
    }

}
