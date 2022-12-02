package appeng.libs.micromark.factory;

import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.ContentType;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Token;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.symbol.Codes;

public final class FactoryDestination {
    private FactoryDestination() {
    }

    public static State create(Tokenizer.Effects effects, State ok, State nok, String type, String literalType, String literalMarkerType, String rawType, String stringType) {
        return create(effects, ok, nok, type, literalType, literalMarkerType, rawType, stringType, null);
    }

    public static State create(Tokenizer.Effects effects, State ok, State nok, String type, String literalType, String literalMarkerType, String rawType, String stringType, Integer max) {
        return new StateMachine(effects, ok, nok, type, literalType, literalMarkerType, rawType, stringType, max)::start;
    }

    private static class StateMachine {
        private Tokenizer.Effects effects;
        private State ok;
        private State nok;
        private String type;
        private final String literalType;
        private final String literalMarkerType;
        private final String rawType;
        private final String stringType;

        private int limit;
        private int  balance = 0;

        public StateMachine(Tokenizer.Effects effects, State ok, State nok, String type, String literalType, String literalMarkerType, String rawType, String stringType, Integer max) {
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
            this.type = type;
            this.literalType = literalType;
            this.literalMarkerType = literalMarkerType;
            this.rawType = rawType;
            this.stringType = stringType;
            this.limit = max != null ? max : Integer.MAX_VALUE;
        }


        private State start(int code) {
            if (code == Codes.lessThan) {
                effects.enter(type);
                effects.enter(literalType);
                effects.enter(literalMarkerType);
                effects.consume(code);
                effects.exit(literalMarkerType);
                return this::destinationEnclosedBefore;
            }

            if (
                    code == Codes.eof ||
                            code == Codes.rightParenthesis ||
                            CharUtil.asciiControl(code)
            ) {
                return nok.step(code);
            }

            effects.enter(type);
            effects.enter(rawType);
            effects.enter(stringType);
            var tokenFields = new Token();
            tokenFields.contentType = ContentType.STRING;
            effects.enter(Types.chunkString, tokenFields);
            return destinationRaw(code);
        }

        private State destinationEnclosedBefore(int code) {
            if (code == Codes.greaterThan) {
                effects.enter(literalMarkerType);
                effects.consume(code);
                effects.exit(literalMarkerType);
                effects.exit(literalType);
                effects.exit(type);
                return ok;
            }

            effects.enter(stringType);
                    var tokenFields = new Token();
            tokenFields.contentType = ContentType.STRING;
            effects.enter(Types.chunkString, tokenFields);
            return destinationEnclosed(code);
        }

        private State destinationEnclosed(int code) {
            if (code == Codes.greaterThan) {
                effects.exit(Types.chunkString);
                effects.exit(stringType);
                return destinationEnclosedBefore(code);
            }

            if (
                    code == Codes.eof ||
                            code == Codes.lessThan ||
                            CharUtil.markdownLineEnding(code)
            ) {
                return nok.step(code);
            }

            effects.consume(code);
            return code == Codes.backslash
                    ? this::destinationEnclosedEscape
                    : this::destinationEnclosed;
        }

        private State destinationEnclosedEscape(int code) {
            if (
                    code == Codes.lessThan ||
                            code == Codes.greaterThan ||
                            code == Codes.backslash
            ) {
                effects.consume(code);
                return this::destinationEnclosed;
            }

            return destinationEnclosed(code);
        }

        private State destinationRaw(int code) {
            if (code == Codes.leftParenthesis) {
                if (++balance > limit) return nok.step(code);
                effects.consume(code);
                return this::destinationRaw;
            }

            if (code == Codes.rightParenthesis) {
                if (balance-- == 0) {
                    effects.exit(Types.chunkString);
                    effects.exit(stringType);
                    effects.exit(rawType);
                    effects.exit(type);
                    return ok.step(code);
                }

                effects.consume(code);
                return this::destinationRaw;
            }

            if (code == Codes.eof || CharUtil.markdownLineEndingOrSpace(code)) {
                if (balance != 0) return nok.step(code);
                effects.exit(Types.chunkString);
                effects.exit(stringType);
                effects.exit(rawType);
                effects.exit(type);
                return ok.step(code);
            }

            if (CharUtil.asciiControl(code)) return nok.step(code);
            effects.consume(code);
            return code == Codes.backslash ? this::destinationRawEscape : this::destinationRaw;
        }

        private State destinationRawEscape(int code) {
            if (
                    code == Codes.leftParenthesis ||
                            code == Codes.rightParenthesis ||
                            code == Codes.backslash
            ) {
                effects.consume(code);
                return this::destinationRaw;
            }

            return destinationRaw(code);
        }
    }

}
