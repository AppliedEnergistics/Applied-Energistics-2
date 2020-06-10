package appeng.recipes.handlers;

import net.minecraft.item.ItemStack;

/**
 * Describes an optional result of a grinder recipe.
 */
public class GrinderOptionalResult {
    private final float chance;
    private final ItemStack result;

    public GrinderOptionalResult(float chance, ItemStack result) {
        this.chance = chance;
        this.result = result;
    }

    /**
     * Chance to occur from 0 (0%) to 1 (100%).
     */
    public float getChance() {
        return chance;
    }

    public ItemStack getResult() {
        return result;
    }
}
