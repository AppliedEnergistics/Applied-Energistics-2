package appeng.api.upgrades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.items.materials.EnergyCardItem;
import appeng.items.materials.UpgradeCardItem;

/**
 * Manages available upgrades for AE machines, parts and items.
 */
@ThreadSafe
public final class Upgrades {
    // Key is the upgrade cards item
    private static final Map<Item, List<Association>> ASSOCIATIONS = new IdentityHashMap<>();
    // Key is the upgrade cards item
    private static final Map<Item, List<Component>> UPGRADE_CARD_TOOLTIP_LINES = new IdentityHashMap<>();

    private Upgrades() {
    }

    /**
     * Same as {@link #add(ItemLike, ItemLike, int, String)}, but without a tooltip group.
     */
    public static synchronized void add(ItemLike upgradeCard, ItemLike upgradableObject, int maxSupported) {
        add(upgradeCard, upgradableObject, maxSupported, null);
    }

    /**
     * Associates an upgrade card item with an upgradable object and registers the maximum number of this upgrade that
     * the upgradable object supports.
     *
     * @param tooltipGroup If not null, the upgradable object will be shown using this translation key in the tooltip of
     *                     the upgrade card, grouped together with all other upgradable objects using the same
     *                     translation key.
     */
    public static synchronized void add(ItemLike upgradeCard, ItemLike upgradableObject, int maxSupported,
            @Nullable String tooltipGroup) {
        Item item = upgradableObject.asItem();
        Block block;
        if (item instanceof BlockItem blockItem) {
            block = blockItem.getBlock();
        } else {
            block = null;
        }

        var translatedTooltipGroup = tooltipGroup != null ? Component.translatable(tooltipGroup) : null;

        var association = new Association(upgradeCard.asItem(), item, block, maxSupported, translatedTooltipGroup);
        ASSOCIATIONS.computeIfAbsent(association.upgradeCard(), ignored -> new ArrayList<>())
                .add(association);
        // Clear tooltip cache
        UPGRADE_CARD_TOOLTIP_LINES.remove(association.upgradeCard());
    }

    /**
     * Returns how many of the given item can be installed in the given upgradable item. Returns 0 if none are allowed.
     */
    public static synchronized int getMaxInstallable(ItemLike card, ItemLike upgradableItem) {
        var associations = ASSOCIATIONS.get(card.asItem());
        if (associations == null) {
            return 0;
        }

        for (var association : associations) {
            if (association.upgradableItem() == upgradableItem.asItem()) {
                return association.maxCount();
            }
        }

        return 0;
    }

    /**
     * Returns a cumulative power multiplier based on the amount of "energy cards" fitted onto a tool. Returns 0 if no
     * such cards exist within the tool's upgrade inventory.
     */
    public static synchronized int getMaxPowerMultiplier(IUpgradeInventory upgrades) {
        int multiplier = 0;
        for (var card : upgrades) {
            if (card.getItem() instanceof EnergyCardItem ec) {
                multiplier += ec.getEnergyMultiplier();
            }
        }
        return multiplier;
    }

    /**
     * Creates a new upgrade item which can be used to receive automated tooltips and allow custom upgrades to be added
     * to AE2's toolbelt in the network tool.
     */
    public static Item createUpgradeCardItem(Item.Properties p) {
        return new UpgradeCardItem(p);
    }

    /**
     * Checks if the given item is an upgrade card for *any* other object.
     */
    public static boolean isUpgradeCardItem(ItemLike card) {
        return card.asItem() instanceof UpgradeCardItem;
    }

    /**
     * Checks if the given item stack uses an item created using {@link #createUpgradeCardItem}.
     *
     * @see #isUpgradeCardItem(ItemLike)
     */
    public static boolean isUpgradeCardItem(ItemStack stack) {
        return stack.getItem() instanceof UpgradeCardItem;
    }

    /**
     * Gets a list of lines describing where an upgrade card can be used.
     */
    public static synchronized List<Component> getTooltipLinesForCard(ItemLike card) {
        return UPGRADE_CARD_TOOLTIP_LINES.computeIfAbsent(card.asItem(), Upgrades::createTooltipLinesForCard);
    }

    /**
     * Gets a list of tooltip lines describing which upgrades and how many of each are supported by a given upgradable
     * item. Returns an empty list if there are no upgrades available for this item.
     */
    public static synchronized List<Component> getTooltipLinesForMachine(ItemLike upgradableItemLike) {
        var upgradableItem = upgradableItemLike.asItem();

        var result = new ArrayList<Component>();

        for (var cardAssociations : ASSOCIATIONS.values()) {
            for (var association : cardAssociations) {
                if (association.upgradableItem() == upgradableItem) {
                    result.add(GuiText.CompatibleUpgrade
                            .text(association.upgradeCard().getDescription(), association.maxCount())
                            .withStyle(ChatFormatting.GRAY));
                    break;
                }
            }
        }

        return result;
    }

    private record Association(Item upgradeCard,
            Item upgradableItem,
            @Nullable Block upgradableBlock,
            int maxCount,
            @Nullable Component tooltipGroup) {
    }

    private static List<Component> createTooltipLinesForCard(Item card) {
        var associations = new ArrayList<>(ASSOCIATIONS.getOrDefault(card, Collections.emptyList()));
        associations.sort(Comparator.comparingInt(o -> o.maxCount));
        var supportedTooltipLines = new ArrayList<Component>(associations.size());

        // Use a separate set because the final text will include numbers
        Set<Component> namesAdded = new HashSet<>();

        for (int i = 0; i < associations.size(); i++) {
            var association = associations.get(i);
            Component name = association.upgradableItem().getDescription();

            // If the group was already added by a previous item, skip this
            if (association.tooltipGroup() != null && namesAdded.contains(association.tooltipGroup())) {
                continue;
            }

            // If any of the following items would be of the same group, use the group name
            // instead
            if (association.tooltipGroup() != null) {
                for (int j = i + 1; j < associations.size(); j++) {
                    var otherGroup = associations.get(j).tooltipGroup();
                    if (association.tooltipGroup().equals(otherGroup)) {
                        name = association.tooltipGroup();
                        break;
                    }
                }
            }

            if (namesAdded.add(name)) {
                // append the supported count only if its > 1
                Component base = (name.copy().withStyle(Tooltips.NORMAL_TOOLTIP_TEXT));
                Component main = base;

                if (association.maxCount() > 1) {
                    main = Tooltips.of(base, Tooltips.of(" ("),
                            Tooltips.ofUnformattedNumber(association.maxCount()), Tooltips.of(")"));

                }
                supportedTooltipLines.add(main);
            }
        }

        return supportedTooltipLines;
    }

}
