package appeng.libs.micromark;

import appeng.libs.micromark.symbol.Codes;
import appeng.libs.unist.UnistPoint;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

/**
 * Create a tokenizer.
 * Tokenizers deal with one type of data (e.g., containers, flow, text).
 * The parser is the object dealing with it all.
 * `initialize` works like other constructs, except that only its `tokenize`
 * function is used, in which case it doesn’t receive an `ok` or `nok`.
 * `from` can be given to set the point before the first character, although
 * when further lines are indented, they must be set with `defineSkip`.
 */
public class Tokenizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Tokenizer.class);

    private int pointLine;
    private int pointColumn;
    private int pointOffset;
    private int pointIndex = 0;
    private int pointBufferIndex = -1;

    Map<Integer, Integer> columnStart = new HashMap<>();

    List<Construct> resolveAllConstructs = new ArrayList<>();

    private List<Object> chunks = new ArrayList<>();
    private Stack<Token> stack = new Stack<>();
    private boolean consumed = true;

    private TokenizeContext context;

    private Effects effects;

    /**
     * The state function.
     */
    State state;

    private Tokenizer(ParseContext parser, InitialConstruct initialize, @Nullable UnistPoint from) {
        if (from != null) {
            pointLine = from.line();
            pointColumn = from.column();
            pointOffset = from.offset();
        } else {
            pointLine = 1;
            pointColumn = 1;
            pointOffset = 0;
        }

        // TODO: Closure (?)
        context = new TokenizeContext(initialize);
        context.previous = Codes.eof; // to CTOR?
        context.code = Codes.eof;
        context.parser = parser;

        state = initialize.tokenize(context, effects);

        if (initialize.resolveAll != null) {
            resolveAllConstructs.add(initialize);
        }
    }

    public static TokenizeContext create(ParseContext parser, InitialConstruct initialize, @Nullable UnistPoint from) {
        return new Tokenizer(parser, initialize, from).context;
    }

    /**
     * Tools used for tokenizing. A context object to transition the state machine.
     */
    public class Effects {

        /**
         * Deal with the character and move to the next.
         */
        public void consume(int code) {
            if (code != expectedCode) {
                throw new IllegalArgumentException("expected given code to equal expected code");
            }

            LOGGER.debug("consume: `{}`", code);

            if (!consumed) {
                throw new IllegalStateException("expected code to not have been consumed: this might be because `return x(code)` instead of `return x` was used");
            }

            if (code == Codes.eof && !context.events.isEmpty() && context.events.get(context.events.size() - 1).type != EventType.EXIT
                    || code != Codes.eof && context.events.get(context.events.size() - 1).type != EventType.ENTER) {
                throw new IllegalStateException("expected last token to be open");
            }

            if (CharUtil.markdownLineEnding(code)) {
                pointLine++;
                pointColumn = 1;
                pointOffset += code == Codes.carriageReturnLineFeed ? 2 : 1;
                accountForPotentialSkip();
                LOGGER.debug("position: after eol: `{}`", now());
            } else if (code != Codes.virtualSpace) {
                pointColumn++;
                pointOffset++;
            }

            // Not in a string chunk.
            if (pointBufferIndex < 0) {
                pointIndex++;
            } else {
                pointBufferIndex++;

                // At end of string chunk.
                if (pointBufferIndex == ((String) chunks.get(pointIndex)).length()) {
                    pointBufferIndex = -1;
                    pointIndex++;
                }
            }

            // Expose the previous character.
            context.previous = code;

            // Mark as consumed.
            consumed = true;
        }

        public Token enter(String type) {
            return enter(type, null);
        }

        /**
         * Start a new token.
         */
        public Token enter(String type, Token fields) {
            Token token = fields != null ? new Token(fields) : new Token();
            token.type = type;
            token.start = now();

            LOGGER.debug("enter: '{}'", type);

            context.events.add(new Event(EventType.ENTER, token, context));

            stack.add(token);

            return token;
        }

        /**
         * End a started token.
         */
        public Token exit(String type) {
            if (type.isEmpty()) {
                throw new IllegalArgumentException("expected non-empty string");
            }

            if (stack.isEmpty()) {
                throw new IllegalStateException("cannot close w/o open tokens");
            }
            var token = stack.pop();
            token.end = now();

            if (!token.type.equals(type)) {
                throw new IllegalStateException("expected exit token to match current token");
            }

            if (
                    token.start._index() == token.end._index()
                            && token.start._bufferIndex() == token.end._bufferIndex()
            ) {
                throw new IllegalStateException("expected non-empty token ('" + type + "')");
            }

            LOGGER.debug("exit: '{}'", token.type);
            context.events.add(new Event(EventType.EXIT, token, context));

            return token;
        }

        /**
         * Try to tokenize a construct.
         */
        public Hook attempt = constructFactory(Tokenizer.this::onsuccessfulconstruct, null);

        /**
         * Attempt, then revert.
         */
        public Hook check = constructFactory(Tokenizer.this::onsuccessfulcheck, null);

        /**
         * Interrupt is used for stuff right after a line of content.
         */
        public Hook interrupt = constructFactory(Tokenizer.this::onsuccessfulcheck, Map.of("interrupt", true));
    }

    /**
     * Track which character we expect to be consumed, to catch bugs.
     */
    private int expectedCode;

    public Point now() {
        return new Point(
                pointLine,
                pointColumn,
                pointOffset,
                pointIndex,
                pointBufferIndex
        );
    }

    //
    // State management.
    //

    /**
     * Main loop (note that `_index` and `_bufferIndex` in `point` are modified by
     * `consume`).
     * Here is where we walk through the chunks, which either include strings of
     * several characters, or numerical character Codes.
     * The reason to do this in a loop instead of a call is so the stack can
     * drain.
     */
    void main() {
        int chunkIndex;

        while (pointIndex < chunks.size()) {
            var chunk = chunks.get(pointIndex);

            // If we’re in a buffer chunk, loop through it.
            if (chunk instanceof String textChunk) {
                chunkIndex = pointIndex;

                if (pointBufferIndex < 0) {
                    pointBufferIndex = 0;
                }

                while (
                        pointIndex == chunkIndex &&
                                pointBufferIndex < textChunk.length()
                ) {
                    go(textChunk.charAt(pointBufferIndex));
                }
            } else {
                go((int) chunk);
            }
        }
    }

    /**
     * Deal with one code.
     */
    void go(int code) {
        if (!consumed) {
            throw new IllegalStateException("expected character to be consumed");
        }
        consumed = false;
        LOGGER.debug("main: passing `{}` to {}", code, state);
        expectedCode = code;
        if (state == null) {
            throw new IllegalStateException("expected state");
        }
        state = state.step(code);
    }

    /**
     * Use results.
     */
    void onsuccessfulconstruct(Construct construct, Info info) {
        addResult(construct, info.from);
    }

    /**
     * Discard results.
     */
    void onsuccessfulcheck(Construct construct, Info info) {
        info.restore().run();
    }

    public interface Hook {
        default State hook(Construct construct, State returnState, State bogusState) {
            return hook(List.of(construct), returnState, bogusState);
        }

        State hook(List<Construct> construct, State returnState, State bogusState);

        State hook(Map<Integer, List<Construct>> map, State returnState, State bogusState);
    }

    /**
     * Handles a successful run.
     */
    @FunctionalInterface
    interface ReturnHandle {
        void handle(Construct construct, Info info);
    }

    record Info(Runnable restore, int from) {
    }

    /**
     * Factory to attempt/check/interrupt.
     */
    Hook constructFactory(ReturnHandle onreturn, @Nullable Map<String, Object> fields) {
        return new Hook() {
            List<Construct> listOfConstructs;
            int constructIndex;
            Construct currentConstruct;
            Info info;
            State returnState;
            State bogusState;

            @Override
            public State hook(List<Construct> constructs, State returnState, State bogusState) {
                listOfConstructs = constructs;
                constructIndex = 0;
                this.returnState = returnState;
                this.bogusState = bogusState;

                if (constructs.isEmpty()) {
                    if (bogusState == null) {
                        throw new IllegalArgumentException("expected `bogusState` to be given");
                    }
                    return bogusState;
                }

                return handleConstruct(constructs.get(constructIndex));
            }

            @Override
            public State hook(Map<Integer, List<Construct>> map, State returnState, State bogusState) {
                return code -> {
                    List<Construct> def = code != Codes.eof ? map.getOrDefault(code, List.of()) : List.of();
                    List<Construct> all = code != Codes.eof ? map.getOrDefault(Codes.eof, List.of()) : List.of();
                    var list = new ArrayList<Construct>();
                    list.addAll(def);
                    list.addAll(all);

                    return hook(list, returnState, bogusState).step(code);
                };
            }

            /**
             * Handle a single construct.
             */
            private State handleConstruct(Construct construct) {
                return code -> {
                    // To do: not needed to store if there is no bogus state, probably?
                    // Currently doesn’t work because `inspect` in document does a check
                    // w/o a bogus, which doesn’t make sense. But it does seem to help perf
                    // by not storing.
                    info = store();
                    currentConstruct = construct;

                    if (!construct.partial) {
                        context.currentConstruct = construct;
                    }

                    if (construct.name != null && context.parser.constructs.nullDisable.contains(construct.name)) {
                        return nok(code);
                    }

                    return construct.tokenize.tokenize(
                            // If we do have fields, create an object w/ `context` as its
                            // prototype.
                            // This allows a “live binding”, which is needed for `interrupt`.
                            // TODO fields != null ? Object.assign(Object.create(context), fields) : context,
                            context,
                            effects,
                            this::ok,
                            this::nok
                    ).step(code);
                };
            }

            private State ok(int code) {
                if (code != expectedCode) {
                    throw new IllegalStateException("expected code");
                }
                consumed = true;
                onreturn.handle(currentConstruct, info);
                return returnState;
            }

            private State nok(int code) {
                if (code != expectedCode) {
                    throw new IllegalStateException("expected code");
                }
                consumed = true;
                info.restore().run();

                if (++constructIndex < listOfConstructs.size()) {
                    return handleConstruct(listOfConstructs.get(constructIndex));
                }

                return bogusState;
            }
        };
    }

    void addResult(Construct construct, int from) {
        if (construct.resolveAll != null && !resolveAllConstructs.contains(construct)) {
            resolveAllConstructs.add(construct);
        }

        if (construct.resolve != null) {
            ChunkUtils.splice(
                    context.events,
                    from,
                    context.events.size() - from,
                    construct.resolve.resolve(new ArrayList<>(context.events.subList(from, context.events.size())), context)
            );
        }

        if (construct.resolveTo != null) {
            context.events = construct.resolveTo.resolve(context.events, context);
        }

        if (
                !construct.partial &&
                        !context.events.isEmpty() &&
                        context.events.get(context.events.size() - 1).type() != EventType.EXIT
        ) {
            throw new IllegalStateException("expected last token to end");
        }
    }

    /**
     * Store state.
     */
    Info store() {
        var startPoint = now();
        var startPrevious = context.previous;
        var startCurrentConstruct = context.currentConstruct;
        var startEventsIndex = context.events.size();
        var startStack = new Stack<Token>();
        startStack.addAll(stack);

        return new Info(
                () -> {
                    pointLine = startPoint.line();
                    pointColumn = startPoint.column();
                    pointOffset = startPoint.offset();
                    pointIndex = startPoint._index();
                    pointBufferIndex = startPoint._bufferIndex();
                    context.previous = startPrevious;
                    context.currentConstruct = startCurrentConstruct;
                    if (startEventsIndex > context.events.size()) {
                        context.events.subList(startEventsIndex, context.events.size()).clear();
                    }
                    stack = startStack;
                    accountForPotentialSkip();
                    LOGGER.debug("position: restore: '{}'", now());
                },
                startEventsIndex
        );
    }

    /**
     * Move the current point a bit forward in the line when it’s on a column
     * skip.
     */
    void accountForPotentialSkip() {
        if (columnStart.containsKey(pointLine) && pointColumn < 2) {
            pointColumn = columnStart.get(pointLine);
            pointOffset += columnStart.get(pointLine) - 1;
        }
    }

    /**
     * A context object that helps w/ tokenizing markdown constructs.
     */
    public class TokenizeContext {
        public boolean _gfmTableDynamicInterruptHack;

        /**
         * The previous code.
         */
        public int previous;
        /**
         * Current code.
         */
        public int code;
        /**
         * Whether we’re currently interrupting.<br>
         * Take for example:
         * <p>
         * <pre>
         *   ```markdown
         *   a
         *   # b
         *   ```
         *   </pre>
         * <p>
         * At 2:1, we’re “interrupting”.
         */
        public boolean interrupt;

        /**
         * The current construct.
         * <p>
         * Constructs that are not <code>partial</code> are set here.
         */
        @Nullable
        public Construct currentConstruct;

        /**
         * Info set when parsing containers.
         * <p>
         * Containers are parsed in separate phases: their first line (`tokenize`),
         * continued lines (`continuation.tokenize`), and finally `exit`.
         * This record can be used to store some information between these hooks.
         */
        public ContainerState containerState = new ContainerState();

        /**
         * Current list of events.
         */
        public List<Event> events = new ArrayList<>();

        public boolean _gfmTasklistFirstContentOfListItem;

        @Nullable
        public Event getLastEvent() {
            return !events.isEmpty() ? events.get(events.size() - 1) : null;
        }

        /**
         * The relevant parsing context.
         */
        public ParseContext parser;

        final InitialConstruct initialize;

        public TokenizeContext(InitialConstruct initialize) {
            this.initialize = initialize;
        }

        /**
         * Get the chunks that span a token.
         */
        public List<Object> sliceStream(Token token) {
            return sliceStream(token.start, token.end);
        }

        /**
         * Get the chunks that span a location.
         */
        public List<Object> sliceStream(Point start, Point end) {
            return sliceChunks(chunks, start, end);
        }

        /**
         * Get the chunks from a slice of chunks in the range of a token.
         */
        private List<Object> sliceChunks(List<Object> chunks, Point start, Point end) {
            var startIndex = start._index();
            var startBufferIndex = start._bufferIndex();
            var endIndex = end._index();
            var endBufferIndex = end._bufferIndex();
            List<Object> view = new ArrayList<>();

            if (startIndex == endIndex) {
                if (endBufferIndex < 0) {
                    throw new IllegalArgumentException("expected non-negative end buffer index");
                }
                if (startBufferIndex < 0) {
                    throw new IllegalArgumentException("expected non-negative start buffer index");
                }

                view.add(((String) chunks.get(startIndex)).substring(startBufferIndex, endBufferIndex));
            } else {
                view.addAll(chunks.subList(startIndex, endIndex));

                if (startBufferIndex > -1) {
                    view.set(0, ((String) view.get(0)).substring(startBufferIndex));
                }

                if (endBufferIndex > 0) {
                    view.add(((String) chunks.get(endIndex)).substring(0, endBufferIndex));
                }
            }

            return view;
        }

        public List<Event> write(List<Object> slice) {
            chunks = ChunkUtils.push(chunks, slice);

            main();

            // Exit if we’re not done, resolve might change stuff.
            if (!Objects.equals(chunks.get(chunks.size() - 1), Codes.eof)) {
                return Collections.emptyList();
            }

            addResult(initialize, 0);

            // Otherwise, resolve, and exit.
            context.events = Construct.resolveAll(resolveAllConstructs, context.events, context);

            return context.events;
        }

        public String sliceSerialize(Point start, Point end) {
            var t = new Token();
            t.start = start;
            t.end = end;
            return sliceSerialize(t, false);
        }

        public String sliceSerialize(Token token) {
            return sliceSerialize(token, false);
        }

        public String sliceSerialize(Token token, boolean expandTabs) {
            return serializeChunks(sliceStream(token), expandTabs);
        }

        public void defineSkip(Point value) {
            columnStart.put(value.line(), value.column());
            accountForPotentialSkip();
            LOGGER.debug("position: define skip: {}", now());
        }

        public Point now() {
            return Tokenizer.this.now();
        }
    }

    public static class ContainerState extends HashMap<String, Object> {
        boolean _closeFlow;
    }

    public record Event(EventType type, Token token, TokenizeContext context) {
        public static Event enter(Token token, TokenizeContext context) {
            return new Event(EventType.ENTER, token, context);
        }
        public static Event exit(Token token, TokenizeContext context) {
            return new Event(EventType.EXIT, token, context);
        }

        public boolean isEnter() {
            return type() == EventType.ENTER;
        }

        public boolean isExit() {
            return type() == EventType.EXIT;
        }
    }

    public enum EventType {
        ENTER,
        EXIT
    }

    /**
     * Get the string value of a slice of chunks.
     */
    String serializeChunks(List<Object> chunks, boolean expandTabs) {
        var index = -1;
        var result = new StringBuilder();
        boolean atTab = false;

        while (++index < chunks.size()) {
            var chunk = chunks.get(index);

            if (chunk instanceof String textChunk) {
                result.append(textChunk);
            } else if (chunk instanceof Integer code) {
                switch (code) {
                    case Codes.carriageReturn -> result.append('\r');
                    case Codes.lineFeed -> result.append('\n');
                    case Codes.carriageReturnLineFeed -> result.append('\r').append('\n');
                    case Codes.horizontalTab -> result.append(expandTabs ? ' ' : '\t');
                    case Codes.virtualSpace -> {
                        if (!expandTabs && atTab)
                            continue;
                        result.append(' ');
                    }
                    // Currently only replacement character.
                    default -> result.append((char) code.intValue());
                }
            } else {
                throw new IllegalStateException("Expected String or int: " + chunk);
            }

            atTab = Objects.equals(chunk, Codes.horizontalTab);

        }

        return result.toString();
    }

}