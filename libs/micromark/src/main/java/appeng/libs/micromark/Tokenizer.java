package appeng.libs.micromark;

import appeng.libs.micromark.symbol.Codes;
import appeng.libs.unist.UnistPoint;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

    private final ParseContext parser;
    final InitialConstruct initialize;

    int pointLine;
    private int pointColumn;
    private int pointOffset;
    private int pointIndex = 0;
    private int pointBufferIndex = -1;

    Map<Integer, Integer> columnStart = new HashMap<>();

    List<Construct> resolveAllConstructs = new ArrayList<>();

    final List<Object> chunks = new ArrayList<>();
    private Stack<Token> stack = new Stack<>();
    private boolean consumed = true;

    final TokenizeContext context;

    private final Effects effects;

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
        this.parser = parser;
        this.initialize = initialize;
        context = new RootTokenizeContext(this);

        effects = new Effects();

        state = initialize.tokenize(context, effects);

        if (initialize.resolveAll != null) {
            resolveAllConstructs.add(initialize);
        }
    }

    public static TokenizeContext create(ParseContext parser, InitialConstruct initialize, @Nullable UnistPoint from) {
        return new Tokenizer(parser, initialize, from).context;
    }

    public ParseContext getParser() {
        return parser;
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

            Assert.check(
                    !consumed,
                    "expected code to not have been consumed: this might be because `return x(code)` instead of `return x` was used"
            );
            if (code == Codes.eof) {
                Assert.check(
                        context.getEvents().size() == 0 || context.getLastEvent().isExit(),
                        "expected last token to be open"
                );
            } else {
                Assert.check(
                        context.getLastEvent().isEnter(),
                        "expected last token to be open"
                );
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
            context.setPrevious(code);

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

            context.getEvents().add(new Event(EventType.ENTER, token, context));

            stack.add(token);

            return token;
        }

        /**
         * End a started token.
         */
        public Token exit(String type) {
            Assert.check(type != null, "expected string type");
            Assert.check(!type.isEmpty(), "expected non-empty string");

            Assert.check(!stack.isEmpty(), "cannot close w/o open tokens");
            var token = stack.pop();
            token.end = now();

            Assert.check(type.equals(token.type), "expected exit token to match current token");

            Assert.check(
                    !(
                            token.start._index() == token.end._index() &&
                                    token.start._bufferIndex() == token.end._bufferIndex()
                    ),
                    "expected non-empty token (`" + type + "`)"
            );

            LOGGER.debug("exit: '{}'", token.type);
            context.getEvents().add(Event.exit(token, context));

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
        State hook(List<Construct> constructs, State returnState, State bogusState);

        default State hook(Construct construct, State returnState, State bogusState) {
            return hook(List.of(construct), returnState, bogusState);
        }

        default State hook(Map<Integer, List<Construct>> map, State returnState, State bogusState) {
            return code -> {
                List<Construct> def = code != Codes.eof ? map.getOrDefault(code, List.of()) : List.of();
                List<Construct> all = code != Codes.eof ? map.getOrDefault(Codes.eof, List.of()) : List.of();
                var list = new ArrayList<Construct>();
                list.addAll(def);
                list.addAll(all);

                return hook(list, returnState, bogusState).step(code);
            };
        }
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
    private Hook constructFactory(ReturnHandle onreturn, @Nullable Map<String, Object> fields) {
        return (constructs, returnState, bogusState) -> {
            return new HookStateMachineFactory(onreturn, constructs, returnState, bogusState, fields).createFirst();
        };
    }

    private class HookStateMachineFactory {
        private final List<Construct> constructs;
        @Nullable
        private final Map<String, Object> fields;
        private int constructIndex;
        private Construct currentConstruct;
        private Info info;
        private final State returnState;
        private final State bogusState;
        private final ReturnHandle onreturn;

        public HookStateMachineFactory(ReturnHandle onreturn, List<Construct> constructs, State returnState, State bogusState, @Nullable Map<String, Object> fields) {
            this.constructs = constructs;
            this.fields = fields;
            this.constructIndex = 0;
            this.returnState = returnState;
            this.bogusState = bogusState;
            this.onreturn = onreturn;
        }

        public State createFirst() {
            if (constructs.isEmpty()) {
                Assert.check(bogusState != null, "expected `bogusState` to be given");
                return bogusState;
            }

            return create(constructs.get(constructIndex));
        }

        public State create(Construct construct) {
            return code -> {
                // To do: not needed to store if there is no bogus state, probably?
                // Currently doesn’t work because `inspect` in document does a check
                // w/o a bogus, which doesn’t make sense. But it does seem to help perf
                // by not storing.
                info = store();
                currentConstruct = construct;

                if (!currentConstruct.partial) {
                    context.setCurrentConstruct(construct);
                }

                if (currentConstruct.name != null && context.getParser().constructs.nullDisable.contains(currentConstruct.name)) {
                    return nok(code);
                }

                // If we do have fields, create an object w/ `context` as its
                // prototype.
                // This allows a “live binding”, which is needed for `interrupt`.
                var useContext = context;
                if (fields != null && Boolean.TRUE.equals(fields.get("interrupt"))) {
                    useContext = new InterruptedTokenizeContext(context);
                }

                return currentConstruct.tokenize.tokenize(
                        useContext,
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
            Assert.check(code == expectedCode, "expected code");
            consumed = true;
            info.restore().run();

            if (++constructIndex < constructs.size()) {
                return create(constructs.get(constructIndex));
            }

            return bogusState;
        }

    }

    void addResult(Construct construct, int from) {
        if (construct.resolveAll != null && !resolveAllConstructs.contains(construct)) {
            resolveAllConstructs.add(construct);
        }

        if (construct.resolve != null) {
            ListUtils.splice(
                    context.getEvents(),
                    from,
                    context.getEvents().size() - from,
                    construct.resolve.resolve(ListUtils.slice(context.getEvents(), from), context)
            );
        }

        if (construct.resolveTo != null) {
            context.setEvents(construct.resolveTo.resolve(context.getEvents(), context));
        }

        if (
                !construct.partial &&
                        !context.getEvents().isEmpty() &&
                        context.getEvents().get(context.getEvents().size() - 1).type() != EventType.EXIT
        ) {
            throw new IllegalStateException("expected last token to end");
        }
    }

    /**
     * Store state.
     */
    Info store() {
        var startPoint = now();
        var startPrevious = context.getPrevious();
        var startCurrentConstruct = context.getCurrentConstruct();
        var startEventsIndex = context.getEvents().size();
        var startStack = new Stack<Token>();
        startStack.addAll(stack);

        return new Info(
                () -> {
                    pointLine = startPoint.line();
                    pointColumn = startPoint.column();
                    pointOffset = startPoint.offset();
                    pointIndex = startPoint._index();
                    pointBufferIndex = startPoint._bufferIndex();
                    context.setPrevious(startPrevious);
                    context.setCurrentConstruct(startCurrentConstruct);
                    ListUtils.setLength(context.getEvents(), startEventsIndex);
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

    public boolean isOnLazyLine() {
        return parser.isLazyLine(pointLine);
    }

}