package appeng.api.upgrades;

import appeng.api.ids.AEComponents;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.transfer.access.ItemAccess;

/**
 * Utilities for creating {@link IUpgradeInventory upgrade inventories}.
 */
public final class UpgradeInventories {
    private UpgradeInventories() {
    }

    /**
     * Returns an empty, read-only upgrade inventory.
     */
    public static IUpgradeInventory empty() {
        return EmptyUpgradeInventory.INSTANCE;
    }

    /**
     * Creates an upgrade inventory that manages the upgrades inserted into an upgradable item stack such as portable
     * cells or wireless terminals. Ensure to save your machine to disk when the upgrades change using the given
     * callback.
     */
    public static IUpgradeInventory forMachine(ItemLike machineType, int maxUpgrades,
            MachineUpgradesChanged changeCallback) {
        return new MachineUpgradeInventory(machineType, maxUpgrades, changeCallback);
    }

    /**
     * Creates an upgrade inventory that manages the upgrades inserted into an upgradable item stack such as portable
     * cells or wireless terminals. Changes to the upgrades are immediately written into the given stack's NBT.
     */
    public static IUpgradeInventory forItem(ItemAccess access) {
        return forItem(access, null);
    }

    /**
     * Same as {@link #forItem(ItemAccess)}, but with change notifications.
     */
    public static IUpgradeInventory forItem(ItemAccess access, ItemUpgradesChanged changeCallback) {
        var currentItem = access.getResource().getItem();
        if (currentItem instanceof IUpgradeableItem upgradeableItem) {
            return new ItemUpgradeInventory(upgradeableItem, access, upgradeableItem.getMaxUpgrades(access), changeCallback);
        }
        return EmptyUpgradeInventory.INSTANCE;
    }

    /**
     * Same as {@link #forItem(ItemAccess)}, but read only.
     */
    public static ReadOnlyUpgradeInventory forReadOnlyItem(ItemInstance item) {
        // FIXME finish or remove this
        var upgrades = item.getOrDefault(AEComponents.UPGRADES, ItemContainerContents.EMPTY);

        return new ReadOnlyUpgradeInventory() {
            @Override
            public ItemLike getUpgradableItem() {
                return item.typeHolder().value();
            }

            @Override
            public int getInstalledUpgrades(ItemLike u) {
                return 0;
            }

            @Override
            public int getMaxInstalled(ItemLike u) {
                return 0;
            }
        };
    }
}
