package appeng.recipes.handlers;

import appeng.core.AppEng;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GrinderRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<GrinderRecipe> {

    public static final GrinderRecipeSerializer INSTANCE = new GrinderRecipeSerializer();

    static {
        INSTANCE.setRegistryName(AppEng.MOD_ID, "grinder");
    }

    private GrinderRecipeSerializer() {
    }

    @Override
    public GrinderRecipe read(ResourceLocation recipeId, JsonObject json) {
        String group = JSONUtils.getString(json, "group", "");
        Ingredient ingredient = Ingredient.deserialize(JSONUtils.getJsonObject(json, "input"));

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
                float optionalChance = JSONUtils.getFloat(optionalResultJson.getAsJsonObject(), "chance", 1.0f);
                optionalResults.add(new GrinderOptionalResult(optionalChance, optionalResultItem));
            }
        }

        int turns = JSONUtils.getInt(json, "turns", 8);

        return new GrinderRecipe(recipeId, group, ingredient, primaryResult, turns, optionalResults);
    }

    @Nullable
    @Override
    public GrinderRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        return null;
    }

    @Override
    public void write(PacketBuffer buffer, GrinderRecipe recipe) {

    }

}
