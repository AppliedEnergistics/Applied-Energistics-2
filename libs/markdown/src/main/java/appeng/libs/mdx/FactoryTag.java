package appeng.libs.mdx;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.ParseException;
import appeng.libs.micromark.Point;
import appeng.libs.micromark.State;
import appeng.libs.micromark.TokenizeContext;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.factory.FactorySpace;
import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;

import java.util.Locale;

import static appeng.libs.mdx.EcmaScriptIdentifiers.isCont;
import static appeng.libs.mdx.EcmaScriptIdentifiers.isStart;

public final class FactoryTag {
    private FactoryTag() {
    }

    private static final Construct lazyLineEnd;

    static {
        lazyLineEnd = new Construct();
        lazyLineEnd.tokenize = FactoryTag::tokenizeLazyLineEnd;
        lazyLineEnd.partial = true;
    }

    public static State create(
            TokenizeContext context,
            Tokenizer.Effects effects,
            State ok,
            State nok,
            boolean allowLazy,
            String tagType,
            String tagMarkerType,
            String tagClosingMarkerType,
            String tagSelfClosingMarker,
            String tagNameType,
            String tagNamePrimaryType,
            String tagNameMemberMarkerType,
            String tagNameMemberType,
            String tagNamePrefixMarkerType,
            String tagNameLocalType,
            String tagExpressionAttributeType,
            String tagExpressionAttributeMarkerType,
            String tagExpressionAttributeValueType,
            String tagAttributeType,
            String tagAttributeNameType,
            String tagAttributeNamePrimaryType,
            String tagAttributeNamePrefixMarkerType,
            String tagAttributeNameLocalType,
            String tagAttributeInitializerMarkerType,
            String tagAttributeValueLiteralType,
            String tagAttributeValueLiteralMarkerType,
            String tagAttributeValueLiteralValueType,
            String tagAttributeValueExpressionType,
            String tagAttributeValueExpressionMarkerType,
            String tagAttributeValueExpressionValueType
    ) {

        class StateMachine {
            State returnState;
            Integer marker;
            Point startPoint;

            State start(int code) {
                Assert.check(code == Codes.lessThan, "expected `<`");
                startPoint = context.now();
                effects.enter(tagType);
                effects.enter(tagMarkerType);
                effects.consume(code);
                effects.exit(tagMarkerType);
                return this::afterStart;
            }

            State afterStart(int code) {
                // Deviate from JSX, which allows arbitrary whitespace.
                // See: <https://github.com/micromark/micromark-extension-mdx-jsx/issues/7>.
                if (CharUtil.markdownLineEnding(code) || CharUtil.markdownSpace(code)) {
                    return nok.step(code);
                }

                // Any other ES whitespace does not get this treatment.
                returnState = this::beforeName;
                return optionalEsWhitespace(code);
            }

            // Right after `<`, before an optional name.
            State beforeName(int code) {
                // Closing tag.
                if (code == Codes.slash) {
                    effects.enter(tagClosingMarkerType);
                    effects.consume(code);
                    effects.exit(tagClosingMarkerType);
                    returnState = this::beforeClosingTagName;
                    return this::optionalEsWhitespace;
                }

                // Fragment opening tag.
                if (code == Codes.greaterThan) {
                    return tagEnd(code);
                }

                // Start of a name.
                if (code != Codes.eof && isStart(code)) {
                    effects.enter(tagNameType);
                    effects.enter(tagNamePrimaryType);
                    effects.consume(code);
                    return this::primaryName;
                }

                return crash(
                        code,
                        "before name",
                        "a character that can start a name, such as a letter, `$`, or `_`" +
                                (code == Codes.exclamationMark
                                        ? " (note: to create a comment in MDX, use `{/* text */}`)"
                                        : "")
                );
            }

            // At the start of a closing tag, right after `</`.
            State beforeClosingTagName(int code) {
                // Fragment closing tag.
                if (code == Codes.greaterThan) {
                    return tagEnd(code);
                }

                // Start of a closing tag name.
                if (code != Codes.eof && isStart(code)) {
                    effects.enter(tagNameType);
                    effects.enter(tagNamePrimaryType);
                    effects.consume(code);
                    return this::primaryName;
                }

                return crash(
                        code,
                        "before name",
                        "a character that can start a name, such as a letter, `$`, or `_`" +
                                (code == Codes.asterisk || code == Codes.slash
                                        ? " (note: JS comments in JSX tags are not supported in MDX)"
                                        : "")
                );
            }

            // Inside the primary name.
            State primaryName(int code) {
                // Continuation of name: remain.
                if (code == Codes.dash || (code != Codes.eof && isCont(code))) {
                    effects.consume(code);
                    return this::primaryName;
                }

                // End of name.
                if (
                        code == Codes.dot ||
                                code == Codes.slash ||
                                code == Codes.colon ||
                                code == Codes.greaterThan ||
                                code == Codes.leftCurlyBrace ||
                                CharUtil.markdownLineEndingOrSpace(code) ||
                                CharUtil.unicodeWhitespace(code)
                ) {
                    effects.exit(tagNamePrimaryType);
                    returnState = this::afterPrimaryName;
                    return optionalEsWhitespace(code);
                }

                return crash(
                        code,
                        "in name",
                        "a name character such as letters, digits, `$`, or `_`; whitespace before attributes; or the end of the tag" +
                                (code == Codes.atSign
                                        ? " (note: to create a link in MDX, use `[text](url)`)"
                                        : "")
                );
            }

            // After a name.
            State afterPrimaryName(int code) {
                // Start of a member name.
                if (code == Codes.dot) {
                    effects.enter(tagNameMemberMarkerType);
                    effects.consume(code);
                    effects.exit(tagNameMemberMarkerType);
                    returnState = this::beforeMemberName;
                    return this::optionalEsWhitespace;
                }

                // Start of a local name.
                if (code == Codes.colon) {
                    effects.enter(tagNamePrefixMarkerType);
                    effects.consume(code);
                    effects.exit(tagNamePrefixMarkerType);
                    returnState = this::beforeLocalName;
                    return this::optionalEsWhitespace;
                }

                // End of name.
                if (
                        code == Codes.slash ||
                                code == Codes.greaterThan ||
                                code == Codes.leftCurlyBrace ||
                                (code != Codes.eof && isStart(code))
                ) {
                    effects.exit(tagNameType);
                    return beforeAttribute(code);
                }

                return crash(
                        code,
                        "after name",
                        "a character that can start an attribute name, such as a letter, `$`, or `_`; whitespace before attributes; or the end of the tag"
                );
            }

            // We’ve seen a `.` and are expecting a member name.
            State beforeMemberName(int code) {
                // Start of a member name.
                if (code != Codes.eof && isStart(code)) {
                    effects.enter(tagNameMemberType);
                    effects.consume(code);
                    return this::memberName;
                }

                return crash(
                        code,
                        "before member name",
                        "a character that can start an attribute name, such as a letter, `$`, or `_`; whitespace before attributes; or the end of the tag"
                );
            }

            // Inside the member name.
            State memberName(int code) {
                // Continuation of member name: stay in state
                if (code == Codes.dash || (code != Codes.eof && isCont(code))) {
                    effects.consume(code);
                    return this::memberName;
                }

                // End of member name (note that namespaces and members can’t be combined).
                if (
                        code == Codes.dot ||
                                code == Codes.slash ||
                                code == Codes.greaterThan ||
                                code == Codes.leftCurlyBrace ||
                                CharUtil.markdownLineEndingOrSpace(code) ||
                                CharUtil.unicodeWhitespace(code)
                ) {
                    effects.exit(tagNameMemberType);
                    returnState = this::afterMemberName;
                    return optionalEsWhitespace(code);
                }

                return crash(
                        code,
                        "in member name",
                        "a name character such as letters, digits, `$`, or `_`; whitespace before attributes; or the end of the tag" +
                                (code == Codes.atSign
                                        ? " (note: to create a link in MDX, use `[text](url)`)"
                                        : "")
                );
            }

            // After a member name: this is the same as `afterPrimaryName` but we don’t
            // expect colons.
            State afterMemberName(int code) {
                // Start another member name.
                if (code == Codes.dot) {
                    effects.enter(tagNameMemberMarkerType);
                    effects.consume(code);
                    effects.exit(tagNameMemberMarkerType);
                    returnState = this::beforeMemberName;
                    return this::optionalEsWhitespace;
                }

                // End of name.
                if (
                        code == Codes.slash ||
                                code == Codes.greaterThan ||
                                code == Codes.leftCurlyBrace ||
                                (code != Codes.eof && isStart(code))
                ) {
                    effects.exit(tagNameType);
                    return beforeAttribute(code);
                }

                return crash(
                        code,
                        "after member name",
                        "a character that can start an attribute name, such as a letter, `$`, or `_`; whitespace before attributes; or the end of the tag"
                );
            }

            // We’ve seen a `:`, and are expecting a local name.
            State beforeLocalName(int code) {
                // Start of a local name.
                if (code != Codes.eof && isStart(code)) {
                    effects.enter(tagNameLocalType);
                    effects.consume(code);
                    return this::localName;
                }

                return crash(
                        code,
                        "before local name",
                        "a character that can start a name, such as a letter, `$`, or `_`" +
                                (code == Codes.plusSign ||
                                        (code > Codes.dot &&
                                                code < Codes.colon) /* `/` - `9` */
                                        ? " (note: to create a link in MDX, use `[text](url)`)"
                                        : "")
                );
            }

            // Inside the local name.
            State localName(int code) {
                // Continuation of local name: stay in state
                if (code == Codes.dash || (code != Codes.eof && isCont(code))) {
                    effects.consume(code);
                    return this::localName;
                }

                // End of local name (note that we don’t expect another colon, or a member).
                if (
                        code == Codes.slash ||
                                code == Codes.greaterThan ||
                                code == Codes.leftCurlyBrace ||
                                CharUtil.markdownLineEndingOrSpace(code) ||
                                CharUtil.unicodeWhitespace(code)
                ) {
                    effects.exit(tagNameLocalType);
                    returnState = this::afterLocalName;
                    return optionalEsWhitespace(code);
                }

                return crash(
                        code,
                        "in local name",
                        "a name character such as letters, digits, `$`, or `_`; whitespace before attributes; or the end of the tag"
                );
            }

            // After a local name: this is the same as `afterPrimaryName` but we don’t
            // expect colons or periods.
            State afterLocalName(int code) {
                // End of name.
                if (
                        code == Codes.slash ||
                                code == Codes.greaterThan ||
                                code == Codes.leftCurlyBrace ||
                                (code != Codes.eof && isStart(code))
                ) {
                    effects.exit(tagNameType);
                    return beforeAttribute(code);
                }

                return crash(
                        code,
                        "after local name",
                        "a character that can start an attribute name, such as a letter, `$`, or `_`; whitespace before attributes; or the end of the tag"
                );
            }

            State beforeAttribute(int code) {
                // Mark as self closing.
                if (code == Codes.slash) {
                    effects.enter(tagSelfClosingMarker);
                    effects.consume(code);
                    effects.exit(tagSelfClosingMarker);
                    returnState = this::selfClosing;
                    return this::optionalEsWhitespace;
                }

                // End of tag.
                if (code == Codes.greaterThan) {
                    return tagEnd(code);
                }

                // Attribute expression.
                if (code == Codes.leftCurlyBrace) {
                    Assert.check(startPoint != null, "expected `startPoint` to be defined");
                    return FactoryMdxExpression.create(
                            context,
                            effects,
                            this::afterAttributeExpression,
                            tagExpressionAttributeType,
                            tagExpressionAttributeMarkerType,
                            tagExpressionAttributeValueType,
                            allowLazy,
                            startPoint.column()
                    ).step(code);
                }

                // Start of an attribute name.
                if (code != Codes.eof && isStart(code)) {
                    effects.enter(tagAttributeType);
                    effects.enter(tagAttributeNameType);
                    effects.enter(tagAttributeNamePrimaryType);
                    effects.consume(code);
                    return this::attributePrimaryName;
                }

                return crash(
                        code,
                        "before attribute name",
                        "a character that can start an attribute name, such as a letter, `$`, or `_`; whitespace before attributes; or the end of the tag"
                );
            }

            // At the start of an attribute expression.
            State afterAttributeExpression(int code) {
                returnState = this::beforeAttribute;
                return optionalEsWhitespace(code);
            }

            // In the attribute name.
            State attributePrimaryName(int code) {
                // Continuation of the attribute name.
                if (code == Codes.dash || (code != Codes.eof && isCont(code))) {
                    effects.consume(code);
                    return this::attributePrimaryName;
                }

                // End of attribute name or tag.
                if (
                        code == Codes.slash ||
                                code == Codes.colon ||
                                code == Codes.equalsTo ||
                                code == Codes.greaterThan ||
                                code == Codes.leftCurlyBrace ||
                                CharUtil.markdownLineEndingOrSpace(code) ||
                                CharUtil.unicodeWhitespace(code)
                ) {
                    effects.exit(tagAttributeNamePrimaryType);
                    returnState = this::afterAttributePrimaryName;
                    return optionalEsWhitespace(code);
                }

                return crash(
                        code,
                        "in attribute name",
                        "an attribute name character such as letters, digits, `$`, or `_`; `=` to initialize a value; whitespace before attributes; or the end of the tag"
                );
            }

            // After an attribute name, probably finding an equals.
            State afterAttributePrimaryName(int code) {
                // Start of a local name.
                if (code == Codes.colon) {
                    effects.enter(tagAttributeNamePrefixMarkerType);
                    effects.consume(code);
                    effects.exit(tagAttributeNamePrefixMarkerType);
                    returnState = this::beforeAttributeLocalName;
                    return this::optionalEsWhitespace;
                }

                // Start of an attribute value.
                if (code == Codes.equalsTo) {
                    effects.exit(tagAttributeNameType);
                    effects.enter(tagAttributeInitializerMarkerType);
                    effects.consume(code);
                    effects.exit(tagAttributeInitializerMarkerType);
                    returnState = this::beforeAttributeValue;
                    return this::optionalEsWhitespace;
                }

                // End of tag / new attribute.
                if (
                        code == Codes.slash ||
                                code == Codes.greaterThan ||
                                code == Codes.leftCurlyBrace ||
                                CharUtil.markdownLineEndingOrSpace(code) ||
                                CharUtil.unicodeWhitespace(code) ||
                                (code != Codes.eof && isStart(code))
                ) {
                    effects.exit(tagAttributeNameType);
                    effects.exit(tagAttributeType);
                    returnState = this::beforeAttribute;
                    return optionalEsWhitespace(code);
                }

                return crash(
                        code,
                        "after attribute name",
                        "a character that can start an attribute name, such as a letter, `$`, or `_`; `=` to initialize a value; or the end of the tag"
                );
            }

            // We’ve seen a `:`, and are expecting a local name.
            State beforeAttributeLocalName(int code) {
                // Start of a local name.
                if (code != Codes.eof && isStart(code)) {
                    effects.enter(tagAttributeNameLocalType);
                    effects.consume(code);
                    return this::attributeLocalName;
                }

                return crash(
                        code,
                        "before local attribute name",
                        "a character that can start an attribute name, such as a letter, `$`, or `_`; `=` to initialize a value; or the end of the tag"
                );
            }

            // In the local attribute name.
            State attributeLocalName(int code) {
                // Continuation of the local attribute name.
                if (code == Codes.dash || (code != Codes.eof && isCont(code))) {
                    effects.consume(code);
                    return this::attributeLocalName;
                }

                // End of tag / attribute name.
                if (
                        code == Codes.slash ||
                                code == Codes.equalsTo ||
                                code == Codes.greaterThan ||
                                code == Codes.leftCurlyBrace ||
                                CharUtil.markdownLineEndingOrSpace(code) ||
                                CharUtil.unicodeWhitespace(code)
                ) {
                    effects.exit(tagAttributeNameLocalType);
                    effects.exit(tagAttributeNameType);
                    returnState = this::afterAttributeLocalName;
                    return optionalEsWhitespace(code);
                }

                return crash(
                        code,
                        "in local attribute name",
                        "an attribute name character such as letters, digits, `$`, or `_`; `=` to initialize a value; whitespace before attributes; or the end of the tag"
                );
            }

            // After a local attribute name, expecting an equals.
            State afterAttributeLocalName(int code) {
                // Start of an attribute value.
                if (code == Codes.equalsTo) {
                    effects.enter(tagAttributeInitializerMarkerType);
                    effects.consume(code);
                    effects.exit(tagAttributeInitializerMarkerType);
                    returnState = this::beforeAttributeValue;
                    return this::optionalEsWhitespace;
                }

                // End of tag / new attribute.
                if (
                        code == Codes.slash ||
                                code == Codes.greaterThan ||
                                code == Codes.leftCurlyBrace ||
                                (code != Codes.eof && isStart(code))
                ) {
                    effects.exit(tagAttributeType);
                    return beforeAttribute(code);
                }

                return crash(
                        code,
                        "after local attribute name",
                        "a character that can start an attribute name, such as a letter, `$`, or `_`; `=` to initialize a value; or the end of the tag"
                );
            }

            // After an attribute value initializer, expecting quotes and such.
            State beforeAttributeValue(int code) {
                // Start of double- or single quoted value.
                if (code == Codes.quotationMark || code == Codes.apostrophe) {
                    effects.enter(tagAttributeValueLiteralType);
                    effects.enter(tagAttributeValueLiteralMarkerType);
                    effects.consume(code);
                    effects.exit(tagAttributeValueLiteralMarkerType);
                    marker = code;
                    return this::attributeValueQuotedStart;
                }

                // Start of an assignment expression.
                if (code == Codes.leftCurlyBrace) {
                    Assert.check(startPoint != null, "expected `startPoint` to be defined");
                    return FactoryMdxExpression.create(
                            context,
                            effects,
                            this::afterAttributeValueExpression,
                            tagAttributeValueExpressionType,
                            tagAttributeValueExpressionMarkerType,
                            tagAttributeValueExpressionValueType,
                            allowLazy,
                            startPoint.column()
                    ).step(code);
                }

                return crash(
                        code,
                        "before attribute value",
                        "a character that can start an attribute value, such as `\"`, `'`, or `{`" +
                                (code == Codes.lessThan
                                        ? " (note: to use an element or fragment as a prop value in MDX, use `{<element />}`)"
                                        : "")
                );
            }

            State afterAttributeValueExpression(int code) {
                effects.exit(tagAttributeType);
                returnState = this::beforeAttribute;
                return optionalEsWhitespace(code);
            }

            // At the start of a quoted attribute value.
            State attributeValueQuotedStart(int code) {
                Assert.check(marker != null, "expected `marker` to be defined");

                if (code == Codes.eof) {
                    crash(
                            code,
                            "in attribute value",
                            "a corresponding closing quote `" + (char) marker.intValue() + '`'
                    );
                }

                if (code == marker) {
                    effects.enter(tagAttributeValueLiteralMarkerType);
                    effects.consume(code);
                    effects.exit(tagAttributeValueLiteralMarkerType);
                    effects.exit(tagAttributeValueLiteralType);
                    effects.exit(tagAttributeType);
                    marker = null;
                    returnState = this::beforeAttribute;
                    return this::optionalEsWhitespace;
                }

                if (CharUtil.markdownLineEnding(code)) {
                    returnState = this::attributeValueQuotedStart;
                    return optionalEsWhitespace(code);
                }

                effects.enter(tagAttributeValueLiteralValueType);
                return attributeValueQuoted(code);
            }

            // In a quoted attribute value.
            State attributeValueQuoted(int code) {
                if (code == Codes.eof || code == marker || CharUtil.markdownLineEnding(code)) {
                    effects.exit(tagAttributeValueLiteralValueType);
                    return attributeValueQuotedStart(code);
                }

                // Continuation.
                effects.consume(code);
                return this::attributeValueQuoted;
            }

            // Right after the slash on a tag, e.g., `<asd /`.
            private State selfClosing(int code) {
                // End of tag.
                if (code == Codes.greaterThan) {
                    return tagEnd(code);
                }

                return crash(
                        code,
                        "after self-closing slash",
                        "`>` to end the tag" +
                                (code == Codes.asterisk || code == Codes.slash
                                        ? " (note: JS comments in JSX tags are not supported in MDX)"
                                        : "")
                );
            }

            // At a `>`.
            State tagEnd(int code) {
                Assert.check(code == Codes.greaterThan, "expected `>`");
                effects.enter(tagMarkerType);
                effects.consume(code);
                effects.exit(tagMarkerType);
                effects.exit(tagType);
                return ok;
            }

            // Optionally start whitespace.
            State optionalEsWhitespace(int code) {
                if (CharUtil.markdownLineEnding(code)) {
                    if (allowLazy) {
                        effects.enter(Types.lineEnding);
                        effects.consume(code);
                        effects.exit(Types.lineEnding);
                        return FactorySpace.create(
                                effects,
                                this::optionalEsWhitespace,
                                Types.linePrefix,
                                Constants.tabSize
                        );
                    }

                    return effects.attempt.hook(
                            lazyLineEnd,
                            FactorySpace.create(
                                    effects,
                                    this::optionalEsWhitespace,
                                    Types.linePrefix,
                                    Constants.tabSize
                            ),
                            this::crashEol
                    ).step(code);
                }

                if (CharUtil.markdownSpace(code) || CharUtil.unicodeWhitespace(code)) {
                    effects.enter("esWhitespace");
                    return optionalEsWhitespaceContinue(code);
                }

                return returnState.step(code);
            }

            // Continue optional whitespace.
            State optionalEsWhitespaceContinue(int code) {
                if (
                        CharUtil.markdownLineEnding(code) ||
                                !(CharUtil.markdownSpace(code) || CharUtil.unicodeWhitespace(code))
                ) {
                    effects.exit("esWhitespace");
                    return optionalEsWhitespace(code);
                }

                effects.consume(code);
                return this::optionalEsWhitespaceContinue;
            }

            private State crashEol(int code) {
                throw new ParseException(
                        "Unexpected lazy line in container, expected line to be prefixed with `>` when in a block quote, whitespace when in a list, etc",
                        context.now(),
                        "micromark-extension-mdx-jsx:unexpected-eof"
                );
            }

            // Crash at a nonconforming character.
            private <T> T crash(int code, String at, String expect) {
                throw new ParseException(
                        "Unexpected " +
                                (code == Codes.eof
                                        ? "end of file"
                                        : "character `" +
                                        (code == Codes.graveAccent
                                                ? "` ` `"
                                                : String.valueOf((char) code)) +
                                        "` (" +
                                        serializeCharCode(code) +
                                        ')') +
                                ' ' +
                                at +
                                ", expected " +
                                expect,
                        context.now(),
                        "micromark-extension-mdx-jsx:unexpected-" +
                                (code == Codes.eof ? "eof" : "character")
                );
            }
        }

        return new StateMachine()::start;
    }

    private static State tokenizeLazyLineEnd(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {
        class StateMachine {

            State start(int code) {
                Assert.check(CharUtil.markdownLineEnding(code), "expected eol");
                effects.enter(Types.lineEnding);
                effects.consume(code);
                effects.exit(Types.lineEnding);
                return this::lineStart;
            }

            private State lineStart(int code) {
                return context.isOnLazyLine() ? nok.step(code) : ok.step(code);
            }
        }

        return new StateMachine()::start;
    }

    private static String serializeCharCode(int code) {
        return String.format(Locale.ROOT, "U+%04X", code);
    }

}
