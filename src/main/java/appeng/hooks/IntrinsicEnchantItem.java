package appeng.hooks;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Allows items to have an "intrinsic" enchant level in places where a specific enchant is checked.
 */
public interface IntrinsicEnchantItem {
    int getIntrinsicEnchantLevel(ItemStack stack, Holder<Enchantment> enchantment);
}
