/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.recipes.mattercannon;

import java.util.List;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.recipes.AERecipeTypes;

/**
 * Defines a type of ammo that can be used for the {@link appeng.items.tools.powered.MatterCannonItem}.
 */
public class MatterCannonAmmo implements Recipe<RecipeInput> {
    @Deprecated(forRemoval = true, since = "1.21.1")
    public static final Identifier TYPE_ID = AppEng.makeId("matter_cannon");

    @Deprecated(forRemoval = true, since = "1.21.1")
    public static final RecipeType<MatterCannonAmmo> TYPE = AERecipeTypes.MATTER_CANNON_AMMO;

    public static final MapCodec<MatterCannonAmmo> CODEC = RecordCodecBuilder.mapCodec((builder) -> {
        return builder.group(
                Ingredient.CODEC.fieldOf("ammo").forGetter(MatterCannonAmmo::getAmmo),
                Codec.FLOAT.fieldOf("weight").forGetter(MatterCannonAmmo::getWeight))
                .apply(builder, MatterCannonAmmo::new);
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, MatterCannonAmmo> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            MatterCannonAmmo::getAmmo,
            ByteBufCodecs.FLOAT,
            MatterCannonAmmo::getWeight,
            MatterCannonAmmo::new);

    private final Ingredient ammo;

    private final float weight;

    public MatterCannonAmmo(Ingredient ammo, float weight) {
        Preconditions.checkArgument(weight >= 0, "Weight must not be negative");
        this.ammo = Objects.requireNonNull(ammo, "ammo must not be null");
        this.weight = weight;
    }

    public static void ammo(RecipeOutput consumer, Identifier id, ItemLike item, float weight) {
        consumer.accept(ResourceKey.create(Registries.RECIPE, id), new MatterCannonAmmo(Ingredient.of(item), weight),
                null);
    }

    public static void ammo(RecipeOutput consumer, Identifier id, Ingredient ammo, float weight) {
        consumer.accept(ResourceKey.create(Registries.RECIPE, id), new MatterCannonAmmo(ammo, weight), null);
    }

    public static void ammo(HolderGetter<Item> items, RecipeOutput consumer, Identifier id, TagKey<Item> tag,
            float weight) {
        var recipe = new MatterCannonAmmo(Ingredient.of(items.getOrThrow(tag)), weight);
        var condition = new NotCondition(new TagEmptyCondition<>(tag));
        consumer.accept(ResourceKey.create(Registries.RECIPE, id), recipe, null, condition);
    }

    @Override
    public boolean matches(RecipeInput inv, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput inv, HolderLookup.Provider registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<MatterCannonAmmo> getSerializer() {
        return MatterCannonAmmoSerializer.INSTANCE;
    }

    @Override
    public RecipeType<MatterCannonAmmo> getType() {
        return TYPE;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(
                new MatterCannonAmmoDisplay(ammo.display(), weight,
                        new SlotDisplay.ItemSlotDisplay(AEItems.MATTER_CANNON.asItem())));
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public Ingredient getAmmo() {
        return ammo;
    }

    public float getWeight() {
        return weight;
    }
}
