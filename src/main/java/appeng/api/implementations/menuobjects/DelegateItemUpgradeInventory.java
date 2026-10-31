package appeng.api.implementations.menuobjects;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.access.ItemAccess;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.api.upgrades.UpgradeInventories;
import appeng.items.contents.AccessDependentSupplier;
import appeng.util.inv.SupplierInternalInventory;

public final class DelegateItemUpgradeInventory extends SupplierInternalInventory<IUpgradeInventory>
        implements IUpgradeInventory {
    public DelegateItemUpgradeInventory(ItemAccess access) {
        super(new AccessDependentSupplier<>(access, DelegateItemUpgradeInventory::inventoryFromAccess));
    }

    @Override
    public ItemLike getUpgradableItem() {
        return getDelegate().getUpgradableItem();
    }

    @Override
    public int getInstalledUpgrades(ItemLike u) {
        return getDelegate().getInstalledUpgrades(u);
    }

    @Override
    public int getMaxInstalled(ItemLike u) {
        return getDelegate().getMaxInstalled(u);
    }

    @Override
    public void readFromNBT(ValueInput input, String subtag) {
        getDelegate().readFromNBT(input, subtag);
    }

    @Override
    public void writeToNBT(ValueOutput output, String subtag) {
        getDelegate().writeToNBT(output, subtag);
    }

    private static IUpgradeInventory inventoryFromAccess(ItemAccess access) {
        if (access.getResource().getItem() instanceof IUpgradeableItem upgradeableItem) {
            return upgradeableItem.getUpgrades(access);
        } else {
            return UpgradeInventories.empty();
        }
    }
}
