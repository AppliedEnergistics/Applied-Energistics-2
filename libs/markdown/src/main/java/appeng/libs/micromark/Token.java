package appeng.libs.micromark;

import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * A token: a span of chunks.
 * Tokens are what the core of micromark produces: the built-in HTML compiler
 * or other tools can turn them into different things.
 * <p>
 * Tokens are essentially names attached to a slice of chunks, such as
 * `lineEndingBlank` for certain line endings, or `codeFenced` for a whole
 * fenced code.
 * <p>
 * Sometimes, more info is attached to tokens, such as `_open` and `_close`
 * by `attention` (strong, emphasis) to signal whether the sequence can open
 * or close an attention run.
 * <p>
 * Linked tokens are used because outer constructs are parsed first.
 * Take for example:
 * <p>
 * ```markdown
 * > *a
 * b*.
 * ```
 * <p>
 * 1.  The block quote marker and the space after it is parsed first
 * 2.  The rest of the line is a `chunkFlow` token
 * 3.  The two spaces on the second line are a `linePrefix`
 * 4.  The rest of the line is another `chunkFlow` token
 * <p>
 * The two `chunkFlow` tokens are linked together.
 * The chunks they span are then passed through the flow tokenizer.
 */
public class Token {
    private static final boolean DEBUG_TOKEN_CREATION = false;

    @Nullable
    private Map<TokenProperty<?>, Object> tokenData;

    public String type;
    public Point start;
    public Point end;

    /**
     * The previous token in a list of linked tokens.
     */
    @Nullable
    public Token previous;

    /**
     * The next token in a list of linked tokens
     */
    @Nullable
    public Token next;

    /**
     * Declares a token as having content of a certain type.
     */
    @Nullable
    public ContentType contentType;

    /**
     * Used when dealing with linked tokens.
     * A child tokenizer is needed to tokenize them, which is stored on those
     * tokens.
     */
    @Nullable
    public TokenizeContext _tokenizer;

    /**
     * A marker used to parse attention, depending on the characters before
     * sequences (`**`), the sequence can open, close, both, or none
     */
    public boolean _open;

    /**
     * A marker used to parse attention, depending on the characters after
     * sequences (`**`), the sequence can open, close, both, or none
     */
    public boolean _close;

    /**
     * A boolean used internally to figure out if a token is in the first content
     * of a list item construct.
     */
    public boolean _isInFirstContentOfListItem;

    /**
     * A boolean used internally to figure out if a token is a container token.
     */
    public boolean _container;

    /**
     * A boolean used internally to figure out if a list is loose or not.
     */
    public boolean _loose;

    /**
     * A boolean used internally to figure out if a link opening can’t be used
     * (because links in links are incorrect).
     */
    public boolean _inactive;

    /**
     * A boolean used internally to figure out if a link opening is balanced: it’s
     * not a link opening but has a balanced closing.
     */
    public boolean _balanced;

    public StackTraceElement[] stackTrace;

    public Token() {
        if (DEBUG_TOKEN_CREATION) {
            this.stackTrace = Thread.currentThread().getStackTrace();
        }
    }

    public Token(Token other) {
        other.copyTo(this);
        if (DEBUG_TOKEN_CREATION) {
            this.stackTrace = Thread.currentThread().getStackTrace();
        }
    }

    void copyTo(Token other) {
        other.type = type;
        other.start = start;
        other.end = end;
        other.previous = previous;
        other.next = next;
        other.contentType = contentType;
        other._tokenizer = _tokenizer;
        other._open = _open;
        other._close = _close;
        other._isInFirstContentOfListItem = _isInFirstContentOfListItem;
        other._container = _container;
        other._loose = _loose;
        other._inactive = _inactive;
        other._balanced = _balanced;
        other.tokenData = tokenData;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T get(TokenProperty<T> property) {
        if (tokenData == null) {
            return null;
        }
        return (T) tokenData.get(property);
    }

    public <T> void set(TokenProperty<T> property, T value) {
        if (tokenData == null) {
            tokenData = new IdentityHashMap<>();
        }
        tokenData.put(property, value);
    }

    public <T> void remove(TokenProperty<T> property) {
        if (tokenData != null) {
            tokenData.remove(property);
            if (tokenData.isEmpty()) {
                tokenData = null;
            }
        }
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append("Token{").append(type);

        if (start != null) {
            builder.append(",start=").append(start.line())
                    .append(":").append(start.column());
        }
        if (end != null) {
            builder.append(",end=").append(end.line())
                    .append(":").append(end.column());
        }
        builder.append("}");
        return builder.toString();
    }
}
