package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.Construct;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.Tokenizer;

import java.util.List;

public final class Attention {
    private Attention() {
    }

    public static final Construct attention;
    static {
        attention = new Construct();
        attention.name = "attention";
        attention.tokenize = tokenizeAttention;
        attention.resolveAll = Attention::resolveAllAttention;
    }

    /**
     * Take all events and resolve attention to emphasis or strong.
     */
    private static List<Tokenizer.Event> resolveAllAttention(List<Tokenizer.Event> events, Tokenizer.TokenizeContext context) {
        int index = -1;
        int open;
        Token group;
        Token text;
        Token openingSequence;
        Token closingSequence;
        int use;
        List<Tokenizer.Event> nextEvents;
        int offset;

        // Walk through all events.
        //
        // Note: performance of this is fine on an mb of normal markdown, but it’s
        // a bottleneck for malicious stuff.
        while (++index < events.size()) {
            var event = events.get(index);
            
            // Find a token that can close.
            if (
                    event.type() == Tokenizer.EventType.ENTER &&
                            event.token().type.equals("attentionSequence") &&
                            event.token()._close
            ) {
                open = index;

                // Now walk back to find an opener.
                while (open-- > 0) {
                    var openEvent = events.get(open);

                    // Find a token that can open the closer.
                    if (
                            openEvent.type() == Tokenizer.EventType.EXIT &&
                                    openEvent.token().type.equals("attentionSequence") &&
                                    openEvent.token()._open &&
                                    // If the markers are the same:
                                    context.sliceSerialize(openEvent.token()).charCodeAt(0) ==
                                    context.sliceSerialize(event.token()).charCodeAt(0)
                    ) {
                        // If the opening can close or the closing can open,
                        // and the close size *is not* a multiple of three,
                        // but the sum of the opening and closing size *is* multiple of three,
                        // then don’t match.
                        if (
                                (openEvent.token()._close || event.token()._open) &&
                                        (event.token().end.offset - event.token().start.offset) % 3 &&
                                        !(
                                                (openEvent.token().end.offset -
                                                        openEvent.token().start.offset +
                                                        event.token().end.offset -
                                                        event.token().start.offset) %
                                                        3
                                        )
                        ) {
                            continue;
                        }

                        // Number of markers to use from the sequence.
                        use =
                                openEvent.token().end.offset - openEvent.token().start.offset > 1 &&
                                        event.token().end.offset - event.token().start.offset > 1
                                        ? 2
                                        : 1

          const start = Object.assign({}, openEvent.token().end)
          const end = Object.assign({}, event.token().start)
                        movePoint(start, -use)
                        movePoint(end, use)

                        openingSequence = {
                                type: use > 1 ? types.strongSequence : types.emphasisSequence,
                                start,
                                end: Object.assign({}, openEvent.token().end)
          }
                        closingSequence = {
                                type: use > 1 ? types.strongSequence : types.emphasisSequence,
                                start: Object.assign({}, event.token().start),
                                end
          }
                        text = {
                                type: use > 1 ? types.strongText : types.emphasisText,
                                start: Object.assign({}, openEvent.token().end),
                                end: Object.assign({}, event.token().start)
          }
                        group = {
                                type: use > 1 ? types.strong : types.emphasis,
                                start: Object.assign({}, openingSequence.start),
                                end: Object.assign({}, closingSequence.end)
          }

                        openEvent.token().end = Object.assign({}, openingSequence.start)
                        event.token().start = Object.assign({}, closingSequence.end)

                        nextEvents = []

                        // If there are more markers in the opening, add them before.
                        if (openEvent.token().end.offset - openEvent.token().start.offset) {
                            nextEvents = push(nextEvents, [
              ["enter", openEvent.token(), context],
              ["exit", openEvent.token(), context]
            ])
                        }

                        // Opening.
                        nextEvents = push(nextEvents, [
            ["enter", group, context],
            ["enter", openingSequence, context],
            ["exit", openingSequence, context],
            ["enter", text, context]
          ])

                        // Between.
                        nextEvents = push(
                                nextEvents,
                                resolveAll(
                                        context.parser.constructs.insideSpan.null,
                                events.slice(open + 1, index),
                                context
            )
          )

                        // Closing.
                        nextEvents = push(nextEvents, [
            ["exit", text, context],
            ["enter", closingSequence, context],
            ["exit", closingSequence, context],
            ["exit", group, context]
          ])

                        // If there are more markers in the closing, add them after.
                        if (event.token().end.offset - event.token().start.offset) {
                            offset = 2
                            nextEvents = push(nextEvents, [
              ["enter", event.token(), context],
              ["exit", event.token(), context]
            ])
                        } else {
                            offset = 0
                        }

                        splice(events, open - 1, index - open + 3, nextEvents)

                        index = open + nextEvents.length - offset - 2
                        break
                    }
                }
            }
        }

        // Remove remaining sequences.
        index = -1

        while (++index < events.size()) {
            var event = events.get(index);
            if (event.token().type.equals("attentionSequence")) {
                event.token().type = "data"
            }
        }

        return events
    }

}
