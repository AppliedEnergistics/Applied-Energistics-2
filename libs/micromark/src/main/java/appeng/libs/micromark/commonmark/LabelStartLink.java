package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.symbol.Codes;

public final class LabelStartLink {
    private LabelStartLink() {
    }

    public static final Construct labelStartLink;
    static {
        labelStartLink = new Construct();
        labelStartLink.name = "labelStartLink";
        labelStartLink.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects, ok, nok)::start;
        labelStartLink.resolveAll = LabelEnd::resolveAll;
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
         * Start of label (link) start.
         *
         * <pre>
         * > | a [b] c
         *       ^
         * </pre>
         *
         
         */
        private State start(int code) {
            Assert.check(code == Codes.leftSquareBracket, "expected `[`");
            effects.enter(Types.labelLink);
            effects.enter(Types.labelMarker);
            effects.consume(code);
            effects.exit(Types.labelMarker);
            effects.exit(Types.labelLink);
            return this::after;
        }

        
        private State after(int code) {
            /* To do: remove in the future once we’ve switched from
             * `micromark-extension-footnote` to `micromark-extension-gfm-footnote`,
             * which doesn’t need this */
            /* Hidden footnotes hook. */
            /* c8 ignore next 3 */
            return code == Codes.caret &&
                    context.parser.constructs._hiddenFootnoteSupport
                    ? nok.step(code)
                    : ok.step(code);
        }
    }
}
