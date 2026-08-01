package appeng.api.upgrades;

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
            return new ItemUpgradeInventory(upgradeableItem, access, upgradeableItem.getMaxUpgrades(access),
                    changeCallback);
        }
        return EmptyUpgradeInventory.INSTANCE;
    }
}
