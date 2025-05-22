package appeng.crafting.pattern;

import java.util.LinkedHashMap;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

/**
 * Helpers that apply to both processing and crafting patterns.
 */
final class AEPatternHelper {
    private AEPatternHelper() {
    }

    /**
     * Given an array of potentially null stacks, which can include multiples of the same type, produce an array that
     * has no null elements and only contains every input type once, while preserving order.
     */
    public static GenericStack[] condenseStacks(GenericStack[] sparseInput) {
        // Use a linked map to preserve ordering.
        var map = new LinkedHashMap<AEKey, Long>();

        for (var input : sparseInput) {
            if (input != null) {
                map.merge(input.what(), input.amount(), Long::sum);
            }
        }

        if (map.isEmpty()) {
            throw new IllegalStateException("No pattern here!");
        }

        GenericStack[] out = new GenericStack[map.size()];
        int i = 0;
        for (var entry : map.entrySet()) {
            out[i++] = new GenericStack(entry.getKey(), entry.getValue());
        }
        return out;
    }
}
