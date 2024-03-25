package appeng.menu.locator;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Provides a static ItemStack, used for accessing a menu host without actually opening the menu.
 */
record StackItemLocator(ItemStack stack) implements ItemMenuHostLocator {
    @Override
    public ItemStack locateItem(Player player) {
        return stack;
    }

    @Override
    public @Nullable BlockHitResult hitResult() {
        return null;
    }
}
