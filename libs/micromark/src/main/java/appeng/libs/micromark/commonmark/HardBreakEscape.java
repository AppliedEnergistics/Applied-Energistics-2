package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.symbol.Codes;

public final class HardBreakEscape {
    private HardBreakEscape() {
    }

    public static final Construct hardBreakEscape;

    static {
        hardBreakEscape = new Construct();
        hardBreakEscape.name = "hardBreakEscape";
        hardBreakEscape.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects, ok, nok)::start;
    }

    private static class StateMachine {
        private final Tokenizer.TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;

        public StateMachine(Tokenizer.TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }

        /**
         * Start of a hard break (escape).
         *
         * <pre>
         * > | a\
         *      ^
         *   | b
         * </pre>
         */
        private State start(int code) {
            Assert.check(code == Codes.backslash, "expected `\\`");
            effects.enter(Types.hardBreakEscape);
            effects.consume(code);
            return this::open;
        }

        /**
         * At the end of a hard break (escape), after `\`.
         *
         * <pre>
         * > | a\
         *       ^
         *   | b
         * </pre>
         */
        private State open(int code) {
            if (CharUtil.markdownLineEnding(code)) {
                effects.exit(Types.hardBreakEscape);
                return ok.step(code);
            }

            return nok.step(code);
        }
    }
}
