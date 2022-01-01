package appeng.api.upgrades;

import javax.annotation.Nullable;

import net.minecraft.world.level.ItemLike;

import appeng.api.inventories.InternalInventory;

class MachineUpgradeInventory extends UpgradeInventory {
    @Nullable
    private final MachineUpgradesChanged changeCallback;

    public MachineUpgradeInventory(ItemLike item, int slots, @Nullable MachineUpgradesChanged changeCallback) {
        super(item.asItem(), slots);
        this.changeCallback = changeCallback;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        super.onChangeInventory(inv, slot);

        if (changeCallback != null) {
            changeCallback.onUpgradesChanged();
        }
    }
}
