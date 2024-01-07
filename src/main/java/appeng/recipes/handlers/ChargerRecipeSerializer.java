package appeng.recipes.handlers;

import com.mojang.serialization.Codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ChargerRecipeSerializer implements RecipeSerializer<ChargerRecipe> {

    public static final ChargerRecipeSerializer INSTANCE = new ChargerRecipeSerializer();

    @Override
    public Codec<ChargerRecipe> codec() {
        return ChargerRecipe.CODEC;
    }

    @Override
    public ChargerRecipe fromNetwork(FriendlyByteBuf buffer) {
        Ingredient ingredient = Ingredient.fromNetwork(buffer);
        ItemStack result = buffer.readItem();

        return new ChargerRecipe(ingredient, result.getItem());
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, ChargerRecipe recipe) {
        recipe.ingredient.toNetwork(buffer);
        buffer.writeItem(new ItemStack(recipe.result));
    }
}
