package appeng.menu.locator;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Provides a static ItemStack, used for accessing a menu host without actually opening the menu.
 */
record StackItemLocator(ItemStack stack) implements ItemMenuHostLocator {
    @Override
    public ItemStack locateItem(Player player) {
        return stack;
    }
}
