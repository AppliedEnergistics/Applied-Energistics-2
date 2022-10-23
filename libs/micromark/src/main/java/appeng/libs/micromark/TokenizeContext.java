package appeng.libs.micromark;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface TokenizeContext {
    @Nullable Tokenizer.Event getLastEvent();

    List<Object> sliceStream(Token token);

    List<Object> sliceStream(Point start, Point end);

    /**
     * Get the chunks from a slice of chunks in the range of a token.
     */
    default List<Object> sliceChunks(List<Object> chunks, Point start, Point end) {
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

    List<Tokenizer.Event> write(List<Object> slice);

    String sliceSerialize(Point start, Point end);

    String sliceSerialize(Token token);

    String sliceSerialize(Token token, boolean expandTabs);

    void defineSkip(@NotNull Point value);

    Point now();

    boolean isOnLazyLine();

    boolean isInterrupt();

    void setInterrupt(boolean interrupt);

    int getPrevious();

    void setPrevious(int previous);

    Construct getCurrentConstruct();

    void setCurrentConstruct(Construct currentConstruct);

    Tokenizer.ContainerState getContainerState();

    void setContainerState(Tokenizer.ContainerState containerState);

    List<Tokenizer.Event> getEvents();

    void setEvents(List<Tokenizer.Event> events);

    Tokenizer getTokenizer();

    boolean isGfmTableDynamicInterruptHack();

    boolean isGfmTasklistFirstContentOfListItem();

    void setGfmTasklistFirstContentOfListItem(boolean value);

    default ParseContext getParser() {
        return getTokenizer().getParser();
    }
}
