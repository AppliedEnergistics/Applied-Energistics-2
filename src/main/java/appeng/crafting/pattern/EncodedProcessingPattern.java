package appeng.crafting.pattern;

import appeng.api.stacks.GenericStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record EncodedProcessingPattern(
        List<GenericStack> sparseInputs,
        List<GenericStack> sparseOutputs
) {
    public EncodedProcessingPattern {
        sparseInputs = List.copyOf(sparseInputs);
        sparseOutputs = List.copyOf(sparseOutputs);
    }

    public static final Codec<EncodedProcessingPattern> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            GenericStack.NULLABLE_LIST_CODEC.fieldOf("sparseInputs").forGetter(EncodedProcessingPattern::sparseInputs),
            GenericStack.NULLABLE_LIST_CODEC.fieldOf("sparseOutputs").forGetter(EncodedProcessingPattern::sparseOutputs)
    ).apply(builder, EncodedProcessingPattern::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EncodedProcessingPattern> STREAM_CODEC = StreamCodec.composite(
            GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
            EncodedProcessingPattern::sparseInputs,
            GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
            EncodedProcessingPattern::sparseOutputs,
            EncodedProcessingPattern::new
    );
}
