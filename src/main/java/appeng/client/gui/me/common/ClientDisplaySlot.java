package appeng.client.gui.me.common;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.GenericStack;

/**
 * A slot to showcase an item on the client-side.
 */
public class ClientDisplaySlot extends ClientReadOnlySlot {
    private final ItemStack item;

    public ClientDisplaySlot(@Nullable GenericStack stack) {
        item = GenericStack.wrapInItemStack(stack);
    }

    @Override
    public ItemStack getItem() {
        return item;
    }
}
