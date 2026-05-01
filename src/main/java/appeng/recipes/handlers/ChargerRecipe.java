package appeng.recipes.handlers;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.MechanicsRecipe;

public class ChargerRecipe extends MechanicsRecipe<RecipeInput> {
    @Deprecated(forRemoval = true, since = "1.21.1")
    public static final Identifier TYPE_ID = AppEng.makeId("charger");
    @Deprecated(forRemoval = true, since = "1.21.1")
    public static final RecipeType<ChargerRecipe> TYPE = AERecipeTypes.CHARGER;

    private final Ingredient ingredient;
    private final ItemStackTemplate result;

    public static final MapCodec<ChargerRecipe> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            Ingredient.CODEC.fieldOf("ingredient").forGetter(ChargerRecipe::ingredient),
                            ItemStackTemplate.CODEC.fieldOf("result").forGetter(cr -> cr.result))
                    .apply(builder, ChargerRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChargerRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            ChargerRecipe::ingredient,
            ItemStackTemplate.STREAM_CODEC,
            ChargerRecipe::result,
            ChargerRecipe::new);

    public static final RecipeSerializer<ChargerRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public ChargerRecipe(Ingredient ingredient, ItemStackTemplate result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public ItemStackTemplate result() {
        return result;
    }

    @Override
    public RecipeSerializer<ChargerRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<ChargerRecipe> getType() {
        return TYPE;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(
                new ChargerRecipeDisplay(
                        ingredient.display(),
                        new SlotDisplay.ItemStackSlotDisplay(result),
                        new SlotDisplay.ItemSlotDisplay(AEBlocks.CHARGER.asItem())));
    }
}
