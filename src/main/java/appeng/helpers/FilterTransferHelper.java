package appeng.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.math.LongMath;
import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.FakeSlot;

public class FilterTransferHelper<T extends UpgradeableMenu<? extends IUpgradeableObject>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void addOrMerge(List<GenericStack> stacks, GenericStack newStack) {
        for (int i = 0; i < stacks.size(); i++) {
            var existingStack = stacks.get(i);
            if (Objects.equals(existingStack.what(), newStack.what())) {
                // Add the new amount onto the existing amount
                long newAmount = LongMath.saturatedAdd(existingStack.amount(), newStack.amount());
                stacks.set(i, new GenericStack(newStack.what(), newAmount));

                // Determine if the addition overflowed. If it did, add the remainder as a new
                // stack.
                long overflow = newStack.amount() - (newAmount - existingStack.amount());
                if (overflow > 0) {
                    stacks.add(new GenericStack(newStack.what(), overflow));
                }
                return;
            }
        }
        stacks.add(newStack);
    }

    private List<FakeSlot> getFakeSlots(T menu) {
        List<FakeSlot> slots = new ArrayList<>(
                menu.getSlots(SlotSemantics.CONFIG).stream()
                        .map(s -> (FakeSlot) s)
                        .toList());
        if (!slots.isEmpty()) {
            return slots;
        }
        // `AEBaseMenu#getSlots` does not always work correctly with interfaces from addons,
        // but this somewhat brute force approach _does._
        LOGGER.info(
                String.format(
                        "Attempting to recipe fill into an interface (%s) that has not properly defined its SlotSemantics!",
                        menu.getClass().getName()));

        for (Slot slot : menu.slots) {
            if (slot instanceof FakeSlot fs) {
                slots.add(fs);
            }
        }
        return slots;
    }

    public void transfer(T menu, List<List<GenericStack>> recipeInputs) {
        ArrayList<GenericStack> inputsToSet = new ArrayList<>();
        for (List<GenericStack> genericIngredient : recipeInputs) {
            if (!genericIngredient.isEmpty()) {
                // Taking the first possible ingredient from the stack is a bit lazy, but
                // the number of use cases for autofilling tagged recipe inputs into an interface
                // has got to be in the single digits...
                addOrMerge(inputsToSet, genericIngredient.getFirst());
            }
        }
        List<FakeSlot> interfaceConfig = getFakeSlots(menu);
        int i = 0;
        for (FakeSlot slot : interfaceConfig) {
            var filter = (i < inputsToSet.size())
                    ? GenericStack.wrapInItemStack(inputsToSet.get(i))
                    : ItemStack.EMPTY;

            slot.setFilterTo(filter);
            i++;
        }
    }
}
