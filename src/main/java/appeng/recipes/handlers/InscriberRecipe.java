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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import appeng.core.definitions.AEBlocks;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.MechanicsRecipe;

public class InscriberRecipe extends MechanicsRecipe<RecipeInput> {

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
                            ItemStackTemplate.CODEC.fieldOf("result").forGetter(ir -> ir.result),
                            MODE_CODEC.fieldOf("mode").forGetter(ir -> ir.processType))
                    .apply(builder, InscriberRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InscriberRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredients.STREAM_CODEC,
            InscriberRecipe::getSerializedIngredients,
            ItemStackTemplate.STREAM_CODEC,
            InscriberRecipe::result,
            NeoForgeStreamCodecs.enumCodec(InscriberProcessType.class),
            InscriberRecipe::getProcessType,
            InscriberRecipe::new);

    public static final RecipeSerializer<InscriberRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private final Ingredient middleInput;
    private final Optional<Ingredient> topOptional;
    private final Optional<Ingredient> bottomOptional;
    private final ItemStackTemplate result;
    private final InscriberProcessType processType;

    private InscriberRecipe(Ingredients ingredients, ItemStackTemplate result, InscriberProcessType processType) {
        this(ingredients.middle(), result, ingredients.top(), ingredients.bottom(), processType);
    }

    public InscriberRecipe(Ingredient middleInput, ItemStackTemplate result,
            Optional<Ingredient> topOptional, Optional<Ingredient> bottomOptional, InscriberProcessType processType) {
        this.middleInput = Objects.requireNonNull(middleInput, "middleInput");
        this.result = Objects.requireNonNull(result, "result");
        this.topOptional = Objects.requireNonNull(topOptional, "topOptional");
        this.bottomOptional = Objects.requireNonNull(bottomOptional, "bottomOptional");
        this.processType = Objects.requireNonNull(processType, "processType");
    }

    public ItemStackTemplate result() {
        return this.result;
    }

    @Override
    public RecipeSerializer<InscriberRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<InscriberRecipe> getType() {
        return AERecipeTypes.INSCRIBER;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(
                new InscriberRecipeDisplay(
                        middleInput.display(),
                        topOptional.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE),
                        bottomOptional.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE),
                        processType,
                        new SlotDisplay.ItemStackSlotDisplay(result),
                        new SlotDisplay.ItemSlotDisplay(AEBlocks.INSCRIBER.asItem())));
    }

    public Ingredient getMiddleInput() {
        return middleInput;
    }

    public Optional<Ingredient> getTopOptional() {
        return topOptional;
    }

    public Optional<Ingredient> getBottomOptional() {
        return bottomOptional;
    }

    public InscriberProcessType getProcessType() {
        return processType;
    }

    private Ingredients getSerializedIngredients() {
        return new Ingredients(
                topOptional,
                middleInput,
                bottomOptional);
    }

    private record Ingredients(
            Optional<Ingredient> top,
            Ingredient middle,
            Optional<Ingredient> bottom) {
        public static final Codec<Ingredients> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Ingredient.CODEC.optionalFieldOf("top")
                        .forGetter(Ingredients::top),
                Ingredient.CODEC.fieldOf("middle")
                        .forGetter(Ingredients::middle),
                Ingredient.CODEC.optionalFieldOf("bottom")
                        .forGetter(Ingredients::bottom))
                .apply(builder, Ingredients::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Ingredients> STREAM_CODEC = StreamCodec.composite(
                Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
                Ingredients::top,
                Ingredient.CONTENTS_STREAM_CODEC,
                Ingredients::middle,
                Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
                Ingredients::bottom,
                Ingredients::new);
    }

}
