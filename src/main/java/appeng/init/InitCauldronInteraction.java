package appeng.init;

import appeng.core.definitions.AEItems;
import appeng.items.tools.MemoryCardItem;
import appeng.items.tools.powered.AbstractPortableCell;
import net.minecraft.core.cauldron.CauldronInteractions;

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
