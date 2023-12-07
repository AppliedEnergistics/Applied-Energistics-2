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

import appeng.core.AppEng;
import appeng.init.InitRecipeTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class InscriberRecipe implements Recipe<Container> {

    private static final Codec<InscriberProcessType> MODE_CODEC = ExtraCodecs.stringResolverCodec(
            mode -> switch (mode) {
                case INSCRIBE -> "inscribe";
                case PRESS -> "press";
            },
            mode -> switch (mode) {
                default -> InscriberProcessType.INSCRIBE;
                case "press" -> InscriberProcessType.PRESS;
            });

    public static final Codec<InscriberRecipe> CODEC = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            Ingredients.CODEC.fieldOf("ingredients").forGetter(InscriberRecipe::getSerializedIngredients),
                            ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter(ir -> ir.output),
                            MODE_CODEC.fieldOf("mode").forGetter(ir -> ir.processType))
                    .apply(builder, InscriberRecipe::new));

    public static final ResourceLocation TYPE_ID = AppEng.makeId("inscriber");

    public static final RecipeType<InscriberRecipe> TYPE = InitRecipeTypes.register(TYPE_ID.toString());

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
    public boolean matches(Container inv, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess) {
        return getResultItem(registryAccess).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
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
                bottomOptional
        );
    }

    private record Ingredients(
            Ingredient top,
            Ingredient middle,
            Ingredient bottom
    ) {
        public static final Codec<Ingredients> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("middle")
                        .forGetter(Ingredients::middle),
                ExtraCodecs.strictOptionalField(Ingredient.CODEC, "top", Ingredient.EMPTY)
                        .forGetter(Ingredients::top),
                ExtraCodecs.strictOptionalField(Ingredient.CODEC, "bottom", Ingredient.EMPTY)
                        .forGetter(Ingredients::bottom)
        ).apply(builder, Ingredients::new));
    }

}
