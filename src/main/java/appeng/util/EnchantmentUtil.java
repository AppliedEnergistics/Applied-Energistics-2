package appeng.util;

import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Allows serializing enchantments to and from NBT data in the same format as {@link ItemStack}.
 */
public final class EnchantmentUtil {
    private EnchantmentUtil() {
    }

    /**
     * Writes a list of enchantments to the given tag the same way as
     * {@link EnchantmentHelper#setEnchantments(Map, ItemStack)} would.
     */
    public static void setEnchantments(CompoundTag tag, Map<Enchantment, Integer> enchantments) {
        ListTag enchantList = new ListTag();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (enchantment == null)
                continue;
            int level = entry.getValue();
            enchantList.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(enchantment), level));
        }
        tag.put("Enchantments", enchantList);
    }
}
