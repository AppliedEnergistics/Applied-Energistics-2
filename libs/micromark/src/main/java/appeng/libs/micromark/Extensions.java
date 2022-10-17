package appeng.libs.micromark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        all.document = mergeMap(all.document, extension.document);
        all.contentInitial = mergeMap(all.contentInitial, extension.contentInitial);
        all.flowInitial = mergeMap(all.flowInitial, extension.flowInitial);
        all.flow = mergeMap(all.flow, extension.flow);
        all.string = mergeMap(all.string, extension.string);
        all.text = mergeMap(all.text, extension.text);

        // TODO
        // nullDisable
        // nullInsideSpan
        // nullAttentionMarkers
    }

    private static Map<Integer, List<Construct>> mergeMap(Map<Integer, List<Construct>> left, Map<Integer, List<Construct>> right) {
        left = new HashMap<>(left);

        for (var code : right.keySet()) {
            if (!left.containsKey(code)) {
                left.put(code, List.of());
            }

            left.put(code, constructs(
                    left.get(code),
                    right.get(code)
            ));
        }

        return left;
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
