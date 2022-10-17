package appeng.libs.micromark;

import appeng.libs.unist.UnistPoint;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A context object that helps w/ parsing markdown.
 */
public class ParseContext {

    public Extension constructs;
    Create content;
    Create document;
    Create flow;
    Create string;
    Create text;

    /**
     * List of defined identifiers
     */
    public java.util.List<String> defined = new ArrayList<>();

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
    public Map<Integer, Boolean> lazy = new HashMap<>();

    public Create get(ContentType contentType) {
        return switch (contentType) {
            case DOCUMENT -> document;
            case FLOW -> flow;
            case CONTENT -> content;
            case TEXT -> text;
            case STRING -> string;
        };
    }

    @FunctionalInterface
    public interface Create {
        default Tokenizer.TokenizeContext create() {
            return create(null);
        }

        Tokenizer.TokenizeContext create(@Nullable UnistPoint from);
    }
}
