package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.NamedCharacterEntities;
import appeng.libs.micromark.State;
import appeng.libs.micromark.TokenizeContext;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;

import java.util.function.IntPredicate;

public final class CharacterReference {
    private CharacterReference() {
    }

    public static final Construct characterReference;
    static {
        characterReference = new Construct();
        characterReference.name = "characterReference";
        characterReference.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects, ok, nok)::start;
    }

    private static class StateMachine {
        private final TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;

        private int size;
        private int max;
        private RefType type;

        public StateMachine(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }


        /**
         * Start of a character reference.
         *
         * <pre>
         * > | a&amp;b
         *      ^
         * > | a&#123;b
         *      ^
         * > | a&#x9;b
         *      ^
         * </pre>
         *
          */
        private State start(int code) {
            Assert.check(code == Codes.ampersand, "expected `&`");
            effects.enter(Types.characterReference);
            effects.enter(Types.characterReferenceMarker);
            effects.consume(code);
            effects.exit(Types.characterReferenceMarker);
            return this::open;
        }

        /**
         * Inside a character reference, after `&`, before `#` for numeric references
         * or an alphanumeric for named references.
         * <pre>
         * > | a&amp;b
         *       ^
         * > | a&#123;b
         *       ^
         * > | a&#x9;b
         *       ^
         * </pre>
         *
          */
        private State open(int code) {
            if (code == Codes.numberSign) {
                effects.enter(Types.characterReferenceMarkerNumeric);
                effects.consume(code);
                effects.exit(Types.characterReferenceMarkerNumeric);
                return this::numeric;
            }

            effects.enter(Types.characterReferenceValue);
            max = Constants.characterReferenceNamedSizeMax;
            type = RefType.ALPHANUMERIC;
            return value(code);
        }

        /**
         * Inside a numeric character reference, right before `x` for hexadecimals,
         * or a digit for decimals.
         * <pre>
         * > | a&#123;b
         *        ^
         * > | a&#x9;b
         *        ^
         * </pre>
         *
          */
        private State numeric(int code) {
            if (code == Codes.uppercaseX || code == Codes.lowercaseX) {
                effects.enter(Types.characterReferenceMarkerHexadecimal);
                effects.consume(code);
                effects.exit(Types.characterReferenceMarkerHexadecimal);
                effects.enter(Types.characterReferenceValue);
                max = Constants.characterReferenceHexadecimalSizeMax;
                type = RefType.HEX_NUMERIC;
                return this::value;
            }

            effects.enter(Types.characterReferenceValue);
            max = Constants.characterReferenceDecimalSizeMax;
            type = RefType.NUMERIC;
            return value(code);
        }

        /**
         * Inside a character reference value, after the markers (`&#x`, `&#`, or
         * `&`) that define its kind, but before the `;`.
         *
         * The character reference kind defines what and how many characters are
         * allowed.
         *
         * <pre>
         * > | a&amp;b
         *       ^^^
         * > | a&#123;b
         *        ^^^
         * > | a&#x9;b
         *         ^
         * </pre>
         *
          */
        private State value(int code) {
            if (code == Codes.semicolon && size != 0) {
            var token = effects.exit(Types.characterReferenceValue);

                if (
                        type == RefType.ALPHANUMERIC &&
                                !NamedCharacterEntities.isNamedReference(context.sliceSerialize(token))
                ) {
                    return nok.step(code);
                }

                effects.enter(Types.characterReferenceMarker);
                effects.consume(code);
                effects.exit(Types.characterReferenceMarker);
                effects.exit(Types.characterReference);
                return ok;
            }

            if (type.test.test(code) && size++ < max) {
                effects.consume(code);
                return this::value;
            }

            return nok.step(code);
        }

        enum RefType {
            ALPHANUMERIC(CharUtil::asciiAlphanumeric),
            NUMERIC(CharUtil::asciiDigit),
            HEX_NUMERIC(CharUtil::asciiHexDigit);
            public final IntPredicate test;

            RefType(IntPredicate test) {
                this.test = test;
            }
        }
    }
}
