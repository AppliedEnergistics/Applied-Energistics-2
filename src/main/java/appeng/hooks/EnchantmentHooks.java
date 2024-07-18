package appeng.hooks;

import net.minecraft.world.item.Item;

import appeng.core.definitions.AEParts;
import appeng.items.parts.PartItem;

public final class EnchantmentHooks {
    private EnchantmentHooks() {
    }

    public static boolean isDiggerEnchantable(Item item) {
        // The instanceof check here tries to avoid class-init of AEParts until it is called for an actual AE item
        // Otherwise it might try to initialize all AE Parts
        if (item instanceof PartItem) {
            return isAnnihilationPlane(item);
        }
        return false;
    }

    private static boolean isAnnihilationPlane(Item item) {
        return item == AEParts.ANNIHILATION_PLANE.asItem();
    }
}
