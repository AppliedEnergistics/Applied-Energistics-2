package appeng.recipes.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import appeng.core.AEConfig;
import appeng.core.sync.BasePacket;

public class GrinderRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
        implements IRecipeSerializer<GrinderRecipe> {

    public static final GrinderRecipeSerializer INSTANCE = new GrinderRecipeSerializer();

    static {
        INSTANCE.setRegistryName(GrinderRecipe.TYPE_ID);
    }

    private GrinderRecipeSerializer() {
    }

    @Override
    public GrinderRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        String group = JSONUtils.getAsString(json, "group", "");
        JsonObject inputObj = JSONUtils.getAsJsonObject(json, "input");
        Ingredient ingredient = Ingredient.fromJson(inputObj);
        int ingredientCount = 1;
        if (inputObj.has("count")) {
            ingredientCount = inputObj.get("count").getAsInt();
        }

        JsonObject result = JSONUtils.getAsJsonObject(json, "result");
        ItemStack primaryResult = ShapedRecipe.itemFromJson(JSONUtils.getAsJsonObject(result, "primary"));
        JsonArray optionalResultsJson = JSONUtils.getAsJsonArray(result, "optional", null);
        List<GrinderOptionalResult> optionalResults = Collections.emptyList();
        if (optionalResultsJson != null) {
            optionalResults = new ArrayList<>(optionalResultsJson.size());
            for (JsonElement optionalResultJson : optionalResultsJson) {
                if (!optionalResultJson.isJsonObject()) {
                    throw new IllegalStateException("Entry in optional result list should be an object.");
                }
                ItemStack optionalResultItem = ShapedRecipe.itemFromJson(optionalResultJson.getAsJsonObject());
                float optionalChance = JSONUtils.getAsFloat(optionalResultJson.getAsJsonObject(), "percentageChance",
                        AEConfig.instance().getOreDoublePercentage()) / 100.0f;
                optionalResults.add(new GrinderOptionalResult(optionalChance, optionalResultItem));
            }
        }

        int turns = JSONUtils.getAsInt(json, "turns", 8);

        return new GrinderRecipe(recipeId, group, ingredient, ingredientCount, primaryResult, turns, optionalResults);
    }

    @Nullable
    @Override
    public GrinderRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {

        String group = buffer.readUtf(BasePacket.MAX_STRING_LENGTH);
        Ingredient ingredient = Ingredient.fromNetwork(buffer);
        int ingredientCount = buffer.readVarInt();
        ItemStack result = buffer.readItem();
        int turns = buffer.readVarInt();
        int optionalResultsCount = buffer.readVarInt();
        List<GrinderOptionalResult> optionalResults = new ArrayList<>(optionalResultsCount);
        for (int i = 0; i < optionalResultsCount; i++) {
            float chance = buffer.readFloat();
            ItemStack optionalResult = buffer.readItem();
            optionalResults.add(new GrinderOptionalResult(chance, optionalResult));
        }

        return new GrinderRecipe(recipeId, group, ingredient, ingredientCount, result, turns, optionalResults);
    }

    @Override
    public void toNetwork(PacketBuffer buffer, GrinderRecipe recipe) {
        buffer.writeUtf(recipe.getGroup());
        recipe.getIngredient().toNetwork(buffer);
        buffer.writeVarInt(recipe.getIngredientCount());
        buffer.writeItem(recipe.getResultItem());
        buffer.writeVarInt(recipe.getTurns());
        List<GrinderOptionalResult> optionalResults = recipe.getOptionalResults();
        buffer.writeVarInt(optionalResults.size());
        for (GrinderOptionalResult optionalResult : optionalResults) {
            buffer.writeFloat(optionalResult.getChance());
            buffer.writeItem(optionalResult.getResult());
        }
    }

}
