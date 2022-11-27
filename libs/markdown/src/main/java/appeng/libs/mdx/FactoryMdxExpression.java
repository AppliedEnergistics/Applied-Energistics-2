package appeng.libs.mdx;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.ParseException;
import appeng.libs.micromark.Point;
import appeng.libs.micromark.State;
import appeng.libs.micromark.TokenizeContext;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.factory.FactorySpace;
import appeng.libs.micromark.symbol.Codes;
import appeng.libs.micromark.symbol.Constants;

public final class FactoryMdxExpression {

    private FactoryMdxExpression() {
    }

    public static State create(
            TokenizeContext context,
            Tokenizer.Effects effects,
            State ok,
            String type,
            String markerType,
            String chunkType,
            boolean allowLazy,
            int startColumn
    ) {

        class StateMachine {
            final Tokenizer.Event tail = context.getLastEvent();
            final int initialPrefix =
                    tail != null && tail.token().type.equals(Types.linePrefix)
                            ? tail.context().sliceSerialize(tail.token(), true).length()
                            : 0;
            final int prefixExpressionIndent = initialPrefix != 0 ? initialPrefix + 1 : 0;
            int balance = 1;
            Point startPosition;
            RuntimeException lastCrash;

            State start(int code) {
                Assert.check(code == Codes.leftCurlyBrace, "expected `{`");
                effects.enter(type);
                effects.enter(markerType);
                effects.consume(code);
                effects.exit(markerType);
                startPosition = context.now();
                return this::atBreak;
            }

            State atBreak(int code) {
                if (code == Codes.eof) {
                    if (lastCrash != null) {
                        throw lastCrash;
                    }
                    throw new ParseException(
                            "Unexpected end of file in expression, expected a corresponding closing brace for `{`",
                            context.now(),
                            "micromark-extension-mdx-expression:unexpected-eof"
                    );
                }

                if (code == Codes.rightCurlyBrace) {
                    return atClosingBrace(code);
                }

                if (CharUtil.markdownLineEnding(code)) {
                    effects.enter(Types.lineEnding);
                    effects.consume(code);
                    effects.exit(Types.lineEnding);
                    // `startColumn` is used by the JSX extensions that also wraps this
                    // factory.
                    // JSX can be indented arbitrarily, but expressions can’t exdent
                    // arbitrarily, due to that they might contain template strings
                    // (backticked strings).
                    // We’ll eat up to where that tag starts (`startColumn`), and a tab size.
                    /* c8 ignore next 3 */
                    var prefixTagIndent = startColumn != 0
                            ? startColumn + Constants.tabSize - context.now().column()
                            : 0;
                    var indent = Math.max(prefixExpressionIndent, prefixTagIndent);
                    return indent != 0
                            ? FactorySpace.create(effects, this::atBreak, Types.linePrefix, indent)
                            : this::atBreak;
                }

                var now = context.now();

                if (
                        now.line() != startPosition.line() &&
                                !allowLazy &&
                                context.isOnLazyLine()
                ) {
                    throw new ParseException(
                            "Unexpected end of file in expression, expected a corresponding closing brace for `{`",
                            context.now(),
                            "micromark-extension-mdx-expression:unexpected-eof"
                    );
                }

                effects.enter(chunkType);
                return inside(code);
            }

            State inside(int code) {
                if (
                        code == Codes.eof ||
                                code == Codes.rightCurlyBrace ||
                                CharUtil.markdownLineEnding(code)
                ) {
                    effects.exit(chunkType);
                    return atBreak(code);
                }

                if (code == Codes.leftCurlyBrace) {
                    effects.consume(code);
                    balance++;
                    return this::inside;
                }

                effects.consume(code);
                return this::inside;
            }

            State atClosingBrace(int code) {
                balance--;

                // Agnostic mode: count balanced braces.
                if (balance != 0) {
                    effects.enter(chunkType);
                    effects.consume(code);
                    return this::inside;
                }

                effects.enter(markerType);
                effects.consume(code);
                effects.exit(markerType);
                effects.exit(type);
                return ok;
            }
        }

        return new StateMachine()::start;
    }
}
