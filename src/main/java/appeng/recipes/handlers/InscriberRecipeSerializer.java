package appeng.recipes.handlers;

import appeng.api.features.InscriberProcessType;
import appeng.core.AppEng;
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

public class InscriberRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<InscriberRecipe> {

    public static final InscriberRecipeSerializer INSTANCE = new InscriberRecipeSerializer();

    static {
        INSTANCE.setRegistryName(AppEng.MOD_ID, "inscriber");
    }

    private InscriberRecipeSerializer() {
    }

    private static InscriberProcessType getMode(JsonObject json) {
        String mode = JSONUtils.getString( json, "mode", "inscribe" );
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
        JsonObject ingredients = JSONUtils.getJsonObject( json, "ingredients" );
		Ingredient middle = Ingredient.deserialize( ingredients.get( "middle" ) );
		Ingredient top = Ingredient.EMPTY;
		if( ingredients.has( "top" ) )
		{
			top = Ingredient.deserialize( ingredients.get( "top" ) );
		}
		Ingredient bottom = Ingredient.EMPTY;
		if( ingredients.has( "bottom" ) )
		{
			bottom = Ingredient.deserialize( ingredients.get( "bottom" ) );
		}

        return new InscriberRecipe(recipeId, group, middle, result, top, bottom, mode);
    }

    @Nullable
    @Override
    public InscriberRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        // FIXME NOT YET IMPLEMENTED
        throw new IllegalStateException();
    }

    @Override
    public void write(PacketBuffer buffer, InscriberRecipe recipe) {
        // FIXME NOT YET IMPLEMENTED
        throw new IllegalStateException();
    }

}
