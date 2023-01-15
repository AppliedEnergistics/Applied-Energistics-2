package appeng.recipes.handlers;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class ChargerRecipeBuilder {

    public static void charge(RecipeOutput consumer, ResourceLocation id, ItemLike input, ItemLike output) {
        consumer.accept(new Result(id, Ingredient.of(input), output));
    }

    public static void charge(RecipeOutput consumer, ResourceLocation id, TagKey<Item> input,
            ItemLike output) {
        consumer.accept(new Result(id, Ingredient.of(input), output));
    }

    public static void charge(RecipeOutput consumer, ResourceLocation id, Ingredient input,
            ItemLike output) {
        consumer.accept(new Result(id, input, output));
    }

    record Result(ResourceLocation id, Ingredient input, ItemLike output) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("ingredient", input.toJson(false));
            var stackObj = new JsonObject();
            stackObj.addProperty("item", BuiltInRegistries.ITEM.getKey(output.asItem()).toString());
            json.add("result", stackObj);
        }

        @Override
        public ResourceLocation id() {
            return id;
        }

        @Override
        public RecipeSerializer<?> type() {
            return ChargerRecipeSerializer.INSTANCE;
        }

        @Nullable
        @Override
        public AdvancementHolder advancement() {
            return null;
        }
    }
}
