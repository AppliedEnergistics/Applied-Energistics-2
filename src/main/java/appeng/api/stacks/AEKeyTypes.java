/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.minecraft.resources.ResourceLocation;

/**
 * AE2's registry of all known {@link AEKeyType key types}.
 * <p/>
 * AE2 has built-in {@link AEKeyType#items() item} and {@link AEKeyType#fluids() fluid} key types. Addons can
 * register additional key types during initialization using {@link #register(AEKeyType)}.
 */
@ThreadSafe
public final class AEKeyTypes {
    private AEKeyTypes() {
    }

    /**
     * Register a new storage channel.
     * <p>
     * AE2 already provides native channels for {@link AEItemKey} and
     * {@link AEFluidKey}.
     * <p>
     * Each {@link AEKey} subtype can only have a single factory instance. Overwriting is not intended. Each subtype
     * should be a direct one, this might be enforced at any time.
     * <p>
     * Channel class and factory instance can be used interchangeable as identifier. In most cases the factory instance
     * is used as key as having direct access the methods is more beneficial compared to being forced to query the
     * registry each time.
     * <p>
     * Caching the factory instance in a field or local variable is perfectly for performance reasons. But do not use
     * any AE2 internal field as they can change randomly between releases.
     */
    public static synchronized void register(@Nonnull AEKeyType keyType) {
        Objects.requireNonNull(keyType, "keyType");
        AEKeyTypesInternal.register(keyType);
    }

    /**
     * Fetch the implementation for a specific storage channel {@link AEKeyType#getId() id}.
     *
     * @param id The {@link AEKeyType#getId() storage channel id}.
     * @return The storage channel implementation.
     * @throws IllegalArgumentException when fetching an unregistered channel.
     */
    @Nonnull
    public static AEKeyType get(@Nonnull ResourceLocation id) {
        var result = AEKeyTypesInternal.getRegistry().get(id);
        if (result == null) {
            throw new IllegalArgumentException("No key type registered for id " + id);
        }
        return result;
    }

    /**
     * An unmodifiable collection of all registered key types.
     * <p>
     * This is mainly used as helper to let storage grids construct their internal storage for each type.
     */
    @Nonnull
    public static Iterable<AEKeyType> getAll() {
        return AEKeyTypesInternal.getRegistry();
    }
}
