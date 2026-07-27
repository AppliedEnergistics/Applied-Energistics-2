package appeng.api.upgrades;

import net.neoforged.neoforge.transfer.access.ItemAccess;

/**
 * Callback for upgrade inventories crated through {@link UpgradeInventories#forItem}.
 */
@FunctionalInterface
public interface ItemUpgradesChanged {
    /**
     * Called when the upgrades inserted into <code>stack</code> have changed. The inventory of upgrades is passed to
     * avoid having to deserialize it from NBT again to inspect installed upgrades.
     * FIXME we probably need transaction context here, since some implementations use thís to set max power based on installed upgrades
     */
    void onUpgradesChanged(ItemAccess access, IUpgradeInventory upgrades);
}
