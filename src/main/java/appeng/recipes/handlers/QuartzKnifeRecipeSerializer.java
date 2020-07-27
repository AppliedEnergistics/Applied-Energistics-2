package appeng.recipes.handlers;

import com.google.gson.JsonObject;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.Identifier;

public class QuartzKnifeRecipeSerializer extends ShapelessRecipe.Serializer {

    public static final QuartzKnifeRecipeSerializer INSTANCE = new QuartzKnifeRecipeSerializer();

    @Override
    public ShapelessRecipe read(Identifier identifier, JsonObject jsonObject) {
        return new QuartzKnifeRecipe(super.read(identifier, jsonObject));
    }

    @Override
    public ShapelessRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
        return new QuartzKnifeRecipe(super.read(identifier, packetByteBuf));
    }

}
