package appeng.crafting.pattern;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;

public record EncodedProcessingPattern(
        List<GenericStack> sparseInputs,
        List<GenericStack> sparseOutputs) {
    public EncodedProcessingPattern {
        sparseInputs = Collections.unmodifiableList(sparseInputs);
        sparseOutputs = Collections.unmodifiableList(sparseOutputs);
    }

    public static final Codec<EncodedProcessingPattern> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            GenericStack.NULLABLE_LIST_CODEC.fieldOf("sparseInputs").forGetter(EncodedProcessingPattern::sparseInputs),
            GenericStack.NULLABLE_LIST_CODEC.fieldOf("sparseOutputs")
                    .forGetter(EncodedProcessingPattern::sparseOutputs))
            .apply(builder, EncodedProcessingPattern::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EncodedProcessingPattern> STREAM_CODEC = StreamCodec
            .composite(
                    GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    EncodedProcessingPattern::sparseInputs,
                    GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    EncodedProcessingPattern::sparseOutputs,
                    EncodedProcessingPattern::new);

    public boolean containsMissingContent() {
        return Stream.concat(sparseInputs.stream(), sparseOutputs.stream())
                .anyMatch(stack -> stack != null && AEItems.MISSING_CONTENT.isSameAs(stack.what()));
    }
}
