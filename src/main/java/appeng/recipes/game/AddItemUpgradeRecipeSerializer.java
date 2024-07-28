package appeng.recipes.game;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class AddItemUpgradeRecipeSerializer implements RecipeSerializer<AddItemUpgradeRecipe> {
    public static final AddItemUpgradeRecipeSerializer INSTANCE = new AddItemUpgradeRecipeSerializer();

    @Override
    public MapCodec<AddItemUpgradeRecipe> codec() {
        return AddItemUpgradeRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AddItemUpgradeRecipe> streamCodec() {
        return AddItemUpgradeRecipe.STREAM_CODEC;
    }
}
