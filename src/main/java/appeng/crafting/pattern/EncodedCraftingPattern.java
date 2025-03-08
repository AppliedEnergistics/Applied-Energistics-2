package appeng.crafting.pattern;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import appeng.core.definitions.AEItems;
import appeng.util.AECodecs;
import net.minecraft.world.item.crafting.Recipe;

public record EncodedCraftingPattern(
        List<ItemStack> inputs,
        ItemStack result,
        ResourceKey<Recipe<?>> recipeId,
        boolean canSubstitute,
        boolean canSubstituteFluids) {

    public static final Codec<EncodedCraftingPattern> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            AECodecs.FAULT_TOLERANT_OPTIONAL_ITEMSTACK_CODEC.listOf().fieldOf("inputs")
                    .forGetter(EncodedCraftingPattern::inputs),
            AECodecs.FAULT_TOLERANT_ITEMSTACK_CODEC.fieldOf("result").forGetter(EncodedCraftingPattern::result),
            ResourceKey.codec(Registries.RECIPE).fieldOf("recipeId").forGetter(EncodedCraftingPattern::recipeId),
            Codec.BOOL.fieldOf("canSubstitute").forGetter(EncodedCraftingPattern::canSubstitute),
            Codec.BOOL.fieldOf("canSubstituteFluids").forGetter(EncodedCraftingPattern::canSubstituteFluids))
            .apply(builder, EncodedCraftingPattern::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EncodedCraftingPattern> STREAM_CODEC = StreamCodec
            .composite(
                    ItemStack.OPTIONAL_LIST_STREAM_CODEC,
                    EncodedCraftingPattern::inputs,
                    ItemStack.STREAM_CODEC,
                    EncodedCraftingPattern::result,
                    ResourceKey.streamCodec(Registries.RECIPE),
                    EncodedCraftingPattern::recipeId,
                    ByteBufCodecs.BOOL,
                    EncodedCraftingPattern::canSubstitute,
                    ByteBufCodecs.BOOL,
                    EncodedCraftingPattern::canSubstituteFluids,
                    EncodedCraftingPattern::new);

    public boolean containsMissingContent() {
        return AEItems.MISSING_CONTENT.is(result) || inputs.stream().anyMatch(AEItems.MISSING_CONTENT::is);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;

        EncodedCraftingPattern that = (EncodedCraftingPattern) object;
        return canSubstitute == that.canSubstitute
                && canSubstituteFluids == that.canSubstituteFluids
                && ItemStack.matches(result, that.result)
                && ItemStack.listMatches(inputs, that.inputs)
                && recipeId.equals(that.recipeId);
    }

    @Override
    public int hashCode() {
        int result1 = ItemStack.hashStackList(inputs);
        result1 = 31 * result1 + ItemStack.hashItemAndComponents(result);
        result1 = 31 * result1 + recipeId.hashCode();
        result1 = 31 * result1 + Boolean.hashCode(canSubstitute);
        result1 = 31 * result1 + Boolean.hashCode(canSubstituteFluids);
        return result1;
    }
}
