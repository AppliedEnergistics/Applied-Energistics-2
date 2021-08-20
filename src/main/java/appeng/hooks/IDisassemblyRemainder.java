package appeng.hooks;

import net.minecraft.world.item.ItemStack;

/**
 * Marks items that will have a special remainder when they're used in a disassembly recipe.
 */
public interface IDisassemblyRemainder {

    default ItemStack getDisassemblyRemainder(ItemStack input) {
        return ItemStack.EMPTY;
    }

}
