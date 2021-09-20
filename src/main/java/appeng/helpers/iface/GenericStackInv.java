package appeng.helpers.iface;

import javax.annotation.Nullable;

import net.minecraft.nbt.ListTag;

import appeng.api.storage.data.IAEStack;
import appeng.crafting.execution.GenericStackHelper;

public class GenericStackInv {
    protected final IAEStack[] stacks;
    private final Listener listener;

    public GenericStackInv(@Nullable Listener listener, int size) {
        this.stacks = new IAEStack[size];
        this.listener = listener;
    }

    public int size() {
        return stacks.length;
    }

    public boolean isEmpty() {
        for (var stack : stacks) {
            if (stack != null) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public IAEStack getStack(int slot) {
        return stacks[slot];
    }

    public void setStack(int slot, @Nullable IAEStack stack) {
        stacks[slot] = stack;
        onChange();
    }

    protected void onChange() {
        if (listener != null) {
            listener.onChange();
        }
    }

    public ListTag writeToNBT() {
        ListTag tag = new ListTag();

        for (var stack : stacks) {
            tag.add(GenericStackHelper.writeGenericStack(stack));
        }

        return tag;
    }

    public void readFromNBT(ListTag tag) {
        for (int i = 0; i < tag.size(); ++i) {
            stacks[i] = GenericStackHelper.readGenericStack(tag.getCompound(i));
        }
    }

    public interface Listener {
        void onChange();
    }
}
