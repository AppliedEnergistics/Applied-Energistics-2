package appeng.hooks;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public interface AEToolItem {
    InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context);
}
