package appeng.libs.micromark;

import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;

import java.util.List;
import java.util.Map;
import java.util.Objects;

final class InitializeText {
    private InitializeText() {
    }

    public static final Construct.Resolver resolver = createResolver(null);
    public static final InitialConstruct string = initializeFactory("string");
    public static final InitialConstruct text = initializeFactory("text");

    static class TextTokenizer {
        private final Tokenizer.TokenizeContext context;
        private final Map<Integer, List<Construct>> constructs;
        private final Tokenizer.Effects effects;
        private final State text;

        public TextTokenizer(Tokenizer.TokenizeContext context,
                             Map<Integer, List<Construct>> constructs,
                             Tokenizer.Effects effects) {
            this.context = context;
            this.constructs = constructs;
            this.effects = effects;
            this.text = effects.attempt.hook(constructs, this::start, this::notText);
        }

        private State start(int code) {
            return atBreak(code) ? text.step(code) : notText(code);
        }

        private State notText(int code) {
            if (code == Codes.eof) {
                effects.consume(code);
                return null;
            }

            effects.enter(Types.data);
            effects.consume(code);
            return this::data;
        }

        ;

        private State data(int code) {
            if (atBreak(code)) {
                effects.exit(Types.data);
                return text.step(code);
            }

            // Data.
            effects.consume(code);
            return this::data;
        }

        ;

        boolean atBreak(int code) {
            if (code == Codes.eof) {
                return true;
            }

            var list = constructs.get(code);
            var index = -1;

            if (list != null) {
                while (++index < list.size()) {
                    var item = list.get(index);
                    if (item.previous == null || item.previous.previous(context, context.previous)) {
                        return true;
                    }
                }
            }

            return false;
        }

    }

    private static InitialConstruct initializeFactory(String field) {
        var construct = new InitialConstruct();
        construct.tokenize = (context, effects, ok, nok) -> {
            var constructs = switch (field) {
                case "text" -> context.parser.constructs.text;
                case "string" -> context.parser.constructs.string;
                default -> throw new IllegalArgumentException(field);
            };
            return new TextTokenizer(context, constructs, effects)::start;
        };
        if ("text".equals(field)) {
            construct.resolveAll = InitializeText::resolveAllLineSuffixes;
        }
        return construct;
    }

    static Construct.Resolver createResolver(Construct.Resolver extraResolver) {
        return (events, context) -> {
            var index = -1;
            int enter = -1;

            // A rather boring computation (to merge adjacent `data` events) which
            // improves mm performance by 29%.
            while (++index <= events.size()) {
                var event = index < events.size() ? events.get(index) : null;

                if (enter == -1) {
                    if (event != null && event.token().type.equals(Types.data)) {
                        enter = index;
                        index++;
                    }
                } else if (event == null || !event.token().type.equals(Types.data)) {
                    // Don’t do anything if there is one data token.
                    if (index != enter + 2) {
                        events.get(enter).token().end = events.get(index - 1).token().end;
                        events.subList(enter + 2, index).clear();
                        index = enter + 2;
                    }

                    enter = -1;
                }
            }

            return extraResolver != null ? extraResolver.resolve(events, context) : events;
        };
    }


    /**
     * A rather ugly set of instructions which again looks at chunks in the input
     * stream.
     * The reason to do this here is that it is *much* faster to parse in reverse.
     * And that we can’t hook into `null` to split the line suffix before an EOF.
     * To do: figure out if we can make this into a clean utility, or even in core.
     * As it will be useful for GFMs literal autolink extension (and maybe even
     * tables?)
     */
    static List<Tokenizer.Event> resolveAllLineSuffixes(List<Tokenizer.Event> events, Tokenizer.TokenizeContext context) {
        var eventIndex = 0; // Skip first.

        while (++eventIndex <= events.size()) {
            if (
                    (eventIndex == events.size() ||
                            events.get(eventIndex).token().type.equals(Types.lineEnding)) &&
                            events.get(eventIndex - 1).token().type.equals(Types.data)
            ) {
                var data = events.get(eventIndex - 1).token();
                var chunks = context.sliceStream(data);
                var index = chunks.size();
                var bufferIndex = -1;
                var size = 0;
                boolean tabs = false;

                while (index-- != 0) {
                    var chunk = chunks.get(index);

                    if (chunk instanceof String textChunk) {
                        bufferIndex = textChunk.length();

                        while (bufferIndex > 0 && textChunk.charAt(bufferIndex - 1) == Codes.space) {
                            size++;
                            bufferIndex--;
                        }

                        if (bufferIndex != 0)
                            break;

                        bufferIndex = -1;
                    }
                    // Number
                    else if (Objects.equals(chunk, Codes.horizontalTab)) {
                        tabs = true;
                        size++;
                    } else if (Objects.equals(chunk, Codes.virtualSpace)) {
                        // Empty
                    } else {
                        // Replacement character, exit.
                        index++;
                        break;
                    }
                }

                if (size != 0) {
                    var token = new Token();

                    token.type = eventIndex == events.size() ||
                            tabs ||
                            size < Constants.hardBreakPrefixSizeMin
                            ? Types.lineSuffix
                            : Types.hardBreakTrailing;

                    token.start = new Point(
                            data.end.line(),
                            data.end.column() - size,
                            data.end.offset() - size,
                            data.start._index() + index,
                            index != 0
                                    ? bufferIndex
                                    : data.start._bufferIndex() + bufferIndex
                    );

                    token.end = data.end;

                    data.end = token.start;

                    if (data.start.offset() == data.end.offset()) {
                        token.copyTo(data);
                    } else {
                        events.add(eventIndex++, new Tokenizer.Event(Tokenizer.EventType.ENTER, token, context));
                        events.add(eventIndex++, new Tokenizer.Event(Tokenizer.EventType.EXIT, token, context));
                    }
                }

                eventIndex++;
            }
        }

        return events;
    }

}
