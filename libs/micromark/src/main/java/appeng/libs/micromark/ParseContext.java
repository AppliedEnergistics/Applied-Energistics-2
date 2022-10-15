package appeng.libs.micromark;

import appeng.libs.unist.UnistPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A context object that helps w/ parsing markdown.
 */
public class ParseContext {

    Extension constructs;
    Create content;
    Create document;
    Create flow;
    Create string;
    Create text;

    /**
     * List of defined identifiers
     */
    java.util.List<String> defined = new ArrayList<>();

    /**
     * Map of line numbers to whether they are lazy (as opposed to the line before
     * them).
     * <p>
     * Take for example:
     * <p>
     * <pre>
     *   > a
     *   b
     *   </pre>
     * <p>
     * L1 here is not lazy, L2 is.
     */
    Map<Integer, Boolean> lazy = new HashMap<>();

    @FunctionalInterface
    public interface Create {
        Tokenizer.TokenizeContext create(UnistPoint from);
    }
}
