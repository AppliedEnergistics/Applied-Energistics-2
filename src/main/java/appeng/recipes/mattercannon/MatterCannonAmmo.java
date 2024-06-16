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

import java.util.Objects;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;

import appeng.core.AppEng;
import appeng.init.InitRecipeTypes;

/**
 * Defines a type of ammo that can be used for the {@link appeng.items.tools.powered.MatterCannonItem}.
 */
public class MatterCannonAmmo implements Recipe<RecipeInput> {

    public static final ResourceLocation TYPE_ID = AppEng.makeId("matter_cannon");

    public static final RecipeType<MatterCannonAmmo> TYPE = InitRecipeTypes.register(TYPE_ID.toString());

    public static final MapCodec<MatterCannonAmmo> CODEC = RecordCodecBuilder.mapCodec((builder) -> {
        return builder.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ammo").forGetter(MatterCannonAmmo::getAmmo),
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

    public static void ammo(RecipeOutput consumer, ResourceLocation id, ItemLike item, float weight) {
        consumer.accept(id, new MatterCannonAmmo(Ingredient.of(item), weight), null);
    }

    public static void ammo(RecipeOutput consumer, ResourceLocation id, Ingredient ammo, float weight) {
        consumer.accept(id, new MatterCannonAmmo(ammo, weight), null);
    }

    public static void ammo(RecipeOutput consumer, ResourceLocation id, TagKey<Item> tag, float weight) {
        consumer.accept(id, new MatterCannonAmmo(Ingredient.of(tag), weight), null, new NotCondition(
                new TagEmptyCondition(tag.location())));
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
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MatterCannonAmmoSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    public Ingredient getAmmo() {
        return ammo;
    }

    public float getWeight() {
        return weight;
    }
}
