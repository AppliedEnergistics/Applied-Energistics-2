package appeng.libs.micromark.factory;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.ContentType;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.TokenizeContext;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;

public final class FactoryLabel {
    private FactoryLabel() {
    }

    public static State create(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok, String type, String markerType, String stringType) {
        return new StateMachine(context, effects, ok, nok, type, markerType, stringType)::start;
    }

    private static class StateMachine {
        private TokenizeContext context;
        private Tokenizer.Effects effects;
        private State ok;
        private State nok;
        private String type;
        private String markerType;
        private String stringType;

        private int size = 0;
        private boolean data;

        public StateMachine(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok, String type, String markerType, String stringType) {
            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
            this.type = type;
            this.markerType = markerType;
            this.stringType = stringType;
        }

        private State start(int code) {
            Assert.check(code == Codes.leftSquareBracket, "expected `[`");
            effects.enter(type);
            effects.enter(markerType);
            effects.consume(code);
            effects.exit(markerType);
            effects.enter(stringType);
            return this::atBreak;
        }

        private State atBreak(int code) {
            if (
                    code == Codes.eof ||
                            code == Codes.leftSquareBracket ||
                            (code == Codes.rightSquareBracket && !data) ||
                            /* To do: remove in the future once we’ve switched from
                             * `micromark-extension-footnote` to `micromark-extension-gfm-footnote`,
                             * which doesn’t need this */
                            /* Hidden footnotes hook */
                            /* c8 ignore next 3 */
                            (code == Codes.caret &&
                                    size == 0 &&
                                    context.getParser().constructs._hiddenFootnoteSupport) ||
            size > Constants.linkReferenceSizeMax
    ) {
                return nok.step(code);
            }

            if (code == Codes.rightSquareBracket) {
                effects.exit(stringType);
                effects.enter(markerType);
                effects.consume(code);
                effects.exit(markerType);
                effects.exit(type);
                return ok;
            }

            if (CharUtil.markdownLineEnding(code)) {
                effects.enter(Types.lineEnding);
                effects.consume(code);
                effects.exit(Types.lineEnding);
                return this::atBreak;
            }

            var tokenFields = new Token();
            tokenFields.contentType = ContentType.STRING;
            effects.enter(Types.chunkString, tokenFields);
            return label(code);
        }

        private State label(int code) {
            if (
                    code == Codes.eof ||
                            code == Codes.leftSquareBracket ||
                            code == Codes.rightSquareBracket ||
                            CharUtil.markdownLineEnding(code) ||
                            size++ > Constants.linkReferenceSizeMax
            ) {
                effects.exit(Types.chunkString);
                return atBreak(code);
            }

            effects.consume(code);
            data = data || !CharUtil.markdownSpace(code);
            return code == Codes.backslash ? this::labelEscape : this::label;
        }

        private State labelEscape(int code) {
            if (
                    code == Codes.leftSquareBracket ||
                            code == Codes.backslash ||
                            code == Codes.rightSquareBracket
            ) {
                effects.consume(code);
                size++;
                return this::label;
            }

            return label(code);
        }
    }

}
