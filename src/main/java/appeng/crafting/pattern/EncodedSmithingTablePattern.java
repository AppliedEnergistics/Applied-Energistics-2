package appeng.crafting.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import appeng.core.definitions.AEItems;

public record EncodedSmithingTablePattern(
        ItemStack template,
        ItemStack base,
        ItemStack addition,
        ItemStack resultItem,
        boolean canSubstitute,
        ResourceLocation recipeId) {

    public static final Codec<EncodedSmithingTablePattern> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ItemStack.CODEC.fieldOf("template").forGetter(EncodedSmithingTablePattern::template),
            ItemStack.CODEC.fieldOf("base").forGetter(EncodedSmithingTablePattern::base),
            ItemStack.CODEC.fieldOf("addition").forGetter(EncodedSmithingTablePattern::addition),
            ItemStack.CODEC.fieldOf("resultItem").forGetter(EncodedSmithingTablePattern::resultItem),
            Codec.BOOL.fieldOf("canSubstitute").forGetter(EncodedSmithingTablePattern::canSubstitute),
            ResourceLocation.CODEC.fieldOf("recipeId").forGetter(EncodedSmithingTablePattern::recipeId))
            .apply(builder, EncodedSmithingTablePattern::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EncodedSmithingTablePattern> STREAM_CODEC = StreamCodec
            .composite(
                    ItemStack.STREAM_CODEC,
                    EncodedSmithingTablePattern::template,
                    ItemStack.STREAM_CODEC,
                    EncodedSmithingTablePattern::base,
                    ItemStack.STREAM_CODEC,
                    EncodedSmithingTablePattern::addition,
                    ItemStack.STREAM_CODEC,
                    EncodedSmithingTablePattern::resultItem,
                    ByteBufCodecs.BOOL,
                    EncodedSmithingTablePattern::canSubstitute,
                    ResourceLocation.STREAM_CODEC,
                    EncodedSmithingTablePattern::recipeId,
                    EncodedSmithingTablePattern::new);

    public boolean containsMissingContent() {
        return AEItems.MISSING_CONTENT.is(template)
                || AEItems.MISSING_CONTENT.is(base)
                || AEItems.MISSING_CONTENT.is(addition)
                || AEItems.MISSING_CONTENT.is(resultItem);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;

        EncodedSmithingTablePattern that = (EncodedSmithingTablePattern) object;
        return canSubstitute == that.canSubstitute
                && ItemStack.matches(base, that.base)
                && ItemStack.matches(template, that.template)
                && ItemStack.matches(addition, that.addition)
                && ItemStack.matches(resultItem, that.resultItem)
                && recipeId.equals(that.recipeId);
    }

    @Override
    public int hashCode() {
        int result = ItemStack.hashItemAndComponents(template);
        result = 31 * result + ItemStack.hashItemAndComponents(base);
        result = 31 * result + ItemStack.hashItemAndComponents(addition);
        result = 31 * result + ItemStack.hashItemAndComponents(resultItem);
        result = 31 * result + Boolean.hashCode(canSubstitute);
        result = 31 * result + recipeId.hashCode();
        return result;
    }
}
