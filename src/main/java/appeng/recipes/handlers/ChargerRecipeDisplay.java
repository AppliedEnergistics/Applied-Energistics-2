package appeng.recipes.handlers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record ChargerRecipeDisplay(
        SlotDisplay ingredient,
        SlotDisplay result,
        SlotDisplay craftingStation) implements RecipeDisplay {

    public static final MapCodec<ChargerRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            SlotDisplay.CODEC.fieldOf("ingredient").forGetter(ChargerRecipeDisplay::ingredient),
                            SlotDisplay.CODEC.fieldOf("result").forGetter(ChargerRecipeDisplay::result),
                            SlotDisplay.CODEC.fieldOf("craftingStation")
                                    .forGetter(ChargerRecipeDisplay::craftingStation))
                    .apply(builder, ChargerRecipeDisplay::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChargerRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
            SlotDisplay.STREAM_CODEC,
            ChargerRecipeDisplay::ingredient,
            SlotDisplay.STREAM_CODEC,
            ChargerRecipeDisplay::result,
            SlotDisplay.STREAM_CODEC,
            ChargerRecipeDisplay::craftingStation,
            ChargerRecipeDisplay::new);

    public static final RecipeDisplay.Type<ChargerRecipeDisplay> TYPE = new RecipeDisplay.Type<>(MAP_CODEC,
            STREAM_CODEC);

    @Override
    public Type<? extends RecipeDisplay> type() {
        return TYPE;
    }
}
