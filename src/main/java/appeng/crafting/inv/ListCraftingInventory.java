package appeng.crafting.inv;

import java.util.Collection;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

public class ListCraftingInventory<T extends IAEStack<T>> implements ICraftingInventory<T> {
    public final IItemList<T> list;
    private final IStorageChannel<T> chan;

    public ListCraftingInventory(IStorageChannel<T> chan) {
        this.list = chan.createList();
        this.chan = chan;
    }

    public void postChange(T template, long delta) {
    }

    @Override
    public void injectItems(T input, Actionable mode) {
        if (mode == Actionable.MODULATE) {
            list.addStorage(input);
            postChange(input, -input.getStackSize());
        }
    }

    @Nullable
    @Override
    public T extractItems(T input, Actionable mode) {
        T precise = list.findPrecise(input);
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
    public Collection<T> findFuzzyTemplates(T input) {
        return list.findFuzzy(input, FuzzyMode.IGNORE_ALL);
    }

    public void clear() {
        for (T stack : list) {
            postChange(stack, stack.getStackSize());
        }
        list.resetStatus();
    }

    public void readFromNBT(ListTag data) {
        list.resetStatus();

        if (data != null) {
            for (int i = 0; i < data.size(); ++i) {
                injectItems(chan.createFromNBT(data.getCompound(i)), Actionable.MODULATE);
            }
        }
    }

    public ListTag writeToNBT() {
        ListTag tag = new ListTag();

        for (T stack : list) {
            CompoundTag t = new CompoundTag();
            stack.writeToNBT(t);
            tag.add(t);
        }

        return tag;
    }
}
