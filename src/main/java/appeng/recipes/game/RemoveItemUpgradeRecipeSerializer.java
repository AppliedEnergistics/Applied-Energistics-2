package appeng.recipes.game;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class RemoveItemUpgradeRecipeSerializer implements RecipeSerializer<RemoveItemUpgradeRecipe> {
    public static final RemoveItemUpgradeRecipeSerializer INSTANCE = new RemoveItemUpgradeRecipeSerializer();

    @Override
    public MapCodec<RemoveItemUpgradeRecipe> codec() {
        return RemoveItemUpgradeRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RemoveItemUpgradeRecipe> streamCodec() {
        return RemoveItemUpgradeRecipe.STREAM_CODEC;
    }
}
