package appeng.hooks;

import net.minecraft.world.item.ItemStack;

// TODO FABRIC 117: Actually implement this hook
public interface ICustomReequipAnimation {

    boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged);

}
