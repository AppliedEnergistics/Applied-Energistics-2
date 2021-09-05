package appeng.helpers;

import java.util.function.Predicate;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.api.inventories.InternalInventory;
import appeng.util.Platform;

public final class Inventories {

    private Inventories() {
    }

    public static void clear(InternalInventory inv) {
        for (int x = 0; x < inv.size(); x++) {
            inv.setItemDirect(x, ItemStack.EMPTY);
        }
    }

    public static void copy(CraftingContainer from, InternalInventory to, boolean deepCopy) {
        for (int i = 0; i < Math.min(from.getContainerSize(), to.size()); ++i) {
            to.setItemDirect(i, deepCopy ? from.getItem(i).copy() : from.getItem(i));
        }
    }

}
