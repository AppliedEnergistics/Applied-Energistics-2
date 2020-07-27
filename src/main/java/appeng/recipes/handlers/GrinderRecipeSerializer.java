package appeng.recipes.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import appeng.core.AEConfig;
import appeng.core.sync.BasePacket;

public class GrinderRecipeSerializer implements RecipeSerializer<GrinderRecipe> {

    public static final GrinderRecipeSerializer INSTANCE = new GrinderRecipeSerializer();

    private GrinderRecipeSerializer() {
    }

    @Override
    public GrinderRecipe read(Identifier recipeId, JsonObject json) {
        String group = JsonHelper.getString(json, "group", "");
        JsonObject inputObj = JsonHelper.getObject(json, "input");
        Ingredient ingredient = Ingredient.fromJson(inputObj);
        int ingredientCount = 1;
        if (inputObj.has("count")) {
            ingredientCount = inputObj.get("count").getAsInt();
        }

        JsonObject result = JsonHelper.getObject(json, "result");
        ItemStack primaryResult = ShapedRecipe.getItemStack(JsonHelper.getObject(result, "primary"));
        JsonArray optionalResultsJson = JsonHelper.getArray(result, "optional", null);
        List<GrinderOptionalResult> optionalResults = Collections.emptyList();
        if (optionalResultsJson != null) {
            optionalResults = new ArrayList<>(optionalResultsJson.size());
            for (JsonElement optionalResultJson : optionalResultsJson) {
                if (!optionalResultJson.isJsonObject()) {
                    throw new IllegalStateException("Entry in optional result list should be an object.");
                }
                ItemStack optionalResultItem = ShapedRecipe.getItemStack(optionalResultJson.getAsJsonObject());
                float optionalChance = JsonHelper.getFloat(optionalResultJson.getAsJsonObject(), "percentageChance",
                        AEConfig.instance().getOreDoublePercentage()) / 100.0f;
                optionalResults.add(new GrinderOptionalResult(optionalChance, optionalResultItem));
            }
        }

        int turns = JsonHelper.getInt(json, "turns", 8);

        return new GrinderRecipe(recipeId, group, ingredient, ingredientCount, primaryResult, turns, optionalResults);
    }

    @Nullable
    @Override
    public GrinderRecipe read(Identifier recipeId, PacketByteBuf buffer) {

        String group = buffer.readString(BasePacket.MAX_STRING_LENGTH);
        Ingredient ingredient = Ingredient.fromPacket(buffer);
        int ingredientCount = buffer.readVarInt();
        ItemStack result = buffer.readItemStack();
        int turns = buffer.readVarInt();
        int optionalResultsCount = buffer.readVarInt();
        List<GrinderOptionalResult> optionalResults = new ArrayList<>(optionalResultsCount);
        for (int i = 0; i < optionalResultsCount; i++) {
            float chance = buffer.readFloat();
            ItemStack optionalResult = buffer.readItemStack();
            optionalResults.add(new GrinderOptionalResult(chance, optionalResult));
        }

        return new GrinderRecipe(recipeId, group, ingredient, ingredientCount, result, turns, optionalResults);
    }

    @Override
    public void write(PacketByteBuf buffer, GrinderRecipe recipe) {
        buffer.writeString(recipe.getGroup());
        recipe.getIngredient().write(buffer);
        buffer.writeVarInt(recipe.getIngredientCount());
        buffer.writeItemStack(recipe.getOutput());
        buffer.writeVarInt(recipe.getTurns());
        List<GrinderOptionalResult> optionalResults = recipe.getOptionalResults();
        buffer.writeVarInt(optionalResults.size());
        for (GrinderOptionalResult optionalResult : optionalResults) {
            buffer.writeFloat(optionalResult.getChance());
            buffer.writeItemStack(optionalResult.getResult());
        }
    }

}
