package appeng.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.storage.data.IAEItemStack;

// TODO FABRIC 117: No concept of itemstack-sensitive container items.
public final class CraftingRemainders {

    public static boolean hasRemainder(ItemStack stackInSlot) {
        return stackInSlot.getItem().hasCraftingRemainingItem();
    }

    /**
     * Gets the container item for the given item or EMPTY. A container item is what remains when the item is used for
     * crafting, i.E. the empty bucket for a bucket of water.
     */
    public static ItemStack getRemainder(final ItemStack stackInSlot) {
        if (stackInSlot == null) {
            return ItemStack.EMPTY;
        }

        final Item i = stackInSlot.getItem();
        if (i == null || !i.hasCraftingRemainingItem()) {
            if (stackInSlot.getCount() > 1) {
                stackInSlot.setCount(stackInSlot.getCount() - 1);
                return stackInSlot;
            }
            return ItemStack.EMPTY;
        }

        ItemStack ci = new ItemStack(i.getCraftingRemainingItem());
        if (!ci.isEmpty() && ci.isDamageableItem() && ci.getDamageValue() == ci.getMaxDamage()) {
            ci = ItemStack.EMPTY;
        }

        return ci;
    }

    public static IAEItemStack getRemainder(IAEItemStack stackInSlot) {
        return IAEItemStack.of(getRemainder(stackInSlot.createItemStack()));
    }
}
