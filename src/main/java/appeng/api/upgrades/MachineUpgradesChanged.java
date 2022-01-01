package appeng.api.upgrades;

/**
 * Callback for upgrade inventories crated through {@link UpgradeInventories#forMachine}.
 */
@FunctionalInterface
public interface MachineUpgradesChanged {
    void onUpgradesChanged();
}
