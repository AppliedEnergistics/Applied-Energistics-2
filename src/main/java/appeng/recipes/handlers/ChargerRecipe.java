package appeng.recipes.handlers;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.recipes.AERecipeTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public class ChargerRecipe implements Recipe<RecipeInput> {
    @Deprecated(forRemoval = true, since = "1.21.1")
    public static final ResourceLocation TYPE_ID = AppEng.makeId("charger");
    @Deprecated(forRemoval = true, since = "1.21.1")
    public static final RecipeType<ChargerRecipe> TYPE = AERecipeTypes.CHARGER;

    public final Ingredient ingredient;
    public final NonNullList<Ingredient> ingredients;
    public final ItemStack result;

    public static final MapCodec<ChargerRecipe> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            Ingredient.CODEC.fieldOf("ingredient").forGetter(ChargerRecipe::getIngredient),
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
        this.ingredients = NonNullList.of(Ingredient.of(), ingredient);
    }

    @Override
    public boolean matches(RecipeInput container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput container, HolderLookup.Provider registries) {
        return null;
    }

    public ItemStack getResultItem() {
        return result;
    }

    @Override
    public RecipeSerializer<ChargerRecipe> getSerializer() {
        return ChargerRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<ChargerRecipe> getType() {
        return TYPE;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(
                new ChargerRecipeDisplay(
                        ingredient.display(),
                        new SlotDisplay.ItemStackSlotDisplay(result),
                        new SlotDisplay.ItemSlotDisplay(AEBlocks.CHARGER.asItem())
                )
        );
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
