package appeng.recipes.handlers;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ChargerRecipeSerializer implements RecipeSerializer<ChargerRecipe> {
    public static final ChargerRecipeSerializer INSTANCE = new ChargerRecipeSerializer();

    @Override
    public MapCodec<ChargerRecipe> codec() {
        return ChargerRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ChargerRecipe> streamCodec() {
        return ChargerRecipe.STREAM_CODEC;
    }
}
