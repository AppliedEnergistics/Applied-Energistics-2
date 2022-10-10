package appeng.api.stacks;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import appeng.items.misc.WrappedGenericStack;

/**
 * Represents some amount of some generic resource that AE can store or handle in crafting.
 */
public record GenericStack(AEKey what, long amount) {
    public GenericStack {
        Objects.requireNonNull(what, "what");
    }

    @Nullable
    public static GenericStack readBuffer(FriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return null;
        }

        var what = AEKey.readKey(buffer);
        if (what == null) {
            return null;
        }

        return new GenericStack(what, buffer.readVarLong());
    }

    public static void writeBuffer(@Nullable GenericStack stack, FriendlyByteBuf buffer) {
        if (stack == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);

            AEKey.writeKey(buffer, stack.what);
            buffer.writeVarLong(stack.amount);
        }
    }

    @Nullable
    public static GenericStack readTag(CompoundTag tag) {
        if (tag.isEmpty()) {
            return null;
        }
        var key = AEKey.fromTagGeneric(tag);
        if (key == null) {
            return null;
        }
        return new GenericStack(key, tag.getLong("#"));
    }

    public static CompoundTag writeTag(@Nullable GenericStack stack) {
        if (stack == null) {
            return new CompoundTag();
        }
        var tag = stack.what.toTagGeneric();
        tag.putLong("#", stack.amount);
        return tag;
    }

    /**
     * Converts a given item stack into a generic stack, accounting for a {@link GenericStack} already wrapped in an
     * {@link ItemStack}, unwrapping it automatically. If the item stack is empty, null is returned.
     */
    @Nullable
    public static GenericStack fromItemStack(ItemStack stack) {
        var genericStack = GenericStack.unwrapItemStack(stack);
        if (genericStack != null) {
            return genericStack;
        }

        var key = AEItemKey.of(stack);
        if (key == null) {
            return null;
        }
        return new GenericStack(key, stack.getCount());
    }

    /**
     * Converts a given fluid stack into a generic stack. If the fluid stack is empty, null is returned.
     */
    @Nullable
    public static GenericStack fromFluidStack(FluidStack stack) {
        var key = AEFluidKey.of(stack);
        if (key == null) {
            return null;
        }
        return new GenericStack(key, stack.getAmount());
    }

    public static long getStackSizeOrZero(@Nullable GenericStack stack) {
        return stack == null ? 0 : stack.amount;
    }

    public static ItemStack wrapInItemStack(@Nullable GenericStack stack) {
        if (stack != null) {
            return wrapInItemStack(stack.what(), stack.amount());
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static ItemStack wrapInItemStack(AEKey what, long amount) {
        return WrappedGenericStack.wrap(what, amount);
    }

    public static boolean isWrapped(ItemStack stack) {
        return stack.getItem() instanceof WrappedGenericStack;
    }

    public static GenericStack unwrapItemStack(ItemStack stack) {
        // The isEmpty stack is needed because the item can match while its count is 0
        if (!stack.isEmpty() && stack.getItem() instanceof WrappedGenericStack item) {
            var what = item.unwrapWhat(stack);
            if (what != null) {
                var amount = item.unwrapAmount(stack);
                return new GenericStack(what, amount);
            }
        }

        return null;
    }

    public static GenericStack sum(GenericStack left, GenericStack right) {
        if (!left.what.equals(right.what)) {
            throw new IllegalArgumentException("Cannot sum generic stacks of " + left.what + " and " + right.what);
        }
        return new GenericStack(left.what, left.amount + right.amount);
    }

}
