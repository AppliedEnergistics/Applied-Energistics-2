package appeng.api.util;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public final class ItemHashHelper {
    private ItemHashHelper() {
    }

    public static long hashItemAndComponents(@Nullable ItemStack stack) {
        long r = 6454648847654032877L;
        if (stack != null) {
            r += 4362920881865638517L * stack.getItem().hashCode();
            r ^= r >> 32;
            r = r * 0xd4ea53368b5cf33dL + hashComponentPatch(stack.getComponentsPatch());
            r *= 2817661010293465519L;
        }
        return r ^ (r >> 32);
    }

    public static long hashComponentPatch(@Nullable DataComponentPatch componentPatch) {
        long r = 6454648847654032877L;
        if (componentPatch != null && !componentPatch.isEmpty()) {
            var entries = componentPatch.entrySet();
            for (var entry : entries) {
                var kh = ((0xAF5A_4C77L * Integer.toUnsignedLong(entry.getKey().hashCode())) << 32) | 0xEF09_3255L;
                var vh = 5406832182879323197L * (long) entry.getValue().hashCode();
                kh *= vh;
                kh ^= vh >> 32;
                r += kh;
            }
        }
        return r ^ (r >> 32);
    }
}
