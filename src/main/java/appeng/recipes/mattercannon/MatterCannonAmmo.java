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
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;

import appeng.core.AppEng;
import appeng.init.InitRecipeTypes;

/**
 * Defines a type of ammo that can be used for the {@link appeng.items.tools.powered.MatterCannonItem}.
 */
public class MatterCannonAmmo implements Recipe<Container> {

    public static final ResourceLocation TYPE_ID = AppEng.makeId("matter_cannon");

    public static final RecipeType<MatterCannonAmmo> TYPE = InitRecipeTypes.register(TYPE_ID.toString());

    private final ResourceLocation id;

    private final Ingredient ammo;

    private final float weight;

    public MatterCannonAmmo(ResourceLocation id, Ingredient ammo, float weight) {
        Preconditions.checkArgument(weight >= 0, "Weight must not be negative");
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.ammo = Objects.requireNonNull(ammo, "ammo must not be null");
        this.weight = weight;
    }

    public static void ammo(Consumer<FinishedRecipe> consumer, ResourceLocation id, ItemLike item, float weight) {
        consumer.accept(new Ammo(id, null, Ingredient.of(item), weight));
    }

    public static void ammo(Consumer<FinishedRecipe> consumer, ResourceLocation id, Ingredient ammo, float weight) {
        consumer.accept(new Ammo(id, null, ammo, weight));
    }

    public static void ammo(Consumer<FinishedRecipe> consumer, ResourceLocation id, TagKey<Item> tag, float weight) {
        consumer.accept(new Ammo(id, tag, null, weight));
    }

    @Override
    public boolean matches(Container inv, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
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

    public record Ammo(ResourceLocation id, TagKey<Item> tag, Ingredient nonTag,
            float weight) implements FinishedRecipe {

        public void serializeRecipeData(JsonObject json) {
            JsonArray conditions = new JsonArray();
            if (tag != null) {
                json.add("ammo", Ingredient.of(tag).toJson());
                conditions.add(CraftingHelper.serialize(new NotCondition(
                        new TagEmptyCondition(tag.location()))));
            } else if (nonTag != null) {
                json.add("ammo", nonTag.toJson());
            }

            json.addProperty("weight", this.weight);
            json.add("conditions", conditions);
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return MatterCannonAmmoSerializer.INSTANCE;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
