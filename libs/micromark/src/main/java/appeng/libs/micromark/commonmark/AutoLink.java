package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;

public final class AutoLink {
    private AutoLink() {
    }

    public static final Construct autolink;

    static {
        autolink = new Construct();
        autolink.name = "autolink";
        autolink.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects, ok, nok)::start;
    }

    static class StateMachine {
        private final Tokenizer.TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;
        int size = 1;

        public StateMachine(Tokenizer.TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {
            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }


        /**
         * Start of an autolink.
         *
         * <pre>
         * > | a<https://example.com>b
         *      ^
         * > | a<user@example.com>b
         *      ^
         * </pre>
         */
        private State start(int code) {
            Assert.check(code == Codes.lessThan, "expected `<`");
            effects.enter(Types.autolink);
            effects.enter(Types.autolinkMarker);
            effects.consume(code);
            effects.exit(Types.autolinkMarker);
            effects.enter(Types.autolinkProtocol);
            return this::open;
        }

        /**
         * After `<`, before the protocol.
         *
         * <pre>
         * > | a<https://example.com>b
         *       ^
         * > | a<user@example.com>b
         *       ^
         * </pre>
         */
        private State open(int code) {
            if (CharUtil.asciiAlpha(code)) {
                effects.consume(code);
                return this::schemeOrEmailAtext;
            }

            return emailAtext(code);
        }

        /**
         * After the first byte of the protocol or email name.
         *
         * <pre>
         * > | a<https://example.com>b
         *        ^
         * > | a<user@example.com>b
         *        ^
         * </pre>
         */
        private State schemeOrEmailAtext(int code) {
            return code == Codes.plusSign ||
                    code == Codes.dash ||
                    code == Codes.dot ||
                    CharUtil.asciiAlphanumeric(code)
                    ? schemeInsideOrEmailAtext(code)
                    : emailAtext(code);
        }

        /**
         * Inside an ambiguous protocol or email name.
         *
         * <pre>
         * > | a<https://example.com>b
         *        ^
         * > | a<user@example.com>b
         *        ^
         * </pre>
         */
        private State schemeInsideOrEmailAtext(int code) {
            if (code == Codes.colon) {
                effects.consume(code);
                return this::urlInside;
            }

            if (
                    (code == Codes.plusSign ||
                            code == Codes.dash ||
                            code == Codes.dot ||
                            CharUtil.asciiAlphanumeric(code)) &&
                            size++ < Constants.autolinkSchemeSizeMax
            ) {
                effects.consume(code);
                return this::schemeInsideOrEmailAtext;
            }

            return emailAtext(code);
        }

        /**
         * Inside a URL, after the protocol.
         *
         * <pre>
         * > | a<https://example.com>b
         *             ^
         * </pre>
         */
        private State urlInside(int code) {
            if (code == Codes.greaterThan) {
                effects.exit(Types.autolinkProtocol);
                return end(code);
            }

            // ASCII control or space.
            if (
                    code == Codes.eof ||
                            code == Codes.space ||
                            code == Codes.lessThan ||
                            CharUtil.asciiControl(code)
            ) {
                return nok.step(code);
            }

            effects.consume(code);
            return this::urlInside;
        }

        /**
         * Inside email atext.
         *
         * <pre>
         * > | a<user.name@example.com>b
         *              ^
         * </pre>
         */
        private State emailAtext(int code) {
            if (code == Codes.atSign) {
                effects.consume(code);
                size = 0;
                return this::emailAtSignOrDot;
            }

            if (CharUtil.asciiAtext(code)) {
                effects.consume(code);
                return this::emailAtext;
            }

            return nok.step(code);
        }

        /**
         * After an at-sign or a dot in the label.
         *
         * <pre>
         * > | a<user.name@example.com>b
         *                 ^       ^
         * </pre>
         */
        private State emailAtSignOrDot(int code) {
            return CharUtil.asciiAlphanumeric(code) ? emailLabel(code) : nok.step(code);
        }

        /**
         * In the label, where `.` and `>` are allowed.
         *
         * <pre>
         * > | a<user.name@example.com>b
         *                   ^
         * </pre>
         */
        private State emailLabel(int code) {
            if (code == Codes.dot) {
                effects.consume(code);
                size = 0;
                return this::emailAtSignOrDot;
            }

            if (code == Codes.greaterThan) {
                // Exit, then change the token type.
                effects.exit(Types.autolinkProtocol).type = Types.autolinkEmail;
                return end(code);
            }

            return emailValue(code);
        }

        /**
         * In the label, where `.` and `>` are *not* allowed.
         *
         * Though, this is also used in `email_label` to parse other values.
         *
         * <pre>
         * > | a<user.name@ex-ample.com>b
         *                    ^
         * </pre>
         */
        private State emailValue(int code) {
            if (
                    (code == Codes.dash || CharUtil.asciiAlphanumeric(code)) &&
                            size++ < Constants.autolinkDomainSizeMax
            ) {
                effects.consume(code);
                return code == Codes.dash ? this::emailValue : this::emailLabel;
            }

            return nok.step(code);
        }

        /**
         * At the `>`.
         *
         * <pre>
         * > | a<https://example.com>b
         *                          ^
         * > | a<user@example.com>b
         *                       ^
         * </pre>
         */
        private State end(int code) {
            Assert.check(code == Codes.greaterThan, "expected `>`");
            effects.enter(Types.autolinkMarker);
            effects.consume(code);
            effects.exit(Types.autolinkMarker);
            effects.exit(Types.autolink);
            return ok;
        }
    }
}
