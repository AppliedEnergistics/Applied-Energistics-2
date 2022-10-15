package appeng.libs.micromark;

import java.util.HashSet;
import java.util.List;

/**
 * An object describing how to parse a markdown construct.
 */
public class Construct {

    /**
     * Guard whether the previous character can come before the construct
     */
    public Previous previous;

    /**
     * For containers, a continuation construct.
     */
    public Construct continuation;

    /**
     * For containers, a final hook.
     */
    public Exiter exit;

    /**
     * Name of the construct, used to toggle constructs off.
     * Named constructs must not be `partial`.
     */
    public String name;

    /**
     * Whether this construct represents a partial construct.
     * Partial constructs must not have a `name`.
     */
    public boolean partial;

    /**
     * Resolve the events parsed by `tokenize`.
     * <p>
     * For example, if we’re currently parsing a link title and this construct
     * parses character references, then `resolve` is called with the events
     * ranging from the start to the end of a character reference each time one is
     * found.
     */
    public Resolver resolve;

    /**
     * Resolve the events from the start of the content (which includes other
     * constructs) to the last one parsed by `tokenize`.
     * <p>
     * For example, if we’re currently parsing a link title and this construct
     * parses character references, then `resolveTo` is called with the events
     * ranging from the start of the link title to the end of a character
     * reference each time one is found.
     */
    public Resolver resolveTo;

    /**
     * Resolve all events when the content is complete, from the start to the end.
     * Only used if `tokenize` is successful once in the content.
     * <p>
     * For example, if we’re currently parsing a link title and this construct
     * parses character references, then `resolveAll` is called *if* at least one
     * character reference is found, ranging from the start to the end of the link
     * title to the end.
     */
    public Resolver resolveAll;

    /**
     * Concrete constructs cannot be interrupted by more containers.
     * <p>
     * For example, when parsing the document (containers, such as block quotes
     * and lists) and this construct is parsing fenced code:
     * <p>
     * <pre>
     *  > ```js
     *  > - list?
     *  </pre>
     * …then `- list?` cannot form if this fenced code construct is concrete.
     * <p>
     * An example of a construct that is not concrete is a GFM table:
     * <p>
     * <pre>
     *  | a |
     *  | - |
     *  > | b |
     *  </pre>
     * <p>
     * …`b` is not part of the table.
     */
    public boolean concrete;

    /**
     * Whether the construct, when in a `ConstructRecord`, precedes over existing
     * constructs for the same character code when merged
     * The default is that new constructs precede over existing ones.
     */
    public ConstructPrecedence add = ConstructPrecedence.BEFORE;

    /**
     * A resolver handles and cleans events coming from `tokenize`.
     */
    @FunctionalInterface
    public interface Resolver {
        /**
         * @param events  List of events.
         * @param context Context.
         */
        List<Tokenizer.Event> resolve(List<Tokenizer.Event> events, Tokenizer.TokenizeContext context);
    }

    /**
     * Like a tokenizer, but without `ok` or `nok`, and returning void.
     * This is the final hook when a container must be closed.
     */
    @FunctionalInterface
    public interface Exiter {
        void exit(Tokenizer.TokenizeContext context, Tokenizer.Effects effects);
    }

    /**
     * Guard whether `code` can come before the construct.
     * In certain cases a construct can hook into many potential start characters.
     * Instead of setting up an attempt to parse that construct for most
     * characters, this is a speedy way to reduce that.
     */
    @FunctionalInterface
    public interface Previous {
        boolean previous(Tokenizer.TokenizeContext context, int code);
    }

    /**
     * Call all `resolveAll`s.
     */
    public static List<Tokenizer.Event> resolveAll(List<Construct> constructs,
                                                   List<Tokenizer.Event> events,
                                                   Tokenizer.TokenizeContext context) {
        var called = new HashSet<Resolver>();
        var index = -1;

        while (++index < constructs.size()) {
            var resolve = constructs.get(index).resolveAll;

            if (resolve != null && !called.contains(resolve)) {
                events = resolve.resolve(events, context);
                called.add(resolve);
            }
        }

        return events;
    }

    @FunctionalInterface
    public interface TokenizerFunction {
        State tokenize(Tokenizer.TokenizeContext context, Tokenizer.Effects effects, State ok, State nok);
    }

    public TokenizerFunction tokenize;

}
