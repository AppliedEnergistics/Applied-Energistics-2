package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.TokenizeContext;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.factory.FactorySpace;
import appeng.libs.micromark.symbol.Codes;

import java.util.List;

public final class SetextUnderline {
    private SetextUnderline() {
    }

    public static final Construct setextUnderline;

    static {
        setextUnderline = new Construct();
        setextUnderline.name = "setextUnderline";
        setextUnderline.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects, ok, nok)::start;
        setextUnderline.resolveTo = SetextUnderline::resolveToSetextUnderline;
    }

    private static List<Tokenizer.Event> resolveToSetextUnderline(List<Tokenizer.Event> events, TokenizeContext context) {
        var index = events.size();
        Integer content = null;
        Integer text = null;
        Integer definition = null;

        // Find the opening of the content.
        // It’ll always exist: we don’t tokenize if it isn’t there.
        while (index-- > 0) {
            if (events.get(index).isEnter()) {
                if (events.get(index).token().type.equals(Types.content)) {
                    content = index;
                    break;
                }

                if (events.get(index).token().type.equals(Types.paragraph)) {
                    text = index;
                }
            }
            // Exit
            else {
                if (events.get(index).token().type.equals(Types.content)) {
                    // Remove the content end (if needed we’ll add it later)
                    events.remove(index);
                }

                if (definition == null && events.get(index).token().type.equals(Types.definition)) {
                    definition = index;
                }
            }
        }

        Assert.check(text != null, "expected a `text` index to be found");
        Assert.check(content != null, "expected a `text` index to be found");

        var heading = new Token();
        heading.type = Types.setextHeading;
        heading.start = events.get(text).token().start;
        heading.end = events.get(events.size() - 1).token().end;

        // Change the paragraph to setext heading text.
        events.get(text).token().type = Types.setextHeadingText;

        // If we have definitions in the content, we’ll keep on having content,
        // but we need move it.
        if (definition != null) {
            events.add(text, Tokenizer.Event.enter(heading, context));
            events.add(definition + 1, Tokenizer.Event.exit(events.get(content).token(), context));
            events.get(content).token().end = events.get(definition).token().end;
        } else {
            var prevEvent = events.get(content);
            events.set(content, new Tokenizer.Event(
                    prevEvent.type(),
                    heading,
                    prevEvent.context()
            ));
        }

        // Add the heading exit at the end.
        events.add(Tokenizer.Event.exit(heading, context));

        return events;
    }

    private static class StateMachine {
        private final TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;
        int marker;
        boolean paragraph;

        public StateMachine(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;

            var index = context.getEvents().size();
            // Find an opening.
            while (index-- > 0) {
                // Skip enter/exit of line ending, line prefix, and content.
                // We can now either have a definition or a paragraph.
                if (
                        !context.getEvents().get(index).token().type.equals(Types.lineEnding) &&
                                !context.getEvents().get(index).token().type.equals(Types.linePrefix) &&
                                !context.getEvents().get(index).token().type.equals(Types.content)
                ) {
                    paragraph = context.getEvents().get(index).token().type.equals(Types.paragraph);
                    break;
                }
            }
        }


        private State start(int code) {
            Assert.check(
                    code == Codes.dash || code == Codes.equalsTo,
                    "expected `=` or `-`"
            );

            if (!context.isOnLazyLine() && (context.isInterrupt() || paragraph)) {
                effects.enter(Types.setextHeadingLine);
                effects.enter(Types.setextHeadingLineSequence);
                marker = code;
                return closingSequence(code);
            }

            return nok.step(code);
        }


        private State closingSequence(int code) {
            if (code == marker) {
                effects.consume(code);
                return this::closingSequence;
            }

            effects.exit(Types.setextHeadingLineSequence);
            return FactorySpace.create(effects, this::closingSequenceEnd, Types.lineSuffix).step(code);
        }


        private State closingSequenceEnd(int code) {
            if (code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                effects.exit(Types.setextHeadingLine);
                return ok.step(code);
            }

            return nok.step(code);
        }

    }
}
