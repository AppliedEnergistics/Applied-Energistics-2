package appeng.api.upgrades;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.world.level.ItemLike;

/**
 * Counterpart for {@link IUpgradeableObject}, but for {@link net.minecraft.world.item.Item}.
 */
@ApiStatus.NonExtendable
public interface IUpgradeableItem extends ItemLike {
    /**
     * {@return how many of the given upgrade type can at most be installed in this item}
     */
    int getMaxUpgrades(ItemInstance item, Holder<Item> upgrade);
}
