package appeng.api.upgrades;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Counterpart for {@link IUpgradeableObject}, but for {@link net.minecraft.world.item.Item}.
 */
@ApiStatus.NonExtendable
public interface IUpgradeableItem extends ItemLike {
    /**
     * Used to edit the upgrades on your item, should have a capacity of 0-8 slots. You are also responsible for
     * implementing the valid checks, and any storage/usage of them.
     * <p>
     * onInventoryChange will be called when saving is needed.
     */
    default IUpgradeInventory getUpgrades(ItemStack stack) {
        return EmptyUpgradeInventory.INSTANCE;
    }
}
