/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.storage.data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;

public interface IAEStack {

    /**
     * Adds the stack size, requestable count and craftable status.
     */
    static <T extends IAEStack> void add(@Nonnull T stack, @Nullable T otherStack) {
        if (otherStack == null) {
            return;
        }

        stack.setStackSize(stack.getStackSize() + otherStack.getStackSize());
        stack.setCountRequestable(stack.getCountRequestable() + otherStack.getCountRequestable());
        stack.setCraftable(stack.isCraftable() || otherStack.isCraftable());
    }

    /**
     * Generalized method to clone an item stack of an arbitrary storage channel.
     *
     * @return a new Stack, which is copied from the original.
     */
    @SuppressWarnings("unchecked")
    static <T extends IAEStack> T copy(@Nonnull T stack) {
        var channel = (IStorageChannel<T>) stack.getChannel();
        return channel.copy(stack);
    }

    /**
     * Copies a stack and sets the stack size of the copy. Also clears craftable and requestable.
     */
    static <T extends IAEStack> T copy(@Nonnull T stack, long newStackSize) {
        var result = IAEStack.copy(stack);
        result.reset();
        result.setStackSize(newStackSize);
        return result;
    }

    static long getStackSizeOrZero(@Nullable IAEStack stack) {
        return stack == null ? 0 : stack.getStackSize();
    }

    /**
     * number of items in the stack.
     *
     * @return basically ItemStack.stackSize
     */
    long getStackSize();

    /**
     * changes the number of items in the stack.
     *
     * @param stackSize , ItemStack.stackSize = N
     */
    void setStackSize(long stackSize);

    /**
     * Same as getStackSize, but for requestable items. ( LP )
     *
     * @return basically itemStack.stackSize but for requestable items.
     */
    long getCountRequestable();

    /**
     * Same as setStackSize, but for requestable items. ( LP )
     */
    void setCountRequestable(long countRequestable);

    /**
     * true, if the item can be crafted.
     *
     * @return true, if it can be crafted.
     */
    boolean isCraftable();

    /**
     * change weather the item can be crafted.
     *
     * @param isCraftable can item be crafted
     */
    void setCraftable(boolean isCraftable);

    /**
     * clears, requestable, craftable, and stack sizes.
     */
    void reset();

    /**
     * returns true, if the item can be crafted, requested, or extracted.
     *
     * @return isThisRecordMeaningful
     */
    boolean isMeaningful();

    /**
     * Adds more to the stack size...
     *
     * @param i additional stack size
     */
    default void incStackSize(long i) {
        setStackSize(getStackSize() + i);
    }

    /**
     * removes some from the stack size.
     */
    default void decStackSize(long i) {
        setStackSize(getStackSize() - i);
    }

    default void multStackSize(long i) {
        setStackSize(getStackSize() * i);
    }

    /**
     * adds items to the requestable
     *
     * @param i increased amount of requested items
     */
    void incCountRequestable(long i);

    /**
     * removes items from the requestable
     *
     * @param i decreased amount of requested items
     */
    void decCountRequestable(long i);

    /**
     * write to a CompoundNBT.
     *
     * @param i to be written data
     */
    void writeToNBT(CompoundTag i);

    /**
     * Compare stacks using precise logic.
     *
     * a IAEItemStack to another AEItemStack or a ItemStack.
     *
     * or
     *
     * IAEFluidStack, FluidStack
     *
     * @param obj compared object
     *
     * @return true if they are the same.
     */
    @Override
    boolean equals(Object obj);

    /**
     * Compare the same subtype of {@link IAEStack} with another using a fuzzy comparison.
     *
     * @param other The stack to compare.
     * @param mode  Which {@link FuzzyMode} should be used.
     *
     * @return true if two stacks are equal based on AE Fuzzy Comparison.
     */
    boolean fuzzyEquals(IAEStack other, FuzzyMode mode);

    /**
     * Slower for disk saving, but smaller/more efficient for packets.
     *
     * @param data to be written data
     */
    void writeToPacket(FriendlyByteBuf data);

    IStorageChannel<?> getChannel();

    /**
     * Returns itemstack for display and similar purposes. Always has a count of 1.
     *
     * @return itemstack
     */
    ItemStack asItemStackRepresentation();

    /**
     * Convenience method to cast inventory handlers with wildcard generic types to the concrete type used by the given
     * storage channel, but only if the given storage channel is equal to {@link #getChannel()}.
     *
     * @throws IllegalArgumentException If channel is not equal to {@link #getChannel()}.
     */
    @SuppressWarnings("unchecked")
    default <SC extends IAEStack> SC cast(IStorageChannel<SC> channel) {
        if (getChannel() == channel) {
            return (SC) this;
        }
        throw new IllegalArgumentException("This stack's storage channel " + getChannel()
                + " is not compatible with " + channel);
    }
}
