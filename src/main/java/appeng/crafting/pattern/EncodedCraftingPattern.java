package appeng.crafting.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record EncodedCraftingPattern(
        List<ItemStack> inputs,
        ItemStack result,
        ResourceLocation recipeId,
        boolean canSubstitute,
        boolean canSubstituteFluids
) {

    public static final Codec<EncodedCraftingPattern> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("inputs").forGetter(EncodedCraftingPattern::inputs),
            ItemStack.CODEC.fieldOf("result").forGetter(EncodedCraftingPattern::result),
            ResourceLocation.CODEC.fieldOf("recipeId").forGetter(EncodedCraftingPattern::recipeId),
            Codec.BOOL.fieldOf("canSubstitute").forGetter(EncodedCraftingPattern::canSubstitute),
            Codec.BOOL.fieldOf("canSubstituteFluids").forGetter(EncodedCraftingPattern::canSubstituteFluids)
    ).apply(builder, EncodedCraftingPattern::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EncodedCraftingPattern> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_LIST_STREAM_CODEC,
            EncodedCraftingPattern::inputs,
            ItemStack.STREAM_CODEC,
            EncodedCraftingPattern::result,
            ResourceLocation.STREAM_CODEC,
            EncodedCraftingPattern::recipeId,
            ByteBufCodecs.BOOL,
            EncodedCraftingPattern::canSubstitute,
            ByteBufCodecs.BOOL,
            EncodedCraftingPattern::canSubstituteFluids,
            EncodedCraftingPattern::new
    );

}
