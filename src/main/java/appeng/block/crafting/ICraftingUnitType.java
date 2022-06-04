package appeng.block.crafting;

import net.minecraft.world.item.Item;

/**
 * Implemented by classes/enums meant to provide their own types of crafting CPU blocks.
 */
public interface ICraftingUnitType {

    /**
     * @return the capacity of a given crafting <i>storage</i> block in bytes (should be 0 if not storage).
     */
    int getStorageBytes();

    /**
     * @return how many co-processors a crafting unit provides. For lag-mitigation purposes, a hard-coded limit has been
     *         set of 16 threads for any given co-processing unit block.
     */
    int getAcceleratorThreads();

    /**
     * @return the BlockItem for the crafting storage block corresponding with its type for block-entity purposes.
     */
    Item getItemFromType();
}
