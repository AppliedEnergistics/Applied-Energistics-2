package appeng.parts.automation;

import net.minecraftforge.items.wrapper.EmptyHandler;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeInventory;

public final class EmptyUpgradeInventory extends EmptyHandler implements IUpgradeInventory {
    public static final EmptyUpgradeInventory INSTANCE = new EmptyUpgradeInventory();

    @Override
    public int getInstalledUpgrades(Upgrades u) {
        return 0;
    }

    @Override
    public int getMaxInstalled(Upgrades u) {
        return 0;
    }
}
