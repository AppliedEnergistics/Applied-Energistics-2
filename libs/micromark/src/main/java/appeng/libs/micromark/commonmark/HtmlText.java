package appeng.libs.micromark.commonmark;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.State;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.factory.FactorySpace;
import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class HtmlText {
    private HtmlText() {
    }

    public static final Construct htmlText;
    static {
        htmlText = new Construct();
        htmlText.name = "htmlText";
        htmlText.tokenize = (context, effects, ok, nok) -> new StateMachine(context, effects, ok, nok)::start;
    }

    private static class StateMachine {
        private final Tokenizer.TokenizeContext context;
        private final Tokenizer.Effects effects;
        private final State ok;
        private final State nok;
        @Nullable
        Integer marker;
        String buffer;
        private int index;
        
        State returnState;

        public StateMachine(Tokenizer.TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {

            this.context = context;
            this.effects = effects;
            this.ok = ok;
            this.nok = nok;
        }

        /**
         * Start of HTML (text)
         *
         * <pre>
         * > | a <b> c
         *       ^
         * </pre>
         *
         
         */
        private State start(int code) {
            Assert.check(code == Codes.lessThan, "expected `<`");
            effects.enter(Types.htmlText);
            effects.enter(Types.htmlTextData);
            effects.consume(code);
            return this::open;
        }

        /**
         * After `<`, before a tag name or other stuff.
         *
         * <pre>
         * > | a <b> c
         *        ^
         * > | a <!doctype> c
         *        ^
         * > | a <!--b--> c
         *        ^
         * </pre>
         *
         
         */
        private State open(int code) {
            if (code == Codes.exclamationMark) {
                effects.consume(code);
                return this::declarationOpen;
            }

            if (code == Codes.slash) {
                effects.consume(code);
                return this::tagCloseStart;
            }

            if (code == Codes.questionMark) {
                effects.consume(code);
                return this::instruction;
            }

            if (CharUtil.asciiAlpha(code)) {
                effects.consume(code);
                return this::tagOpen;
            }

            return nok.step(code);
        }

        /**
         * After `<!`, so inside a declaration, comment, or CDATA.
         *
         * <pre>
         * > | a <!doctype> c
         *         ^
         * > | a <!--b--> c
         *         ^
         * > | a <![CDATA[>&<]]> c
         *         ^
         * </pre>
         *
         
         */
        private State declarationOpen(int code) {
            if (code == Codes.dash) {
                effects.consume(code);
                return this::commentOpenInside;
            }

            if (code == Codes.leftSquareBracket) {
                effects.consume(code);
                buffer = Constants.cdataOpeningString;
                index = 0;
                return this::cdataOpenInside;
            }

            if (CharUtil.asciiAlpha(code)) {
                effects.consume(code);
                return this::declaration;
            }

            return nok.step(code);
        }

        /**
         * After `<!-`, inside a comment, before another `-`.
         *
         * <pre>
         * > | a <!--b--> c
         *          ^
         * </pre>
         *
         
         */
        private State commentOpenInside(int code) {
            if (code == Codes.dash) {
                effects.consume(code);
                return this::commentStart;
            }

            return nok.step(code);
        }

        /**
         * After `<!--`, inside a comment
         * <p>
         * > 👉 **Note**: html (flow) does allow `<!-->` or `<!--->` as empty
         * > comments.
         * > This is prohibited in html (text).
         * > See: <https://github.com/commonmark/commonmark-spec/issues/712>.
         *
         * <pre>
         * > | a <!--b--> c
         *           ^
         * </pre>
         *
         
         */
        private State commentStart(int code) {
            if (code == Codes.greaterThan) {
                return nok.step(code);
            }

            if (code == Codes.dash) {
                effects.consume(code);
                return this::commentStartDash;
            }

            return comment(code);
        }

        /**
         * After `<!---`, inside a comment
         * <p>
         * > 👉 **Note**: html (flow) does allow `<!-->` or `<!--->` as empty
         * > comments.
         * > This is prohibited in html (text).
         * > See: <https://github.com/commonmark/commonmark-spec/issues/712>.
         *
         * <pre>
         * > | a <!---b--> c
         *            ^
         * </pre>
         *
         
         */
        private State commentStartDash(int code) {
            if (code == Codes.greaterThan) {
                return nok.step(code);
            }

            return comment(code);
        }

        /**
         * In a comment.
         *
         * <pre>
         * > | a <!--b--> c
         *           ^
         * </pre>
         *
         
         */
        private State comment(int code) {
            if (code == Codes.eof) {
                return nok.step(code);
            }

            if (code == Codes.dash) {
                effects.consume(code);
                return this::commentClose;
            }

            if (CharUtil.markdownLineEnding(code)) {
                returnState = this::comment;
                return atLineEnding(code);
            }

            effects.consume(code);
            return this::comment;
        }

        /**
         * In a comment, after `-`.
         *
         * <pre>
         * > | a <!--b--> c
         *             ^
         * </pre>
         *
         
         */
        private State commentClose(int code) {
            if (code == Codes.dash) {
                effects.consume(code);
                return this::end;
            }

            return comment(code);
        }

        /**
         * After `<![`, inside CDATA, expecting `CDATA[`.
         *
         * <pre>
         * > | a <![CDATA[>&<]]> b
         *          ^^^^^^
         * </pre>
         *
         
         */
        private State cdataOpenInside(int code) {
            if (code == buffer.charAt(index++)) {
                effects.consume(code);
                return index == buffer.length() ? this::cdata : this::cdataOpenInside;
            }

            return nok.step(code);
        }

        /**
         * In CDATA.
         *
         * <pre>
         * > | a <![CDATA[>&<]]> b
         *                ^^^
         * </pre>
         *
         
         */
        private State cdata(int code) {
            if (code == Codes.eof) {
                return nok.step(code);
            }

            if (code == Codes.rightSquareBracket) {
                effects.consume(code);
                return this::cdataClose;
            }

            if (CharUtil.markdownLineEnding(code)) {
                returnState = this::cdata;
                return atLineEnding(code);
            }

            effects.consume(code);
            return this::cdata;
        }

        /**
         * In CDATA, after `]`.
         *
         * <pre>
         * > | a <![CDATA[>&<]]> b
         *                    ^
         * </pre>
         *
         
         */
        private State cdataClose(int code) {
            if (code == Codes.rightSquareBracket) {
                effects.consume(code);
                return this::cdataEnd;
            }

            return cdata(code);
        }

        /**
         * In CDATA, after `]]`.
         *
         * <pre>
         * > | a <![CDATA[>&<]]> b
         *                     ^
         * </pre>
         *
         
         */
        private State cdataEnd(int code) {
            if (code == Codes.greaterThan) {
                return end(code);
            }

            if (code == Codes.rightSquareBracket) {
                effects.consume(code);
                return this::cdataEnd;
            }

            return cdata(code);
        }

        /**
         * In a declaration.
         *
         * <pre>
         * > | a <!b> c
         *          ^
         * </pre>
         *
         
         */
        private State declaration(int code) {
            if (code == Codes.eof || code == Codes.greaterThan) {
                return end(code);
            }

            if (CharUtil.markdownLineEnding(code)) {
                returnState = this::declaration;
                return atLineEnding(code);
            }

            effects.consume(code);
            return this::declaration;
        }

        /**
         * In an instruction.
         *
         * <pre>
         * > | a <?b?> c
         *         ^
         * </pre>
         *
         
         */
        private State instruction(int code) {
            if (code == Codes.eof) {
                return nok.step(code);
            }

            if (code == Codes.questionMark) {
                effects.consume(code);
                return this::instructionClose;
            }

            if (CharUtil.markdownLineEnding(code)) {
                returnState = this::instruction;
                return atLineEnding(code);
            }

            effects.consume(code);
            return this::instruction;
        }

        /**
         * In an instruction, after `?`.
         *
         * <pre>
         * > | a <?b?> c
         *           ^
         * </pre>
         *
         
         */
        private State instructionClose(int code) {
            return code == Codes.greaterThan ? end(code) : instruction(code);
        }

        /**
         * After `</`, in a closing tag, before a tag name.
         *
         * <pre>
         * > | a </b> c
         *         ^
         * </pre>
         *
         
         */
        private State tagCloseStart(int code) {
            if (CharUtil.asciiAlpha(code)) {
                effects.consume(code);
                return this::tagClose;
            }

            return nok.step(code);
        }

        /**
         * After `</x`, in a tag name.
         *
         * <pre>
         * > | a </b> c
         *          ^
         * </pre>
         *
         
         */
        private State tagClose(int code) {
            if (code == Codes.dash || CharUtil.asciiAlphanumeric(code)) {
                effects.consume(code);
                return this::tagClose;
            }

            return tagCloseBetween(code);
        }

        /**
         * In a closing tag, after the tag name.
         *
         * <pre>
         * > | a </b> c
         *          ^
         * </pre>
         *
         
         */
        private State tagCloseBetween(int code) {
            if (CharUtil.markdownLineEnding(code)) {
                returnState = this::tagCloseBetween;
                return atLineEnding(code);
            }

            if (CharUtil.markdownSpace(code)) {
                effects.consume(code);
                return this::tagCloseBetween;
            }

            return end(code);
        }

        /**
         * After `<x`, in an opening tag name.
         *
         * <pre>
         * > | a <b> c
         *         ^
         * </pre>
         *
         
         */
        private State tagOpen(int code) {
            if (code == Codes.dash || CharUtil.asciiAlphanumeric(code)) {
                effects.consume(code);
                return this::tagOpen;
            }

            if (
                    code == Codes.slash ||
                            code == Codes.greaterThan ||
                            CharUtil.markdownLineEndingOrSpace(code)
            ) {
                return tagOpenBetween(code);
            }

            return nok.step(code);
        }

        /**
         * In an opening tag, after the tag name.
         *
         * <pre>
         * > | a <b> c
         *         ^
         * </pre>
         *
         
         */
        private State tagOpenBetween(int code) {
            if (code == Codes.slash) {
                effects.consume(code);
                return this::end;
            }

            if (code == Codes.colon || code == Codes.underscore || CharUtil.asciiAlpha(code)) {
                effects.consume(code);
                return this::tagOpenAttributeName;
            }

            if (CharUtil.markdownLineEnding(code)) {
                returnState = this::tagOpenBetween;
                return atLineEnding(code);
            }

            if (CharUtil.markdownSpace(code)) {
                effects.consume(code);
                return this::tagOpenBetween;
            }

            return end(code);
        }

        /**
         * In an attribute name.
         *
         * <pre>
         * > | a <b c> d
         *          ^
         * </pre>
         *
         
         */
        private State tagOpenAttributeName(int code) {
            if (
                    code == Codes.dash ||
                            code == Codes.dot ||
                            code == Codes.colon ||
                            code == Codes.underscore ||
                            CharUtil.asciiAlphanumeric(code)
            ) {
                effects.consume(code);
                return this::tagOpenAttributeName;
            }

            return tagOpenAttributeNameAfter(code);
        }

        /**
         * After an attribute name, before an attribute initializer, the end of the
         * tag, or whitespace.
         *
         * <pre>
         * > | a <b c> d
         *           ^
         * </pre>
         *
         
         */
        private State tagOpenAttributeNameAfter(int code) {
            if (code == Codes.equalsTo) {
                effects.consume(code);
                return this::tagOpenAttributeValueBefore;
            }

            if (CharUtil.markdownLineEnding(code)) {
                returnState = this::tagOpenAttributeNameAfter;
                return atLineEnding(code);
            }

            if (CharUtil.markdownSpace(code)) {
                effects.consume(code);
                return this::tagOpenAttributeNameAfter;
            }

            return tagOpenBetween(code);
        }

        /**
         * Before an unquoted, double quoted, or single quoted attribute value,
         * allowing whitespace.
         *
         * <pre>
         * > | a <b c=d> e
         *            ^
         * </pre>
         *
         
         */
        private State tagOpenAttributeValueBefore(int code) {
            if (
                    code == Codes.eof ||
                            code == Codes.lessThan ||
                            code == Codes.equalsTo ||
                            code == Codes.greaterThan ||
                            code == Codes.graveAccent
            ) {
                return nok.step(code);
            }

            if (code == Codes.quotationMark || code == Codes.apostrophe) {
                effects.consume(code);
                marker = code;
                return this::tagOpenAttributeValueQuoted;
            }

            if (CharUtil.markdownLineEnding(code)) {
                returnState = this::tagOpenAttributeValueBefore;
                return atLineEnding(code);
            }

            if (CharUtil.markdownSpace(code)) {
                effects.consume(code);
                return this::tagOpenAttributeValueBefore;
            }

            effects.consume(code);
            marker = null;
            return this::tagOpenAttributeValueUnquoted;
        }

        /**
         * In a double or single quoted attribute value.
         *
         * <pre>
         * > | a <b c="d"> e
         *             ^
         * </pre>
         *
         
         */
        private State tagOpenAttributeValueQuoted(int code) {
            if (Objects.equals(code, marker)) {
                effects.consume(code);
                return this::tagOpenAttributeValueQuotedAfter;
            }

            if (code == Codes.eof) {
                return nok.step(code);
            }

            if (CharUtil.markdownLineEnding(code)) {
                returnState = this::tagOpenAttributeValueQuoted;
                return atLineEnding(code);
            }

            effects.consume(code);
            return this::tagOpenAttributeValueQuoted;
        }

        /**
         * In an unquoted attribute value.
         *
         * <pre>
         * > | a <b c=d> e
         *            ^
         * </pre>
         *
         
         */
        private State tagOpenAttributeValueUnquoted(int code) {
            if (
                    code == Codes.eof ||
                            code == Codes.quotationMark ||
                            code == Codes.apostrophe ||
                            code == Codes.lessThan ||
                            code == Codes.equalsTo ||
                            code == Codes.graveAccent
            ) {
                return nok.step(code);
            }

            if (
                    code == Codes.greaterThan ||
                            code == Codes.slash ||
                            CharUtil.markdownLineEndingOrSpace(code)
            ) {
                return tagOpenBetween(code);
            }

            effects.consume(code);
            return this::tagOpenAttributeValueUnquoted;
        }

        /**
         * After a double or single quoted attribute value, before whitespace or the
         * end of the tag.
         *
         * <pre>
         * > | a <b c="d"> e
         *               ^
         * </pre>
         *
         
         */
        private State tagOpenAttributeValueQuotedAfter(int code) {
            if (
                    code == Codes.greaterThan ||
                            code == Codes.slash ||
                            CharUtil.markdownLineEndingOrSpace(code)
            ) {
                return tagOpenBetween(code);
            }

            return nok.step(code);
        }

        /**
         * In certain circumstances of a complete tag where only an `>` is allowed.
         *
         * <pre>
         * > | a <b c="d"> e
         *               ^
         * </pre>
         *
         
         */
        private State end(int code) {
            if (code == Codes.greaterThan) {
                effects.consume(code);
                effects.exit(Types.htmlTextData);
                effects.exit(Types.htmlText);
                return ok;
            }

            return nok.step(code);
        }

        /**
         * At an allowed line ending.
         * <p>
         * > 👉 **Note**: we can’t have blank lines in text, so no need to worry about
         * > empty tokens.
         *
         * <pre>
         * > | a <!--a
         *            ^
         *   | b-->
         * </pre>
         *
         
         */
        private State atLineEnding(int code) {
            Assert.check(returnState != null, "expected return state");
            Assert.check(CharUtil.markdownLineEnding(code), "expected eol");
            effects.exit(Types.htmlTextData);
            effects.enter(Types.lineEnding);
            effects.consume(code);
            effects.exit(Types.lineEnding);
            return FactorySpace.create(
                    effects,
                    this::afterPrefix,
                    Types.linePrefix,
                    context.parser.constructs.nullDisable.contains("codeIndented")
                    ? null
                    : Constants.tabSize
    );
        }

        /**
         * After a line ending.
         * <p>
         * > 👉 **Note**: we can’t have blank lines in text, so no need to worry about
         * > empty tokens.
         *
         * <pre>
         *   | a <!--a
         * > | b-->
         *     ^
         * </pre>
         *
         
         */
        private State afterPrefix(int code) {
            effects.enter(Types.htmlTextData);
            return returnState.step(code);
        }
    }
}
