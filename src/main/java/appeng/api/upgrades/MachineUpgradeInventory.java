package appeng.api.upgrades;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.ItemLike;

import appeng.util.inv.AppEngInternalInventory;

class MachineUpgradeInventory extends UpgradeInventory {
    @Nullable
    private final MachineUpgradesChanged changeCallback;

    public MachineUpgradeInventory(ItemLike item, int slots, @Nullable MachineUpgradesChanged changeCallback) {
        super(item.asItem(), slots);
        this.changeCallback = changeCallback;
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        super.onChangeInventory(inv, slot);

        if (changeCallback != null) {
            changeCallback.onUpgradesChanged();
        }
    }
}
