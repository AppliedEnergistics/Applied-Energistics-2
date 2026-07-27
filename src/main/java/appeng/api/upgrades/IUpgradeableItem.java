package appeng.api.upgrades;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.transfer.access.ItemAccess;

/**
 * Counterpart for {@link IUpgradeableObject}, but for {@link net.minecraft.world.item.Item}.
 */
@ApiStatus.NonExtendable
public interface IUpgradeableItem extends ItemLike {
    /**
     * {@return how many upgrades can at most be installed in this item}
     */
    int getMaxUpgrades(ItemAccess access);
    /*
     * /** {@return how many of the given upgrade type can at most be installed in this item}
     */
    // int getMaxUpgrades(ItemInstance item, Holder<Item> upgrade);
    // FIXME remove, I don't think it makes sense, we already have the registry for that information

    default IUpgradeInventory getUpgrades(ItemAccess access) {
        return EmptyUpgradeInventory.INSTANCE;
    }
}
