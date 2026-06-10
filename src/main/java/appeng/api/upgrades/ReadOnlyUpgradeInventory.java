package appeng.api.upgrades;

import net.minecraft.world.level.ItemLike;

/**
 * Provides read-only access to an inventory containing upgrades. This allows the upgrades
 * of items to be read without having to account for write-access.
 */
public interface ReadOnlyUpgradeInventory {
    /**
     * Item representation of the upgradable object this inventory is managing upgrades for.
     */
    ItemLike getUpgradableItem();

    /**
     * @return Checks if the given upgrade card is installed in this inventory.
     */
    default boolean isInstalled(ItemLike upgradeCard) {
        return getInstalledUpgrades(upgradeCard) > 0;
    }

    /**
     * determine how many of an upgrade are installed.
     */
    int getInstalledUpgrades(ItemLike u);

    /**
     * determine how many of an upgrade can be installed.
     */
    int getMaxInstalled(ItemLike u);

}
