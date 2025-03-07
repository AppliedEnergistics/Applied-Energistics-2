package appeng.recipes.transform;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;

public record TransformRecipeDisplay(
        List<SlotDisplay> ingredients,
        TransformCircumstance circumstance,
        SlotDisplay result) implements RecipeDisplay {

    public static final MapCodec<TransformRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> {
        return builder.group(SlotDisplay.CODEC.listOf().fieldOf("ingredients").forGetter(TransformRecipeDisplay::ingredients),
                        TransformCircumstance.CODEC
                                .optionalFieldOf("circumstance", TransformCircumstance.fluid(FluidTags.WATER))
                                .forGetter(TransformRecipeDisplay::circumstance),
                        SlotDisplay.CODEC.fieldOf("result").forGetter(TransformRecipeDisplay::result)
                )
                .apply(builder, TransformRecipeDisplay::new);
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, TransformRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
            SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TransformRecipeDisplay::ingredients,
            TransformCircumstance.STREAM_CODEC,
            TransformRecipeDisplay::circumstance,
            SlotDisplay.STREAM_CODEC,
            TransformRecipeDisplay::result,
            TransformRecipeDisplay::new
    );

    public static final RecipeDisplay.Type<TransformRecipeDisplay> TYPE = new RecipeDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public SlotDisplay craftingStation() {
        return SlotDisplay.Empty.INSTANCE;
    }

    @Override
    public Type<TransformRecipeDisplay> type() {
        return TYPE;
    }
}
