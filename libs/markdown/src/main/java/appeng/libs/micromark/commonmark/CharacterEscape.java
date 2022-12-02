package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.State;
import appeng.libs.micromark.TokenizeContext;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.symbol.Codes;

public final class CharacterEscape {
    private CharacterEscape() {
    }

    public static final Construct characterEscape;

    static {
        characterEscape = new Construct();
        characterEscape.name = "characterEscape";
        characterEscape.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects, ok, nok)::start;
    }

    private static class StateMachine {
        private final TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;

        public StateMachine(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {
            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }

            /**
             * Start of a character escape.
             *
             * <pre>
             * > | a\*b
             *      ^
             * </pre>
             */
            private State start(int code) {
                    Assert.check(code == Codes.backslash, "expected `\\`");
                    effects.enter(Types.characterEscape);
                    effects.enter(Types.escapeMarker);
                    effects.consume(code);
                    effects.exit(Types.escapeMarker);
                    return this::open;
          }

            /**
             * Inside a character escape, after `\`.
             *
             * <pre>
             * > | a\*b
             *       ^
             * </pre>
             */
            private State open(int code) {
            if (CharUtil.asciiPunctuation(code)) {
                effects.enter(Types.characterEscapeValue);
                effects.consume(code);
                effects.exit(Types.characterEscapeValue);
                effects.exit(Types.characterEscape);
                return ok;
            }

            return nok.step(code);
        }

    }
}
