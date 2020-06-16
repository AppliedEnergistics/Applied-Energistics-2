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
import appeng.core.AppEng;

public class GrinderRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
        implements IRecipeSerializer<GrinderRecipe> {

    public static final GrinderRecipeSerializer INSTANCE = new GrinderRecipeSerializer();

    static {
        INSTANCE.setRegistryName(AppEng.MOD_ID, "grinder");
    }

    private GrinderRecipeSerializer() {
    }

    @Override
    public GrinderRecipe read(ResourceLocation recipeId, JsonObject json) {
        String group = JSONUtils.getString(json, "group", "");
        JsonObject inputObj = JSONUtils.getJsonObject(json, "input");
        Ingredient ingredient = Ingredient.deserialize(inputObj);
        int ingredientCount = 1;
        if (inputObj.has("count")) {
            ingredientCount = inputObj.get("count").getAsInt();
        }

        JsonObject result = JSONUtils.getJsonObject(json, "result");
        ItemStack primaryResult = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(result, "primary"));
        JsonArray optionalResultsJson = JSONUtils.getJsonArray(result, "optional", null);
        List<GrinderOptionalResult> optionalResults = Collections.emptyList();
        if (optionalResultsJson != null) {
            optionalResults = new ArrayList<>(optionalResultsJson.size());
            for (JsonElement optionalResultJson : optionalResultsJson) {
                if (!optionalResultJson.isJsonObject()) {
                    throw new IllegalStateException("Entry in optional result list should be an object.");
                }
                ItemStack optionalResultItem = ShapedRecipe.deserializeItem(optionalResultJson.getAsJsonObject());
                float optionalChance = JSONUtils.getFloat(optionalResultJson.getAsJsonObject(), "percentageChance",
                        AEConfig.instance().getOreDoublePercentage()) / 100.0f;
                optionalResults.add(new GrinderOptionalResult(optionalChance, optionalResultItem));
            }
        }

        int turns = JSONUtils.getInt(json, "turns", 8);

        return new GrinderRecipe(recipeId, group, ingredient, ingredientCount, primaryResult, turns, optionalResults);
    }

    @Nullable
    @Override
    public GrinderRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        // FIXME NOT YET IMPLEMENTED
        throw new IllegalStateException();
    }

    @Override
    public void write(PacketBuffer buffer, GrinderRecipe recipe) {
        // FIXME NOT YET IMPLEMENTED
        throw new IllegalStateException();
    }

}
