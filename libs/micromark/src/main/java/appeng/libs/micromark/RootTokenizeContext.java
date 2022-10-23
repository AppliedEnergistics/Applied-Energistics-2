package appeng.libs.micromark;

import appeng.libs.micromark.symbol.Codes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A context object that helps w/ tokenizing markdown constructs.
 */
class RootTokenizeContext implements TokenizeContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(RootTokenizeContext.class);

    private final Tokenizer tokenizer;

    public boolean _gfmTableDynamicInterruptHack;
    public boolean _gfmTasklistFirstContentOfListItem;

    private int previous = Codes.eof;

    private boolean interrupt;

    @Nullable
    private Construct currentConstruct;

    private Tokenizer.ContainerState containerState = new Tokenizer.ContainerState();

    private List<Tokenizer.Event> events = new ArrayList<>();

    @Override
    @Nullable
    public Tokenizer.Event getLastEvent() {
        return !getEvents().isEmpty() ? getEvents().get(getEvents().size() - 1) : null;
    }

    public RootTokenizeContext(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    /**
     * Get the chunks that span a token.
     */
    @Override
    public List<Object> sliceStream(Token token) {
        return sliceStream(token.start, token.end);
    }

    /**
     * Get the chunks that span a location.
     */
    @Override
    public List<Object> sliceStream(Point start, Point end) {
        return sliceChunks(tokenizer.chunks, start, end);
    }

    @Override
    public List<Tokenizer.Event> write(List<Object> slice) {
        tokenizer.chunks.addAll(slice);

        tokenizer.main();

        // Exit if we’re not done, resolve might change stuff.
        if (!Objects.equals(tokenizer.chunks.get(tokenizer.chunks.size() - 1), Codes.eof)) {
            return Collections.emptyList();
        }

        tokenizer.addResult(tokenizer.initialize, 0);

        // Otherwise, resolve, and exit.
        tokenizer.context.setEvents(Construct.resolveAll(tokenizer.resolveAllConstructs, tokenizer.context.getEvents(), tokenizer.context));

        return tokenizer.context.getEvents();
    }

    @Override
    public String sliceSerialize(Point start, Point end) {
        var t = new Token();
        t.start = start;
        t.end = end;
        return sliceSerialize(t, false);
    }

    @Override
    public String sliceSerialize(Token token) {
        return sliceSerialize(token, false);
    }

    @Override
    public String sliceSerialize(Token token, boolean expandTabs) {
        return tokenizer.serializeChunks(sliceStream(token), expandTabs);
    }

    @Override
    public void defineSkip(@NotNull Point value) {
        tokenizer.columnStart.put(value.line(), value.column());
        tokenizer.accountForPotentialSkip();
        LOGGER.debug("position: define skip: {}", now());
    }

    @Override
    public Point now() {
        return tokenizer.now();
    }

    @Override
    public boolean isOnLazyLine() {
        return tokenizer.isOnLazyLine();
    }

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
    @Override
    public boolean isInterrupt() {
        return interrupt;
    }

    @Override
    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }

    /**
     * The previous code.
     */
    @Override
    public int getPrevious() {
        return previous;
    }

    @Override
    public void setPrevious(int previous) {
        this.previous = previous;
    }

    /**
     * The current construct.
     * <p>
     * Constructs that are not <code>partial</code> are set here.
     */
    @Override
    public Construct getCurrentConstruct() {
        return currentConstruct;
    }

    @Override
    public void setCurrentConstruct(Construct currentConstruct) {
        this.currentConstruct = currentConstruct;
    }

    /**
     * Info set when parsing containers.
     * <p>
     * Containers are parsed in separate phases: their first line (`tokenize`),
     * continued lines (`continuation.tokenize`), and finally `exit`.
     * This record can be used to store some information between these hooks.
     */
    @Override
    public Tokenizer.ContainerState getContainerState() {
        return containerState;
    }

    @Override
    public void setContainerState(Tokenizer.ContainerState containerState) {
        this.containerState = containerState;
    }

    /**
     * Current list of events.
     */
    @Override
    public List<Tokenizer.Event> getEvents() {
        return events;
    }

    @Override
    public void setEvents(List<Tokenizer.Event> events) {
        this.events = events;
    }

    @Override
    public Tokenizer getTokenizer() {
        return tokenizer;
    }

    @Override
    public boolean isGfmTableDynamicInterruptHack() {
        return _gfmTableDynamicInterruptHack;
    }

    @Override
    public boolean isGfmTasklistFirstContentOfListItem() {
        return _gfmTasklistFirstContentOfListItem;
    }

    @Override
    public void setGfmTasklistFirstContentOfListItem(boolean value) {
        _gfmTasklistFirstContentOfListItem = value;
    }
}
