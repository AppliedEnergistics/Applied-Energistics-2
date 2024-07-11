package appeng.crafting.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import appeng.core.definitions.AEItems;

public record EncodedStonecuttingPattern(
        ItemStack input,
        ItemStack output,
        boolean canSubstitute,
        ResourceLocation recipeId) {

    public static final Codec<EncodedStonecuttingPattern> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ItemStack.CODEC.fieldOf("input").forGetter(EncodedStonecuttingPattern::input),
            ItemStack.CODEC.fieldOf("output").forGetter(EncodedStonecuttingPattern::output),
            Codec.BOOL.fieldOf("canSubstitute").forGetter(EncodedStonecuttingPattern::canSubstitute),
            ResourceLocation.CODEC.fieldOf("recipeId").forGetter(EncodedStonecuttingPattern::recipeId))
            .apply(builder, EncodedStonecuttingPattern::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EncodedStonecuttingPattern> STREAM_CODEC = StreamCodec
            .composite(
                    ItemStack.STREAM_CODEC,
                    EncodedStonecuttingPattern::input,
                    ItemStack.STREAM_CODEC,
                    EncodedStonecuttingPattern::output,
                    ByteBufCodecs.BOOL,
                    EncodedStonecuttingPattern::canSubstitute,
                    ResourceLocation.STREAM_CODEC,
                    EncodedStonecuttingPattern::recipeId,
                    EncodedStonecuttingPattern::new);

    public boolean containsMissingContent() {
        return AEItems.MISSING_CONTENT.is(input) || AEItems.MISSING_CONTENT.is(output);
    }
}
