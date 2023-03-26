package appeng.init;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.DyeableLeatherItem;

import appeng.core.definitions.AEItems;

public class InitCauldronInteraction {
    public static void init() {
        // Undye all dyeable items
        for (var def : AEItems.getItems()) {
            if (def.asItem() instanceof DyeableLeatherItem) {
                CauldronInteraction.WATER.put(def.asItem(), CauldronInteraction.DYED_ITEM);
            }
        }
    }
}
