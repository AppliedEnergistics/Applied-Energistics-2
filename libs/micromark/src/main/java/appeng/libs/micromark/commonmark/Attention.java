package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.ChunkUtils;
import appeng.libs.micromark.ClassifyCharacter;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.Point;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;

import java.util.ArrayList;
import java.util.List;

public final class Attention {
    private Attention() {
    }

    public static final Construct attention;
    static {
        attention = new Construct();
        attention.name = "attention";
        attention.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects, ok)::start;
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
                                    context.sliceSerialize(openEvent.token()).charAt(0) ==
                                    context.sliceSerialize(event.token()).charAt(0)
                    ) {
                        // If the opening can close or the closing can open,
                        // and the close size *is not* a multiple of three,
                        // but the sum of the opening and closing size *is* multiple of three,
                        // then don’t match.
                        if (
                                (openEvent.token()._close || event.token()._open) &&
                                        (event.token().end.offset() - event.token().start.offset()) % 3 != 0 &&
                                        (
                                                (openEvent.token().end.offset() -
                                                        openEvent.token().start.offset() +
                                                        event.token().end.offset() -
                                                        event.token().start.offset()) %
                                                        3
                                        ) == 0
                        ) {
                            continue;
                        }

                        // Number of markers to use from the sequence.
                        use =
                                openEvent.token().end.offset() - openEvent.token().start.offset() > 1 &&
                                        event.token().end.offset() - event.token().start.offset() > 1
                                        ? 2
                                        : 1;

          var start = openEvent.token().end;
          var end = event.token().start;
        start = movePoint(start, -use);
        end = movePoint(end, use);

                        openingSequence = new Token();
                        openingSequence.type = use > 1 ? Types.strongSequence : Types.emphasisSequence;
                        openingSequence.start = start;
                        openingSequence.end = openEvent.token().end;
                        
                        closingSequence = new Token();
                        closingSequence.type = use > 1 ? Types.strongSequence : Types.emphasisSequence;
                        closingSequence.start = event.token().start;
                        closingSequence.end = end;

                        text = new Token();
                        text.type =  use > 1 ? Types.strongText : Types.emphasisText;
                        text.start = openEvent.token().end;
                        text.end = event.token().start;

                        group = new Token();
                        group.type = use > 1 ? Types.strong : Types.emphasis;
                        group.start = openingSequence.start;
                        group.end = closingSequence.end;

                        openEvent.token().end = openingSequence.start;
                        event.token().start = closingSequence.end;

                        nextEvents = new ArrayList<>();

                        // If there are more markers in the opening, add them before.
                        if (openEvent.token().end.offset() - openEvent.token().start.offset() != 0) {
                            nextEvents = ChunkUtils.push(nextEvents, List.of(
                                    Tokenizer.Event.enter(openEvent.token(), context),
                                    Tokenizer.Event.exit(openEvent.token(), context)
                            ));
                        }

                        // Opening.
                        nextEvents = ChunkUtils.push(nextEvents, List.of(
                                Tokenizer.Event.enter(group, context),
                                Tokenizer.Event.enter(openingSequence, context),
                                Tokenizer.Event.exit(openingSequence, context),
                                Tokenizer.Event.enter(text, context)
                        ));

                        // Between.
                        nextEvents = ChunkUtils.push(
                                nextEvents,
                                Construct.resolveAll(
                                        context.parser.constructs.nullInsideSpan,
                                        events.subList(open + 1, index),
                                        context
                                )
                        );

                        // Closing.
                        nextEvents = ChunkUtils.push(nextEvents, List.of(
                            Tokenizer.Event.exit(text, context),
                            Tokenizer.Event.enter(closingSequence, context),
                            Tokenizer.Event.exit(closingSequence, context),
                            Tokenizer.Event.exit(group, context)
                        ));

                        // If there are more markers in the closing, add them after.
                        if (event.token().end.offset() - event.token().start.offset() != 0) {
                            offset = 2;
                            nextEvents = ChunkUtils.push(nextEvents, List.of(
                              Tokenizer.Event.enter(event.token(), context),
                              Tokenizer.Event.exit(event.token(), context)
                            ));
                        } else {
                            offset = 0;
                        }

                        ChunkUtils.splice(events, open - 1, index - open + 3, nextEvents);

                        index = open + nextEvents.size() - offset - 2;
                        break;
                    }
                }
            }
        }

        // Remove remaining sequences.
        index = -1;

        while (++index < events.size()) {
            var event = events.get(index);
            if (event.token().type.equals("attentionSequence")) {
                event.token().type = "data";
            }
        }

        return events;
    }

    /**
     * Move a point a bit.
     * <p>
     * Note: `move` only works inside lines! It’s not possible to move past other
     * chunks (replacement characters, tabs, or line endings).
     */
    private static Point movePoint(Point point, int offset) {
        return new Point(
                point.line(),
                point.column() + offset,
                point.offset() + offset,
                point._index(),
                point._bufferIndex() + offset
        );
    }

    static class StateMachine {
        private final Tokenizer.TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final int previous;
        private final int before;
        private final List<Integer> attentionMarkers;

        int marker;

        public StateMachine(Tokenizer.TokenizeContext context, Tokenizer.Effects effects, State ok) {
            this.context = context;
            this.effects = effects;
            this.ok = ok;

            attentionMarkers = context.parser.constructs.nullAttentionMarkers;
            previous = context.previous;
            before = ClassifyCharacter.classifyCharacter(previous);

        }


        /**
         * Before a sequence.
         * <pre>
         * > | **
         *     ^
         * </pre>
         */
        State start(int code) {
            if (code != Codes.asterisk && code != Codes.underscore) {
                throw new IllegalStateException("expected asterisk or underscore");
            }
            effects.enter("attentionSequence");
            marker = code;
            return sequence(code);
        }

        /**
         * In a sequence.
         * <pre>
         * > | **
         *     ^^
         * </pre>
         */
        State sequence(int code) {
            if (code == marker) {
                effects.consume(code);
                return this::sequence;
            }

    var token = effects.exit("attentionSequence");
    var after = ClassifyCharacter.classifyCharacter(code);

    var open =
                    after == 0 ||
                            (after == Constants.characterGroupPunctuation && before != 0) ||
                            attentionMarkers.contains(code);
    var close =
                    before == 0 ||
                            (before == Constants.characterGroupPunctuation && after != 0) ||
                            attentionMarkers.contains(previous);

            token._open = marker == Codes.asterisk ? open : open && (before != 0 || !close);
            token._close = marker == Codes.asterisk ? close : close && (after != 0 || !open);
            return ok.step(code);
        }
    }

}
