package appeng.libs.micromark.factory;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.ContentType;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.symbol.Codes;

public final class FactoryTitle {
    private FactoryTitle() {
    }

    public static State create(Tokenizer.Effects effects, State ok, State nok, String type, String markerType, String stringType) {
        return new StateMachine(effects, ok, nok, type, markerType, stringType)::start;
    }

    private static class StateMachine {
        private Tokenizer.Effects effects;
        private State ok;
        private State nok;
        private String type;
        private String markerType;
        private String stringType;
        private int marker;

        public StateMachine(Tokenizer.Effects effects, State ok, State nok, String type, String markerType, String stringType) {
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
            this.type = type;
            this.markerType = markerType;
            this.stringType = stringType;
        }

        public State start(int code) {
            Assert.check(
                    code == Codes.quotationMark ||
                            code == Codes.apostrophe ||
                            code == Codes.leftParenthesis,
                    "expected `\"`, `'`, or `(`"
            );
            effects.enter(type);
            effects.enter(markerType);
            effects.consume(code);
            effects.exit(markerType);
            marker = code == Codes.leftParenthesis ? Codes.rightParenthesis : code;
            return this::atFirstTitleBreak;
        }

        private State atFirstTitleBreak(int code) {
            if (code == marker) {
                effects.enter(markerType);
                effects.consume(code);
                effects.exit(markerType);
                effects.exit(type);
                return ok;
            }

            effects.enter(stringType);
            return atTitleBreak(code);
        }

        private State atTitleBreak(int code) {
            if (code == marker) {
                effects.exit(stringType);
                return atFirstTitleBreak(marker);
            }

            if (code == Codes.eof) {
                return nok.step(code);
            }

            // Note: blank lines can’t exist in content.
            if (CharUtil.markdownLineEnding(code)) {
                effects.enter(Types.lineEnding);
                effects.consume(code);
                effects.exit(Types.lineEnding);
                return FactorySpace.create(effects, this::atTitleBreak, Types.linePrefix);
            }

            var tokenFields = new Token();
            tokenFields.contentType = ContentType.STRING;
            effects.enter(Types.chunkString, tokenFields);
            return title(code);
        }


        private State title(int code) {
            if (code == marker || code == Codes.eof || CharUtil.markdownLineEnding(code)) {
                effects.exit(Types.chunkString);
                return atTitleBreak(code);
            }

            effects.consume(code);
            return code == Codes.backslash ? this::titleEscape : this::title;
        }


        private State titleEscape(int code) {
            if (code == marker || code == Codes.backslash) {
                effects.consume(code);
                return this::title;
            }

            return title(code);
        }
    }
}
