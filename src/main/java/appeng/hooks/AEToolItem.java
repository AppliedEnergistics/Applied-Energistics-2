package appeng.hooks;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;

public interface AEToolItem {
    ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context);
}
