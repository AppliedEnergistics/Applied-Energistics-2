package appeng.recipes.game;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CraftingUnitUpgradeSerializer implements RecipeSerializer<CraftingUnitUpgradeRecipe> {
    public static final CraftingUnitUpgradeSerializer INSTANCE = new CraftingUnitUpgradeSerializer();

    private CraftingUnitUpgradeSerializer() {}

    @Override
    public MapCodec<CraftingUnitUpgradeRecipe> codec() {
        return CraftingUnitUpgradeRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CraftingUnitUpgradeRecipe> streamCodec() {
        return CraftingUnitUpgradeRecipe.STREAM_CODEC;
    }
}
