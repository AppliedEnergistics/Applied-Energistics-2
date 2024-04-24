package appeng.api.implementations.menuobjects;

import java.util.function.Supplier;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.api.upgrades.UpgradeInventories;
import appeng.items.contents.StackDependentSupplier;
import appeng.util.inv.SupplierInternalInventory;

public final class DelegateItemUpgradeInventory extends SupplierInternalInventory<IUpgradeInventory>
        implements IUpgradeInventory {
    public DelegateItemUpgradeInventory(Supplier<ItemStack> stackSupplier) {
        super(new StackDependentSupplier<>(stackSupplier, DelegateItemUpgradeInventory::inventoryFromStack));
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
    public void readFromNBT(CompoundTag data, String subtag, HolderLookup.Provider registries) {
        getDelegate().readFromNBT(data, subtag, registries);
    }

    @Override
    public void writeToNBT(CompoundTag data, String subtag, HolderLookup.Provider registries) {
        getDelegate().writeToNBT(data, subtag, registries);
    }

    private static IUpgradeInventory inventoryFromStack(ItemStack stack) {
        if (stack.getItem() instanceof IUpgradeableItem upgradeableItem) {
            return upgradeableItem.getUpgrades(stack);
        } else {
            return UpgradeInventories.empty();
        }
    }
}
