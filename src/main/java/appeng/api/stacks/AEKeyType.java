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

package appeng.api.stacks;

import java.text.NumberFormat;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import appeng.api.storage.AEKeyFilter;
import appeng.util.ReadableNumberConverter;

/**
 * Defines the properties of a specific subclass of {@link AEKey}. I.e. for {@link AEItemKey}, there is
 * {@link AEItemKeys}.
 */
public abstract class AEKeyType {
    private final ResourceLocation id;
    private final Class<? extends AEKey> keyClass;
    private final AEKeyFilter filter;
    private final Component description;

    public AEKeyType(ResourceLocation id, Class<? extends AEKey> keyClass, Component description) {
        Preconditions.checkArgument(!keyClass.equals(AEKey.class), "Can't register a key type for AEKey itself");
        this.id = id;
        this.keyClass = keyClass;
        this.filter = what -> what.getType() == this;
        this.description = description;
    }

    /**
     * @return AE2's key space for {@link AEItemKey}.
     */

    public static AEKeyType items() {
        return AEItemKeys.INSTANCE;
    }

    /**
     * @return See {@link #getRawId()}
     */
    @Nullable
    public static AEKeyType fromRawId(int id) {
        Preconditions.checkArgument(id >= 0 && id <= Byte.MAX_VALUE, "id out of range: %d", id);
        return AEKeyTypesInternal.getRegistry().byId(id);
    }

    /**
     * @return AE2's key space for {@link AEFluidKey}.
     */

    public static AEKeyType fluids() {
        return AEFluidKeys.INSTANCE;
    }

    /**
     * @return The unique ID of this storage channel.
     */
    public final ResourceLocation getId() {
        return id;
    }

    public final Class<? extends AEKey> getKeyClass() {
        return keyClass;
    }

    public final byte getRawId() {
        var id = AEKeyTypesInternal.getRegistry().getId(this);
        if (id < 0 || id > 127) {
            throw new IllegalStateException("Key type " + this + " has an invalid numeric id: " + id);
        }
        return (byte) id;
    }

    /**
     * How much of this key will be transferred as part of a transfer operation. Used to balance item vs. fluids
     * transfers.
     * <p>
     * E.g. used by IO Ports to transfer 1000 mB, not 1 mB to match the item channel transferring a full bucket per
     * operation.
     */
    public int getAmountPerOperation() {
        return 1;
    }

    /**
     * The amount of this key type that can be stored per byte used in a storage cell. Standard value for items is 8,
     * and for fluids it's 8000.
     */
    public int getAmountPerByte() {
        return 8;
    }

    /**
     * Attempts to load a key of this type from the given packet buffer.
     */
    @Nullable
    public abstract AEKey readFromPacket(FriendlyByteBuf input);

    /**
     * Attempts to load a key of this type from the given tag.
     */
    @Nullable
    public abstract AEKey loadKeyFromTag(CompoundTag tag);

    /**
     * Does this key belong to this storage channel.
     */
    @Nullable
    public final AEKey tryCast(AEKey key) {
        return keyClass.isInstance(key) ? keyClass.cast(key) : null;
    }

    /**
     * {@return whether the key is part of this key space}
     */
    public final boolean contains(AEKey key) {
        return keyClass.isInstance(key);
    }

    /**
     * True to indicate that the {@link AEKey} class used by this storage channel supports range-based fuzzy search
     * using {@link AEKey#getFuzzySearchValue()} and {@link AEKey#getFuzzySearchMaxValue()}.
     * <p/>
     * For items this is used for damage-based search and filtering.
     */
    public boolean supportsFuzzyRangeSearch() {
        return false;
    }

    /**
     * @return A filter matching all keys of this type.
     */
    public final AEKeyFilter filter() {
        return filter;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    /**
     * Get the translated name of this key space.
     */
    public Component getDescription() {
        return description;
    }

    /**
     * I.e. "B" for Buckets.
     */
    @Nullable
    public String getUnitSymbol() {
        return null;
    }

    public int getAmountPerUnit() {
        return 1;
    }

    /**
     * Format the amount into a user-readable string. See {@link AmountFormat} for an explanation of the different
     * formats. Includes the unit if applicable.
     */
    public final String formatAmount(long amount, AmountFormat format) {
        return switch (format) {
            case FULL -> formatFullAmount(amount);
            case SLOT -> formatShortAmount(amount, 4);
            case SLOT_LARGE_FONT -> formatShortAmount(amount, 3);
        };
    }

    private String formatFullAmount(long amount) {
        var result = new StringBuilder();

        if (getAmountPerUnit() > 1) {
            var units = amount / (double) getAmountPerUnit();
            result.append(NumberFormat.getNumberInstance().format(units));
        } else {
            result.append(NumberFormat.getNumberInstance().format(amount));
        }

        var unit = getUnitSymbol();
        if (unit != null) {
            result.append(' ').append(unit);
        }

        return result.toString();
    }

    private String formatShortAmount(long amount, int maxWidth) {
        if (getAmountPerUnit() > 1) {
            var units = amount / (double) getAmountPerUnit();
            return ReadableNumberConverter.format(units, maxWidth);
        } else {
            return ReadableNumberConverter.format(amount, maxWidth);
        }
    }

    /**
     * Returns all tags that apply to keys of this type. Is an optional operation is keys of this type cannot have tags,
     * and {@link AEKey#isTagged(TagKey)} is not implemented for this key type.
     * 
     * @see Registry#getTagNames()
     */
    public Stream<TagKey<?>> getTagNames() {
        return Stream.empty();
    }
}
