package appeng.hooks;

import net.minecraft.world.item.ItemStack;

// TODO FABRIC 117 Actually implement a mixin that makes use of this.
public interface ICustomDurabilityBar {

    boolean showDurabilityBar(ItemStack stack);

    double getDurabilityForDisplay(final ItemStack is);

}
