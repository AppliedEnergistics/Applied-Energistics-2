package appeng.crafting.pattern;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import appeng.api.storage.GenericStack;

/**
 * Helpers that apply to both processing and crafting patterns.
 */
final class AEPatternHelper {
    private static final Comparator<GenericStack> COMPARE_BY_STACKSIZE = (left, right) -> Long
            .compare(right.amount(), left.amount());

    private AEPatternHelper() {
    }

    /**
     * Given an array of potentially null stacks, which can include multiples of the same type, produce an array that
     * has no null elements and only contains every input type once.
     */
    public static GenericStack[] condenseStacks(GenericStack[] sparseInput) {
        var merged = Arrays.stream(sparseInput).filter(Objects::nonNull)
                .collect(Collectors.toMap(GenericStack::what, Function.identity(), GenericStack::sum))
                .values().stream().sorted(COMPARE_BY_STACKSIZE).collect(ImmutableList.toImmutableList());

        if (merged.isEmpty()) {
            throw new IllegalStateException("No pattern here!");
        }

        return merged.toArray(new GenericStack[0]);
    }
}
