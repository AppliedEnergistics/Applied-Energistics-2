package appeng.api.networking.crafting;

public enum CalculationStrategy {
    /**
     * If the exact requested amount cannot be crafted, create a {@link ICraftingPlan} containing the missing items.
     */
    REPORT_MISSING_ITEMS,
    /**
     * Try to craft less items than requested. Will try to craft as many items as possible. If even {@code 1} cannot be
     * crafted, fall back to {@link #REPORT_MISSING_ITEMS}.
     */
    CRAFT_LESS,
}
