package appeng.recipes.handlers;

import com.google.gson.JsonObject;

import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class QuartzKnifeRecipeSerializer extends ShapelessRecipe.Serializer {

    public static final QuartzKnifeRecipeSerializer INSTANCE = new QuartzKnifeRecipeSerializer();

    @Override
    public ShapelessRecipe read(ResourceLocation identifier, JsonObject jsonObject) {
        return new QuartzKnifeRecipe(super.read(identifier, jsonObject));
    }

    @Override
    public ShapelessRecipe read(ResourceLocation identifier, PacketBuffer packetByteBuf) {
        return new QuartzKnifeRecipe(super.read(identifier, packetByteBuf));
    }

}
