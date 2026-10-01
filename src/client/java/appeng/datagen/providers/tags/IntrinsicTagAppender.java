/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2026, TeamAppliedEnergistics, All rights reserved.
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

package appeng.datagen.providers.tags;

import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

/**
 * Vanilla's {@link TagAppender} only accepts resource keys since 26.2. This wrapper restores the ability to add
 * registered objects directly by looking up their key in the given registry.
 */
public record IntrinsicTagAppender<T>(TagAppender<T> delegate, Registry<T> registry) implements TagAppender<T> {
    @SafeVarargs
    public final IntrinsicTagAppender<T> add(T... values) {
        for (var value : values) {
            delegate.add(registry.getResourceKey(value)
                    .orElseThrow(() -> new IllegalArgumentException("Unregistered object: " + value)));
        }
        return this;
    }

    @Override
    public IntrinsicTagAppender<T> add(ResourceKey<T> element) {
        delegate.add(element);
        return this;
    }

    @Override
    public IntrinsicTagAppender<T> addOptional(ResourceKey<T> element) {
        delegate.addOptional(element);
        return this;
    }

    @Override
    public IntrinsicTagAppender<T> addTag(TagKey<T> tag) {
        delegate.addTag(tag);
        return this;
    }

    @Override
    public IntrinsicTagAppender<T> addOptionalTag(TagKey<T> tag) {
        delegate.addOptionalTag(tag);
        return this;
    }

    @Override
    public IntrinsicTagAppender<T> add(TagEntry entry) {
        delegate.add(entry);
        return this;
    }

    @Override
    public IntrinsicTagAppender<T> replace(boolean value) {
        delegate.replace(value);
        return this;
    }

    @Override
    public IntrinsicTagAppender<T> remove(ResourceKey<T> element) {
        delegate.remove(element);
        return this;
    }

    @Override
    public IntrinsicTagAppender<T> remove(TagKey<T> tag) {
        delegate.remove(tag);
        return this;
    }
}
