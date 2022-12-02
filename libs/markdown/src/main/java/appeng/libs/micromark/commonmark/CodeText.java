package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.TokenizeContext;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.symbol.Codes;

import java.util.List;

public final class CodeText {
    private CodeText() {
    }

    public static final Construct codeText;
    static {
        codeText = new Construct();
        codeText.name = "codeText";
        codeText.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects, ok, nok)::start;
        codeText.resolve = CodeText::resolveCodeText;
        codeText.previous = CodeText::previous;
    }

    private static List<Tokenizer.Event> resolveCodeText(List<Tokenizer.Event> events, TokenizeContext context) {
        var tailExitIndex = events.size() - 4;
        var headEnterIndex = 3;
        int index;
        Integer enter = null;

        // If we start and end with an EOL or a space.
        if (
                (events.get(headEnterIndex).token().type.equals(Types.lineEnding) ||
                        events.get(headEnterIndex).token().type.equals("space")) &&
                        (events.get(tailExitIndex).token().type.equals(Types.lineEnding) ||
                                events.get(tailExitIndex).token().type.equals("space"))
        ) {
            index = headEnterIndex;

            // And we have data.
            while (++index < tailExitIndex) {
                if (events.get(index).token().type.equals(Types.codeTextData)) {
                    // Then we have padding.
                    events.get(headEnterIndex).token().type = Types.codeTextPadding;
                    events.get(tailExitIndex).token().type = Types.codeTextPadding;
                    headEnterIndex += 2;
                    tailExitIndex -= 2;
                    break;
                }
            }
        }

        // Merge adjacent spaces and data.
        index = headEnterIndex - 1;
        tailExitIndex++;

        while (++index <= tailExitIndex) {
            if (enter == null) {
                if (
                        index != tailExitIndex &&
                                !events.get(index).token().type.equals(Types.lineEnding)
                ) {
                    enter = index;
                }
            } else if (
                    index == tailExitIndex ||
                            events.get(index).token().type.equals(Types.lineEnding)
            ) {
                events.get(enter).token().type = Types.codeTextData;

                if (index != enter + 2) {
                    events.get(enter).token().end = events.get(index - 1).token().end;
                    events.subList(enter + 2, index).clear();
                    tailExitIndex -= index - enter - 2;
                    index = enter + 2;
                }

                enter = null;
            }
        }

        return events;
    }

    private static boolean previous(TokenizeContext context, int code) {
        // If there is a previous code, there will always be a tail.
        return (
                code != Codes.graveAccent ||
                        context.getLastEvent().token().type.equals(Types.characterEscape)
        );
    }

    private static class StateMachine {
        private final TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;
        int sizeOpen;
        private int size;
        private Token token;

        public StateMachine(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }


        /**
         * Start of code (text).
         *
         * <pre>
         * > | `a`
         *     ^
         * > | \`a`
         *      ^
         * </pre>
         *
         
         */
        private State start(int code) {
            Assert.check(code == Codes.graveAccent, "expected `` ` ``");
            Assert.check(previous(context, context.getPrevious()), "expected correct previous");
            effects.enter(Types.codeText);
            effects.enter(Types.codeTextSequence);
            return sequenceOpen(code);
        }

        /**
         * In the opening sequence.
         *
         * <pre>
         * > | `a`
         *     ^
         * </pre>
         *
         
         */
        private State sequenceOpen(int code) {
            if (code == Codes.graveAccent) {
                effects.consume(code);
                sizeOpen++;
                return this::sequenceOpen;
            }

            effects.exit(Types.codeTextSequence);
            return between(code);
        }

        /**
         * Between something and something else
         *
         * <pre>
         * > | `a`
         *      ^^
         * </pre>
         *
         
         */
        private State between(int code) {
            // EOF.
            if (code == Codes.eof) {
                return nok.step(code);
            }

            // Closing fence? Could also be data.
            if (code == Codes.graveAccent) {
                token = effects.enter(Types.codeTextSequence);
                size = 0;
                return sequenceClose(code);
            }

            // Tabs don’t work, and virtual spaces don’t make sense.
            if (code == Codes.space) {
                effects.enter("space");
                effects.consume(code);
                effects.exit("space");
                return this::between;
            }

            if (CharUtil.markdownLineEnding(code)) {
                effects.enter(Types.lineEnding);
                effects.consume(code);
                effects.exit(Types.lineEnding);
                return this::between;
            }

            // Data.
            effects.enter(Types.codeTextData);
            return data(code);
        }

        /**
         * In data.
         *
         * <pre>
         * > | `a`
         *      ^
         * </pre>
         *
         
         */
        private State data(int code) {
            if (
                    code == Codes.eof ||
                            code == Codes.space ||
                            code == Codes.graveAccent ||
                            CharUtil.markdownLineEnding(code)
            ) {
                effects.exit(Types.codeTextData);
                return between(code);
            }

            effects.consume(code);
            return this::data;
        }

        /**
         * In the closing sequence.
         *
         * <pre>
         * > | `a`
         *       ^
         * </pre>
         *
         
         */
        private State sequenceClose(int code) {
            // More.
            if (code == Codes.graveAccent) {
                effects.consume(code);
                size++;
                return this::sequenceClose;
            }

            // Done!
            if (size == sizeOpen) {
                effects.exit(Types.codeTextSequence);
                effects.exit(Types.codeText);
                return ok.step(code);
            }

            // More or less accents: mark as data.
            token.type = Types.codeTextData;
            return data(code);
        }
    }
}
