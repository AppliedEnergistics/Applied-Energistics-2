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
     * @return whether a given crafting unit type corresponds with a crafting <i>accelerator</i>.
     */
    boolean isAccelerator();

    /**
     * @return whether a given crafting unit type corresponds with a crafting <i>monitor</i>.
     */
    boolean isStatus();

    /**
     * @return whether a given crafting unit type corresponds with a crafting <i>storage</i> block. By default, anything
     *         with a (positive) non-zero storage capacity in bytes is assumed to be a storage block.
     */
    default boolean isStorage() {
        return this.getStorageBytes() > 0;
    }

    /**
     * @return the BlockItem for the crafting storage block corresponding with its type for block-entity purposes.
     */
    Item getItemFromType();
}
