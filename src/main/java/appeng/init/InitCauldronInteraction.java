package appeng.init;

import net.minecraft.core.cauldron.CauldronInteractions;

import appeng.core.definitions.AEItems;
import appeng.items.tools.MemoryCardItem;
import appeng.items.tools.powered.AbstractPortableCell;

public class InitCauldronInteraction {
    public static void init() {
        // Undye all dyeable items
        for (var def : AEItems.getItems()) {
            if (def.asItem() instanceof AbstractPortableCell || def.asItem() instanceof MemoryCardItem) {
                CauldronInteractions.WATER.put(def.asItem(), CauldronInteractions::dyedItemIteration);
            }
        }
    }
}
