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

package appeng.recipes.handlers;

import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import appeng.core.AppEng;
import appeng.recipes.AERecipeTypes;

public class InscriberRecipe implements Recipe<RecipeInput> {

    private static final Codec<InscriberProcessType> MODE_CODEC = Codec.stringResolver(
            mode -> switch (mode) {
                case INSCRIBE -> "inscribe";
                case PRESS -> "press";
            },
            mode -> switch (mode) {
                default -> InscriberProcessType.INSCRIBE;
                case "press" -> InscriberProcessType.PRESS;
            });

    public static final MapCodec<InscriberRecipe> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            Ingredients.CODEC.fieldOf("ingredients")
                                    .forGetter(InscriberRecipe::getSerializedIngredients),
                            ItemStack.CODEC.fieldOf("result").forGetter(ir -> ir.output),
                            MODE_CODEC.fieldOf("mode").forGetter(ir -> ir.processType))
                    .apply(builder, InscriberRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InscriberRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredients.STREAM_CODEC,
            InscriberRecipe::getSerializedIngredients,
            ItemStack.STREAM_CODEC,
            InscriberRecipe::getResultItem,
            NeoForgeStreamCodecs.enumCodec(InscriberProcessType.class),
            InscriberRecipe::getProcessType,
            InscriberRecipe::new);

    @Deprecated(forRemoval = true, since = "1.21.1")
    public static final ResourceLocation TYPE_ID = AppEng.makeId("inscriber");

    @Deprecated(forRemoval = true, since = "1.21.1")
    public static final RecipeType<InscriberRecipe> TYPE = AERecipeTypes.INSCRIBER;

    private final Ingredient middleInput;
    private final Ingredient topOptional;
    private final Ingredient bottomOptional;
    private final ItemStack output;
    private final InscriberProcessType processType;

    private InscriberRecipe(Ingredients ingredients, ItemStack output, InscriberProcessType processType) {
        this(ingredients.middle(), output, ingredients.top(), ingredients.bottom(), processType);
    }

    public InscriberRecipe(Ingredient middleInput, ItemStack output,
            Ingredient topOptional, Ingredient bottomOptional, InscriberProcessType processType) {
        this.middleInput = Objects.requireNonNull(middleInput, "middleInput");
        this.output = Objects.requireNonNull(output, "output");
        this.topOptional = Objects.requireNonNull(topOptional, "topOptional");
        this.bottomOptional = Objects.requireNonNull(bottomOptional, "bottomOptional");
        this.processType = Objects.requireNonNull(processType, "processType");
    }

    @Override
    public boolean matches(RecipeInput inv, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput inv, HolderLookup.Provider registries) {
        return getResultItem(registries).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return getResultItem();
    }

    public ItemStack getResultItem() {
        return this.output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return InscriberRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(this.topOptional);
        ingredients.add(this.middleInput);
        ingredients.add(this.bottomOptional);
        return ingredients;
    }

    public Ingredient getMiddleInput() {
        return middleInput;
    }

    public Ingredient getTopOptional() {
        return topOptional;
    }

    public Ingredient getBottomOptional() {
        return bottomOptional;
    }

    public InscriberProcessType getProcessType() {
        return processType;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    private Ingredients getSerializedIngredients() {
        return new Ingredients(
                topOptional,
                middleInput,
                bottomOptional);
    }

    private record Ingredients(
            Ingredient top,
            Ingredient middle,
            Ingredient bottom) {
        public static final Codec<Ingredients> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Ingredient.CODEC.optionalFieldOf("top", Ingredient.EMPTY)
                        .forGetter(Ingredients::top),
                Ingredient.CODEC_NONEMPTY.fieldOf("middle")
                        .forGetter(Ingredients::middle),
                Ingredient.CODEC.optionalFieldOf("bottom", Ingredient.EMPTY)
                        .forGetter(Ingredients::bottom))
                .apply(builder, Ingredients::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Ingredients> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC,
                Ingredients::top,
                Ingredient.CONTENTS_STREAM_CODEC,
                Ingredients::middle,
                Ingredient.CONTENTS_STREAM_CODEC,
                Ingredients::bottom,
                Ingredients::new);
    }

}
