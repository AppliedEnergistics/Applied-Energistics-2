package appeng.libs.micromark;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A context object that helps w/ tokenizing markdown constructs.
 */
class InterruptedTokenizeContext implements TokenizeContext {
    private final TokenizeContext parent;

    public InterruptedTokenizeContext(TokenizeContext parent) {
        this.parent = parent;
    }

    @Override
    @Nullable
    public Tokenizer.Event getLastEvent() {
        return parent.getLastEvent();
    }

    @Override
    public List<Object> sliceStream(Token token) {
        return parent.sliceStream(token);
    }

    @Override
    public List<Object> sliceStream(Point start, Point end) {
        return parent.sliceStream(start, end);
    }

    @Override
    public List<Object> sliceChunks(List<Object> chunks, Point start, Point end) {
        return parent.sliceChunks(chunks, start, end);
    }

    @Override
    public List<Tokenizer.Event> write(List<Object> slice) {
        return parent.write(slice);
    }

    @Override
    public String sliceSerialize(Point start, Point end) {
        return parent.sliceSerialize(start, end);
    }

    @Override
    public String sliceSerialize(Token token) {
        return parent.sliceSerialize(token);
    }

    @Override
    public String sliceSerialize(Token token, boolean expandTabs) {
        return parent.sliceSerialize(token, expandTabs);
    }

    @Override
    public void defineSkip(@NotNull Point value) {
        parent.defineSkip(value);
    }

    @Override
    public Point now() {
        return parent.now();
    }

    @Override
    public boolean isOnLazyLine() {
        return parent.isOnLazyLine();
    }

    @Override
    public boolean isInterrupt() {
        return true;
    }

    @Override
    public void setInterrupt(boolean interrupt) {
        throw new IllegalStateException("An interrupted context shouldn't be modified.");
    }

    @Override
    public int getPrevious() {
        return parent.getPrevious();
    }

    @Override
    public void setPrevious(int previous) {
        throw new IllegalStateException("An interrupted context shouldn't be modified.");
    }

    @Override
    public Construct getCurrentConstruct() {
        return parent.getCurrentConstruct();
    }

    @Override
    public void setCurrentConstruct(Construct currentConstruct) {
        throw new IllegalStateException("An interrupted context shouldn't be modified.");
    }

    @Override
    public Tokenizer.ContainerState getContainerState() {
        return parent.getContainerState();
    }

    @Override
    public void setContainerState(Tokenizer.ContainerState containerState) {
        throw new IllegalStateException("An interrupted context shouldn't be modified.");
    }

    @Override
    public List<Tokenizer.Event> getEvents() {
        return parent.getEvents();
    }

    @Override
    public void setEvents(List<Tokenizer.Event> events) {
        throw new IllegalStateException("An interrupted context shouldn't be modified.");
    }

    @Override
    public Tokenizer getTokenizer() {
        return parent.getTokenizer();
    }

    @Override
    public boolean isGfmTableDynamicInterruptHack() {
        return parent.isGfmTableDynamicInterruptHack();
    }

    @Override
    public boolean isGfmTasklistFirstContentOfListItem() {
        return parent.isGfmTasklistFirstContentOfListItem();
    }

    @Override
    public void setGfmTasklistFirstContentOfListItem(boolean value) {
        throw new IllegalStateException("An interrupted context shouldn't be modified.");
    }

    @Override
    public ParseContext getParser() {
        return parent.getParser();
    }
}
