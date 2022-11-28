package appeng.recipes.handlers;

import java.util.function.Consumer;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ChargerRecipeBuilder {

    public static void charge(Consumer<FinishedRecipe> consumer, ResourceLocation id, Item input, Item output) {
        consumer.accept(new Result(id, Ingredient.of(input), output));
    }

    record Result(ResourceLocation id, Ingredient input, Item output) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("ingredient", input.toJson());
            var stackObj = new JsonObject();
            stackObj.addProperty("item", Registry.ITEM.getKey(output).toString());
            json.add("result", stackObj);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return ChargerRecipeSerializer.INSTANCE;
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
