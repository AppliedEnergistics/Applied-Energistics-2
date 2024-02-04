package appeng.api.implementations.menuobjects;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.api.upgrades.UpgradeInventories;
import appeng.util.inv.SupplierInternalInventory;

public final class DelegateItemUpgradeInventory extends SupplierInternalInventory<IUpgradeInventory>
        implements IUpgradeInventory {
    public DelegateItemUpgradeInventory(Supplier<ItemStack> stackSupplier) {
        super(new UpgradeInventorySupplier(stackSupplier));
    }

    @Override
    public ItemLike getUpgradableItem() {
        return null;
    }

    @Override
    public int getInstalledUpgrades(ItemLike u) {
        return 0;
    }

    @Override
    public int getMaxInstalled(ItemLike u) {
        return 0;
    }

    @Override
    public void readFromNBT(CompoundTag data, String subtag) {

    }

    @Override
    public void writeToNBT(CompoundTag data, String subtag) {

    }

    private final static class UpgradeInventorySupplier implements Supplier<IUpgradeInventory> {
        private final Supplier<ItemStack> stackSupplier;

        private IUpgradeInventory currentInv;
        private ItemStack currentStack;

        public UpgradeInventorySupplier(Supplier<ItemStack> stackSupplier) {
            this.stackSupplier = stackSupplier;
        }

        @Override
        public IUpgradeInventory get() {
            var stack = stackSupplier.get();
            if (currentStack != stack) {
                currentStack = stack;
                if (stack.getItem() instanceof IUpgradeableItem upgradeableItem) {
                    this.currentInv = upgradeableItem.getUpgrades(stack);
                } else {
                    this.currentInv = UpgradeInventories.empty();
                }
            }
            return currentInv;
        }
    }
}
