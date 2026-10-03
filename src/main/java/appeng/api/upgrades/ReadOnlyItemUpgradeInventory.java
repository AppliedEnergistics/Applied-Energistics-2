package appeng.api.upgrades;

import java.util.Iterator;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.ItemLike;

import appeng.api.ids.AEComponents;

public class ReadOnlyItemUpgradeInventory implements ReadOnlyUpgradeInventory<ItemStackTemplate> {
    private final IUpgradeableItem item;
    private final int upgrades;
    private final ItemContainerContents contents;

    public ReadOnlyItemUpgradeInventory(IUpgradeableItem item, DataComponentGetter components) {
        this.item = item;
        upgrades = item.getMaxUpgrades(components);
        contents = components.getOrDefault(AEComponents.UPGRADES, ItemContainerContents.EMPTY);
    }

    @Override
    public IUpgradeableItem getUpgradableItem() {
        return item;
    }

    @Override
    public int getInstalledUpgrades(ItemLike u) {
        int count = 0;
        for (var is : this) {
            if (is.is(u.asItem())) {
                count++;
            }
        }
        return Math.min(count, getMaxInstalled(u));
    }

    @Override
    public int getMaxInstalled(ItemLike u) {
        return upgrades;
    }

    @Override
    public Iterator<ItemStackTemplate> iterator() {
        return contents.nonEmptyItems().iterator();
    }
}
