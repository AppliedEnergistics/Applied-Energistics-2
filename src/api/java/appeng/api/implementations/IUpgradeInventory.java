package appeng.api.implementations;

import javax.annotation.Nonnegative;

import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Upgrades;

/**
 * This specialized inventory can be used to insert and extract upgrade cards into AE2 machines. Only upgrades supported
 * by the machine can be inserted.
 */
public interface IUpgradeInventory extends IItemHandler {

    /**
     * determine how many of an upgrade are installed.
     */
    @Nonnegative
    int getInstalledUpgrades(Upgrades u);

    /**
     * determine how many of an upgrade can be installed.
     */
    @Nonnegative
    int getMaxInstalled(Upgrades u);

}
