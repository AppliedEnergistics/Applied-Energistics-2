package appeng.recipes.handlers;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import appeng.api.features.InscriberProcessType;
import appeng.core.AppEng;
import appeng.core.sync.BasePacket;

public class InscriberRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
        implements IRecipeSerializer<InscriberRecipe> {

    public static final InscriberRecipeSerializer INSTANCE = new InscriberRecipeSerializer();

    static {
        INSTANCE.setRegistryName(InscriberRecipe.TYPE_ID);
    }

    private InscriberRecipeSerializer() {
    }

    private static InscriberProcessType getMode(JsonObject json) {
        String mode = JSONUtils.getString(json, "mode", "inscribe");
        switch (mode) {
            case "inscribe":
                return InscriberProcessType.INSCRIBE;
            case "press":
                return InscriberProcessType.PRESS;
            default:
                throw new IllegalStateException("Unknown mode for inscriber recipe: " + mode);
        }

    }

    @Override
    public InscriberRecipe read(ResourceLocation recipeId, JsonObject json) {

        InscriberProcessType mode = getMode(json);

        String group = JSONUtils.getString(json, "group", "");
        ItemStack result = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));

        // Deserialize the three parts of the input
        JsonObject ingredients = JSONUtils.getJsonObject(json, "ingredients");
        Ingredient middle = Ingredient.deserialize(ingredients.get("middle"));
        Ingredient top = Ingredient.EMPTY;
        if (ingredients.has("top")) {
            top = Ingredient.deserialize(ingredients.get("top"));
        }
        Ingredient bottom = Ingredient.EMPTY;
        if (ingredients.has("bottom")) {
            bottom = Ingredient.deserialize(ingredients.get("bottom"));
        }

        return new InscriberRecipe(recipeId, group, middle, result, top, bottom, mode);
    }

    @Nullable
    @Override
    public InscriberRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        String group = buffer.readString(BasePacket.MAX_STRING_LENGTH);
        Ingredient middle = Ingredient.read(buffer);
        ItemStack result = buffer.readItemStack();
        Ingredient top = Ingredient.read(buffer);
        Ingredient bottom = Ingredient.read(buffer);
        InscriberProcessType mode = buffer.readEnumValue(InscriberProcessType.class);

        return new InscriberRecipe(recipeId, group, middle, result, top, bottom, mode);
    }

    @Override
    public void write(PacketBuffer buffer, InscriberRecipe recipe) {
        buffer.writeString(recipe.getGroup());
        recipe.getMiddleInput().write(buffer);
        buffer.writeItemStack(recipe.getRecipeOutput());
        recipe.getTopOptional().write(buffer);
        recipe.getBottomOptional().write(buffer);
        buffer.writeEnumValue(recipe.getProcessType());
    }

}
