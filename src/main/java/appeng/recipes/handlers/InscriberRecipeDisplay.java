package appeng.recipes.handlers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record InscriberRecipeDisplay(
        SlotDisplay middle,
        SlotDisplay top,
        SlotDisplay bottom,
        InscriberProcessType processType,
        SlotDisplay result,
        SlotDisplay craftingStation) implements RecipeDisplay {

    public static final MapCodec<InscriberRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            SlotDisplay.CODEC.fieldOf("middle").forGetter(InscriberRecipeDisplay::middle),
                            SlotDisplay.CODEC.fieldOf("top").forGetter(InscriberRecipeDisplay::top),
                            SlotDisplay.CODEC.fieldOf("bottom").forGetter(InscriberRecipeDisplay::bottom),
                            InscriberProcessType.CODEC.fieldOf("processType").forGetter(InscriberRecipeDisplay::processType),
                            SlotDisplay.CODEC.fieldOf("result").forGetter(InscriberRecipeDisplay::result),
                            SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(InscriberRecipeDisplay::craftingStation)
                    )
                    .apply(builder, InscriberRecipeDisplay::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, InscriberRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
            SlotDisplay.STREAM_CODEC, InscriberRecipeDisplay::middle,
            SlotDisplay.STREAM_CODEC, InscriberRecipeDisplay::top,
            SlotDisplay.STREAM_CODEC, InscriberRecipeDisplay::bottom,
            InscriberProcessType.STREAM_CODEC, InscriberRecipeDisplay::processType,
            SlotDisplay.STREAM_CODEC, InscriberRecipeDisplay::result,
            SlotDisplay.STREAM_CODEC, InscriberRecipeDisplay::craftingStation,
            InscriberRecipeDisplay::new
    );

    public static final RecipeDisplay.Type<InscriberRecipeDisplay> TYPE = new RecipeDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public Type<InscriberRecipeDisplay> type() {
        return TYPE;
    }
}
