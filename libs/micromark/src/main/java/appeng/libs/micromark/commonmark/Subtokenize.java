package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.ChunkUtils;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.symbol.Codes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Subtokenize {
    private Subtokenize() {
    }

    /**
     * Tokenize subcontent.
     */
    public static boolean subtokenize(List<Tokenizer.Event> events) {
        Map<Integer, Integer> jumps = new HashMap<>();
        var index = -1;
        Tokenizer.Event event;
        int otherIndex;
        List<Tokenizer.Event> subevents;
        boolean more = false;

        while (++index < events.size()) {
            while (jumps.containsKey(index)) {
                index = jumps.get(index);
            }

            event = events.get(index);

            // Add a hook for the GFM tasklist extension, which needs to know if text
            // is in the first content of a list item.
            if (
                    index != 0 &&
                            event.token().type.equals(Types.chunkFlow) &&
                            events.get(index - 1).token().type.equals(Types.listItemPrefix)
            ) {
                Assert.check(event.token()._tokenizer != null, "expected '_tokenizer' on subtokens");
                subevents = event.token()._tokenizer.events;
                otherIndex = 0;

                if (
                        otherIndex < subevents.size() &&
                                subevents.get(otherIndex).token().type.equals(Types.lineEndingBlank)
                ) {
                    otherIndex += 2;
                }

                if (
                        otherIndex < subevents.size() &&
                                subevents.get(otherIndex).token().type.equals(Types.content)
                ) {
                    while (++otherIndex < subevents.size()) {
                        if (subevents.get(otherIndex).token().type.equals(Types.content)) {
                            break;
                        }

                        if (subevents.get(otherIndex).token().type.equals(Types.chunkText)) {
                            subevents.get(otherIndex).token()._isInFirstContentOfListItem = true;
                            otherIndex++;
                        }
                    }
                }
            }

            // Enter.
            if (event.isEnter()) {
                if (event.token().contentType != null) {
                    jumps.putAll(subcontent(events, index));
                    index = jumps.get(index);
                    more = true;
                }
            }
            // Exit.
            else if (event.token()._container) {
                otherIndex = index;
                Integer lineIndex = null;

                while (otherIndex-- != 0) {
                    var otherEvent = events.get(otherIndex);

                    if (
                            otherEvent.token().type.equals(Types.lineEnding) ||
                                    otherEvent.token().type.equals(Types.lineEndingBlank)
                    ) {
                        if (otherEvent.isEnter()) {
                            if (lineIndex != null) {
                                events.get(lineIndex).token().type = Types.lineEndingBlank;
                            }

                            otherEvent.token().type = Types.lineEnding;
                            lineIndex = otherIndex;
                        }
                    } else {
                        break;
                    }
                }

                if (lineIndex != null) {
                    // Fix position.
                    event.token().end = events.get(lineIndex).token().start;

                    // Switch container exit w/ line endings.
                    var parameters = new ArrayList<>(events.subList(lineIndex, index));
                    parameters.add(0, event);
                    ChunkUtils.splice(events, lineIndex, index - lineIndex + 1, parameters);
                }
            }
        }

        return !more;
    }

    record Jump(int first, int second) {
    }

    /**
     * Tokenize embedded tokens.
     */
    private static Map<Integer, Integer> subcontent(List<Tokenizer.Event> events, int eventIndex) {
        var token = events.get(eventIndex).token();
        var context = events.get(eventIndex).context();
        var startPosition = eventIndex - 1;
        List<Integer> startPositions = new ArrayList<>();
        Assert.check(token.contentType != null, "expected 'contentType' on subtokens");
        var tokenizer = Objects.requireNonNullElse(token._tokenizer, context.parser.get(token.contentType).create(token.start));
        var childEvents = tokenizer.events;
        List<Jump> jumps = new ArrayList<>();
        Map<Integer, Integer> gaps = new HashMap<>();
        List<Object> stream;
        Token previous = null;
        int index = -1;
        Token current = token;
        int adjust = 0;
        List<Integer> breaks = new ArrayList<>();
        breaks.add(0);

        // Loop forward through the linked tokens to pass them in order to the
        // subtokenizer.
        while (current != null) {
            // Find the position of the event for this token.
            while (events.get(++startPosition).token() != current) {
                // Empty.
            }

            Assert.check(
                    previous == null || current.previous == previous,
                    "expected previous to match"
            );
            Assert.check(previous == null || previous.next == current, "expected next to match");

            startPositions.add(startPosition);

            if (current._tokenizer == null) {
                stream = context.sliceStream(current);

                if (current.next == null) {
                    stream.add(Codes.eof);
                }

                if (previous != null) {
                    tokenizer.defineSkip(current.start);
                }

                if (current._isInFirstContentOfListItem) {
                    tokenizer._gfmTasklistFirstContentOfListItem = true;
                }

                tokenizer.write(stream);

                if (current._isInFirstContentOfListItem) {
                    tokenizer._gfmTasklistFirstContentOfListItem = false;
                }
            }

            // Unravel the next token.
            previous = current;
            current = current.next;
        }

        // Now, loop back through all events (and linked tokens), to figure out which
        // parts belong where.
        current = token;

        while (++index < childEvents.size()) {
            var childEvent = childEvents.get(index);
            if (
                // Find a void token that includes a break.
                    childEvent.isExit() &&
                            childEvents.get(index - 1).isEnter() &&
                            childEvent.token().type.equals(childEvents.get(index - 1).token().type) &&
                            childEvent.token().start.line() != childEvent.token().end.line()
            ) {
                Assert.check(current != null, "expected a current token");
                breaks.add(index + 1);
                // Help GC.
                current._tokenizer = null;
                current.previous = null;
                current = current.next;
            }
        }

        // Help GC.
        tokenizer.events.clear();

        // If there’s one more token (which is the cases for lines that end in an
        // EOF), that’s perfect: the last point we found starts it.
        // If there isn’t then make sure any remaining content is added to it.
        if (current != null) {
            // Help GC.
            current._tokenizer = null;
            current.previous = null;
            Assert.check(current.next == null, "expected no next token");
        } else {
            breaks.remove(breaks.size() - 1);
        }

        // Now splice the events from the subtokenizer into the current events,
        // moving back to front so that splice indices aren’t affected.
        index = breaks.size();

        while (index-- != 0) {
            var slice = new ArrayList<>(childEvents.subList(breaks.get(index), breaks.get(index + 1)));
            var start = startPositions.remove(startPositions.size() - 1);
            Assert.check(start != null, "expected a start position when splicing");
            jumps.add(0, new Jump(start, start + slice.size() - 1));
            ChunkUtils.splice(events, start, 2, slice);
        }

        index = -1;

        while (++index < jumps.size()) {
            var jump = jumps.get(index);
            gaps.put(adjust + jump.first(), adjust + jump.second());
            adjust += jump.second() - jump.first() - 1;
        }

        return gaps;
    }

}
