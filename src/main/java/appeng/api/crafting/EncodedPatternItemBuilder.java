package appeng.api.crafting;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.Item;

import appeng.crafting.pattern.EncodedPatternItem;

public final class EncodedPatternItemBuilder<T extends IPatternDetails> {
    private final EncodedPatternDecoder<? extends T> decoder;
    private @Nullable EncodedPatternRecovery recovery;
    private Item.Properties properties = new Item.Properties().stacksTo(1);

    EncodedPatternItemBuilder(EncodedPatternDecoder<? extends T> decoder) {
        this.decoder = Objects.requireNonNull(decoder, "decoder");
    }

    /**
     * Enables automated recovery of encoded patterns when the decoder throws an exception while decoding them.
     * <p>
     * The recovery function is given the CompoundTag of the encoded pattern. It should attempt to recover the original
     * pattern from the NBT and re-encode it in the same tag so that the decoder will be able to decode it.
     * <p>
     * If it succeeds in doing so, it should return true.
     */
    public EncodedPatternItemBuilder<T> autoRecovery(EncodedPatternRecovery recovery) {
        this.recovery = recovery;
        return this;
    }

    public EncodedPatternItemBuilder<T> itemProperties(Item.Properties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Builds the pattern item and returns it. Register this item within your mod, and ensure:
     *
     * <ul>
     * <li>>You need to provide an item model.</li>
     * <li>>You need to provide an item name translation.</li>
     * </ul>
     */
    public Item build() {
        return new EncodedPatternItem<>(
                properties,
                decoder,
                recovery);
    }
}
