package appeng.api.crafting;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.Item;

import appeng.crafting.pattern.EncodedPatternItem;

public final class EncodedPatternItemBuilder<T extends IPatternDetails> {
    private final EncodedPatternDecoder<? extends T> decoder;
    private @Nullable InvalidPatternTooltipStrategy invalidPatternDescription;

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
     * Builds the pattern item and returns it. Register this item within your mod, and ensure:
     *
     * <ul>
     * <li>>You need to provide an item model.</li>
     * <li>>You need to provide an item name translation.</li>
     * </ul>
     */
    public Item build(Item.Properties p) {
        return new EncodedPatternItem<>(
                p.stacksTo(1),
                decoder,
                invalidPatternDescription);
    }
}
