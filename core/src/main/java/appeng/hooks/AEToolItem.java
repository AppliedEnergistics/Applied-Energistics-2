package appeng.hooks;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

public interface AEToolItem {
    ActionResult onItemUseFirst(ItemStack stack, ItemUsageContext context);
}
