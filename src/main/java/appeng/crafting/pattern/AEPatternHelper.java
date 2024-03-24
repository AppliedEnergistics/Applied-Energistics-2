package appeng.crafting.pattern;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
    public static List<GenericStack> condenseStacks(List<GenericStack> sparseInput) {
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

        List<GenericStack> out = new ArrayList<>(map.size());
        for (var entry : map.entrySet()) {
            out.add(new GenericStack(entry.getKey(), entry.getValue()));
        }
        return out;
    }
}
