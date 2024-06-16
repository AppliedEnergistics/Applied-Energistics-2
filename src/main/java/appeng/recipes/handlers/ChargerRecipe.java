package appeng.recipes.handlers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import appeng.core.AppEng;
import appeng.init.InitRecipeTypes;

public class ChargerRecipe implements Recipe<RecipeInput> {
    public static final ResourceLocation TYPE_ID = AppEng.makeId("charger");

    public static final RecipeType<ChargerRecipe> TYPE = InitRecipeTypes.register(TYPE_ID.toString());

    public final Ingredient ingredient;
    public final NonNullList<Ingredient> ingredients;
    public final ItemStack result;

    public static final MapCodec<ChargerRecipe> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(ChargerRecipe::getIngredient),
                            ItemStack.CODEC.fieldOf("result").forGetter(cr -> cr.result))
                    .apply(builder, ChargerRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChargerRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            ChargerRecipe::getIngredient,
            ItemStack.STREAM_CODEC,
            ChargerRecipe::getResultItem,
            ChargerRecipe::new);

    public ChargerRecipe(Ingredient ingredient, ItemStack result) {
        this.ingredient = ingredient;
        this.result = result;
        this.ingredients = NonNullList.of(Ingredient.EMPTY, ingredient);
    }

    @Override
    public boolean matches(RecipeInput container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput container, HolderLookup.Provider registries) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return getResultItem();
    }

    public ItemStack getResultItem() {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ChargerRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
