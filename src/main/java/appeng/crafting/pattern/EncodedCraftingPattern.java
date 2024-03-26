package appeng.crafting.pattern;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import appeng.core.definitions.AEItems;
import appeng.util.AECodecs;

public record EncodedCraftingPattern(
        List<ItemStack> inputs,
        ItemStack result,
        ResourceLocation recipeId,
        boolean canSubstitute,
        boolean canSubstituteFluids) {

    public static final Codec<EncodedCraftingPattern> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            AECodecs.FAULT_TOLERANT_OPTIONAL_ITEMSTACK_CODEC.listOf().fieldOf("inputs")
                    .forGetter(EncodedCraftingPattern::inputs),
            AECodecs.FAULT_TOLERANT_ITEMSTACK_CODEC.fieldOf("result").forGetter(EncodedCraftingPattern::result),
            ResourceLocation.CODEC.fieldOf("recipeId").forGetter(EncodedCraftingPattern::recipeId),
            Codec.BOOL.fieldOf("canSubstitute").forGetter(EncodedCraftingPattern::canSubstitute),
            Codec.BOOL.fieldOf("canSubstituteFluids").forGetter(EncodedCraftingPattern::canSubstituteFluids))
            .apply(builder, EncodedCraftingPattern::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EncodedCraftingPattern> STREAM_CODEC = StreamCodec
            .composite(
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
                    EncodedCraftingPattern::new);

    public boolean containsMissingContent() {
        return AEItems.MISSING_CONTENT.isSameAs(result) || inputs.stream().anyMatch(AEItems.MISSING_CONTENT::isSameAs);
    }
}
