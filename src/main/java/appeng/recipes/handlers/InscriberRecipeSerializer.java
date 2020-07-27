package appeng.recipes.handlers;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import appeng.api.features.InscriberProcessType;
import appeng.core.sync.BasePacket;

public class InscriberRecipeSerializer implements RecipeSerializer<InscriberRecipe> {

    public static final InscriberRecipeSerializer INSTANCE = new InscriberRecipeSerializer();

    private InscriberRecipeSerializer() {
    }

    private static InscriberProcessType getMode(JsonObject json) {
        String mode = JsonHelper.getString(json, "mode", "inscribe");
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
    public InscriberRecipe read(Identifier recipeId, JsonObject json) {

        InscriberProcessType mode = getMode(json);

        String group = JsonHelper.getString(json, "group", "");
        ItemStack result = ShapedRecipe.getItemStack(JsonHelper.getObject(json, "result"));

        // Deserialize the three parts of the input
        JsonObject ingredients = JsonHelper.getObject(json, "ingredients");
        Ingredient middle = Ingredient.fromJson(ingredients.get("middle"));
        Ingredient top = Ingredient.EMPTY;
        if (ingredients.has("top")) {
            top = Ingredient.fromJson(ingredients.get("top"));
        }
        Ingredient bottom = Ingredient.EMPTY;
        if (ingredients.has("bottom")) {
            bottom = Ingredient.fromJson(ingredients.get("bottom"));
        }

        return new InscriberRecipe(recipeId, group, middle, result, top, bottom, mode);
    }

    @Nullable
    @Override
    public InscriberRecipe read(Identifier recipeId, PacketByteBuf buffer) {
        String group = buffer.readString(BasePacket.MAX_STRING_LENGTH);
        Ingredient middle = Ingredient.fromPacket(buffer);
        ItemStack result = buffer.readItemStack();
        Ingredient top = Ingredient.fromPacket(buffer);
        Ingredient bottom = Ingredient.fromPacket(buffer);
        InscriberProcessType mode = buffer.readEnumConstant(InscriberProcessType.class);

        return new InscriberRecipe(recipeId, group, middle, result, top, bottom, mode);
    }

    @Override
    public void write(PacketByteBuf buffer, InscriberRecipe recipe) {
        buffer.writeString(recipe.getGroup());
        recipe.getMiddleInput().write(buffer);
        buffer.writeItemStack(recipe.getOutput());
        recipe.getTopOptional().write(buffer);
        recipe.getBottomOptional().write(buffer);
        buffer.writeEnumConstant(recipe.getProcessType());
    }

}
