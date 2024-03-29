/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.core.definitions;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Either;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.util.helpers.ItemComparisonHelper;

public class ItemDefinition<T extends Item> implements ItemLike, Holder<Item>, Supplier<T> {
    private final String englishName;
    private final DeferredItem<T> item;

    public ItemDefinition(String englishName, DeferredItem<T> item) {
        this.englishName = englishName;
        this.item = item;
    }

    public String getEnglishName() {
        return englishName;
    }

    /**
     * Use {@link #getId()} in preperation for moving to DeferredItem
     */
    @Deprecated(since = "1.20.4", forRemoval = true)
    public ResourceLocation id() {
        return this.item.getId();
    }

    public ResourceLocation getId() {
        return this.item.getId();
    }

    public ItemStack stack() {
        return stack(1);
    }

    public ItemStack stack(int stackSize) {
        return new ItemStack((ItemLike) item, stackSize);
    }

    public GenericStack genericStack(long stackSize) {
        return new GenericStack(AEItemKey.of(item), stackSize);
    }

    /**
     * Compare {@link ItemStack} with this
     *
     * @param comparableStack compared item
     * @return true if the item stack is a matching item.
     */
    public final boolean isSameAs(ItemStack comparableStack) {
        return ItemComparisonHelper.isEqualItemType(comparableStack, this.stack());
    }

    /**
     * @return True if this item is represented by the given key.
     */
    public final boolean isSameAs(AEKey key) {
        if (key instanceof AEItemKey itemKey) {
            return asItem() == itemKey.getItem();
        }
        return false;
    }

    @Override
    public Item value() {
        return item.value();
    }

    @Override
    public boolean isBound() {
        return item.isBound();
    }

    @Override
    public boolean is(ResourceLocation pLocation) {
        return item.is(pLocation);
    }

    @Override
    public boolean is(ResourceKey<Item> pResourceKey) {
        return item.is(pResourceKey);
    }

    @Override
    public boolean is(Predicate<ResourceKey<Item>> pPredicate) {
        return item.is(pPredicate);
    }

    @Override
    public boolean is(TagKey<Item> pTagKey) {
        return item.is(pTagKey);
    }

    @Override
    public Stream<TagKey<Item>> tags() {
        return item.tags();
    }

    @Override
    public Either<ResourceKey<Item>, Item> unwrap() {
        return item.unwrap();
    }

    @Override
    public Optional<ResourceKey<Item>> unwrapKey() {
        return item.unwrapKey();
    }

    @Override
    public Kind kind() {
        return item.kind();
    }

    @Override
    public boolean canSerializeIn(HolderOwner<Item> pOwner) {
        return item.canSerializeIn(pOwner);
    }

    public static <T> Holder<T> direct(T pValue) {
        return Holder.direct(pValue);
    }

    @Override
    public <T> @Nullable T getData(DataMapType<Item, T> type) {
        return item.getData(type);
    }

    @Override
    public T get() {
        return item.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T asItem() {
        return (T) item.asItem();
    }
}
