package appeng.libs.micromark;

import java.util.ArrayList;
import java.util.List;

public final class Extensions {
    private Extensions() {
    }

    /**
     * Combine several syntax extensions into one.
     *
     * @param extensions List of syntax extensions.
     * @return A single combined extension.
     */
    public static Extension combineExtensions(List<Extension> extensions) {
        Extension all = new Extension();
        var index = -1;

        while (++index < extensions.size()) {
            syntaxExtension(all, extensions.get(index));
        }

        return all;
    }

    /**
     * Merge `extension` into `all`.
     */
    public static void syntaxExtension(Extension all, Extension extension) {
        var left = all.document;
        var right = extension.document;

        for (var code : right.keySet()) {
            if (!left.containsKey(code)) {
                left.put(code, List.of());
            }

            left.put(code, constructs(
                    left.get(code),
                    right.get(code)
            ));
        }
    }

    /**
     * Merge `list` into `existing` (both lists of constructs).
     * Mutates `existing`.
     */
    private static List<Construct> constructs(List<Construct> existing, List<Construct> list) {
        existing = new ArrayList<>(existing);

        var index = -1;
        var before = new ArrayList<Construct>();

        while (++index < list.size()) {
            (list.get(index).add == ConstructPrecedence.AFTER ? existing : before).add(list.get(index));
        }

        existing.addAll(0, before);
        return existing;
    }

}
