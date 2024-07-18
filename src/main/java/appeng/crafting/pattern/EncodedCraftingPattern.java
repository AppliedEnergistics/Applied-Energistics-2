package appeng.crafting.pattern;

import java.util.Iterator;
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
        return AEItems.MISSING_CONTENT.is(result) || inputs.stream().anyMatch(AEItems.MISSING_CONTENT::is);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hash = 1;

        for (ItemStack i : inputs) {
            hash = prime * hash + (i == null ? 0 : ItemStack.hashItemAndComponents(i) + i.getCount());
        }
        hash = prime * hash + (this.result == null ? 0 : ItemStack.hashItemAndComponents(result) + result.getCount());
        hash = prime * hash + (this.recipeId == null ? 0 : recipeId.hashCode());
        hash = prime * hash + (canSubstitute ? 1 : 0);
        hash = prime * hash + (canSubstituteFluids ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final EncodedCraftingPattern other = (EncodedCraftingPattern) o;

        if (inputs.size() != other.inputs.size())
            return false;
        Iterator<ItemStack> iterThis = inputs.iterator();
        Iterator<ItemStack> iterOther = other.inputs.iterator();
        while (iterThis.hasNext() && iterOther.hasNext()) {
            ItemStack i1 = iterThis.next();
            ItemStack i2 = iterOther.next();
            if (!(ItemStack.isSameItemSameComponents(i1, i2) && i1.getCount() == i2.getCount()))
                return false;
        }
        return ItemStack.isSameItemSameComponents(result, other.result) && result.getCount() == other.result.getCount()
                && recipeId.equals(other.recipeId) && canSubstitute == other.canSubstitute
                && canSubstituteFluids == other.canSubstituteFluids;
    }
}
