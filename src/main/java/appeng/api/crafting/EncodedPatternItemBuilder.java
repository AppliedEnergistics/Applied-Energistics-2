package appeng.api.crafting;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.Item;

import appeng.crafting.pattern.EncodedPatternItem;

public final class EncodedPatternItemBuilder<T extends IPatternDetails> {
    private final EncodedPatternDecoder<? extends T> decoder;
    private @Nullable InvalidPatternTooltipStrategy invalidPatternDescription;
    private Item.Properties properties = new Item.Properties().stacksTo(1);

    EncodedPatternItemBuilder(EncodedPatternDecoder<? extends T> decoder) {
        this.decoder = Objects.requireNonNull(decoder, "decoder");
    }

    /**
     * When a pattern can no longer be decoded successfully, a custom strategy can be used to still provide the player
     * with some useful information about the invalid pattern (such as: what was it crafting? with which ingredients?)
     */
    public EncodedPatternItemBuilder<T> invalidPatternTooltip(InvalidPatternTooltipStrategy strategy) {
        this.invalidPatternDescription = strategy;
        return this;
    }

    /**
     * Overrides the item properties of the generated item. The default properties simply make them unstackable.
     */
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
                invalidPatternDescription);
    }
}
