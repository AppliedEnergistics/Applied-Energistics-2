package appeng.crafting.inv;

import java.util.Collection;

import javax.annotation.Nullable;

import net.minecraft.nbt.ListTag;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.MixedItemList;
import appeng.crafting.execution.GenericStackHelper;

public class ListCraftingInventory implements ICraftingInventory {
    public final MixedItemList list = new MixedItemList();

    public void postChange(IAEStack<?> template, long delta) {
    }

    @Override
    public void injectItems(IAEStack<?> input, Actionable mode) {
        if (mode == Actionable.MODULATE) {
            list.addStorage(input);
            postChange(input, -input.getStackSize());
        }
    }

    @Nullable
    @Override
    public IAEStack<?> extractItems(IAEStack<?> input, Actionable mode) {
        IAEStack<?> precise = list.findPrecise(input);
        if (precise == null)
            return null;
        long extracted = Math.min(precise.getStackSize(), input.getStackSize());
        if (mode == Actionable.MODULATE) {
            precise.decStackSize(extracted);
            postChange(input, extracted);
        }
        return input.copyWithStackSize(extracted);
    }

    @Override
    public Collection<IAEStack<?>> findFuzzyTemplates(IAEStack<?> input) {
        return list.findFuzzy(input, FuzzyMode.IGNORE_ALL);
    }

    public void clear() {
        for (IAEStack<?> stack : list) {
            postChange(stack, stack.getStackSize());
        }
        list.resetStatus();
    }

    public void readFromNBT(ListTag data) {
        list.resetStatus();

        if (data != null) {
            for (int i = 0; i < data.size(); ++i) {
                injectItems(GenericStackHelper.readGenericStack(data.getCompound(i)), Actionable.MODULATE);
            }
        }
    }

    public ListTag writeToNBT() {
        ListTag tag = new ListTag();

        for (IAEStack<?> stack : list) {
            tag.add(GenericStackHelper.writeGenericStack(stack));
        }

        return tag;
    }
}
