package appeng.libs.mdx;

import appeng.libs.micromark.Assert;
import appeng.libs.micromark.CharUtil;
import appeng.libs.micromark.Construct;
import appeng.libs.micromark.State;
import appeng.libs.micromark.TokenizeContext;
import appeng.libs.micromark.Tokenizer;
import appeng.libs.micromark.Types;
import appeng.libs.micromark.factory.FactorySpace;
import appeng.libs.micromark.symbol.Codes;

import java.util.ArrayList;

final class JsxFlow {

    public static final Construct INSTANCE = new Construct();

    static {
        INSTANCE.tokenize = JsxFlow::tokenize;
        INSTANCE.concrete = true;
    }

    private static State tokenize(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {
        class StateMachine {
            State start(int code) {
                Assert.check(code == Codes.lessThan, "expected `<`");
                return FactoryTag.create(
                        context,
                        effects,
                        FactorySpace.create(effects, this::after, Types.whitespace),
                        nok,
                        false,
                        "mdxJsxFlowTag",
                        "mdxJsxFlowTagMarker",
                        "mdxJsxFlowTagClosingMarker",
                        "mdxJsxFlowTagSelfClosingMarker",
                        "mdxJsxFlowTagName",
                        "mdxJsxFlowTagNamePrimary",
                        "mdxJsxFlowTagNameMemberMarker",
                        "mdxJsxFlowTagNameMember",
                        "mdxJsxFlowTagNamePrefixMarker",
                        "mdxJsxFlowTagNameLocal",
                        "mdxJsxFlowTagExpressionAttribute",
                        "mdxJsxFlowTagExpressionAttributeMarker",
                        "mdxJsxFlowTagExpressionAttributeValue",
                        "mdxJsxFlowTagAttribute",
                        "mdxJsxFlowTagAttributeName",
                        "mdxJsxFlowTagAttributeNamePrimary",
                        "mdxJsxFlowTagAttributeNamePrefixMarker",
                        "mdxJsxFlowTagAttributeNameLocal",
                        "mdxJsxFlowTagAttributeInitializerMarker",
                        "mdxJsxFlowTagAttributeValueLiteral",
                        "mdxJsxFlowTagAttributeValueLiteralMarker",
                        "mdxJsxFlowTagAttributeValueLiteralValue",
                        "mdxJsxFlowTagAttributeValueExpression",
                        "mdxJsxFlowTagAttributeValueExpressionMarker",
                        "mdxJsxFlowTagAttributeValueExpressionValue"
                ).step(code);
            }

            State after(int code) {
                // Another tag.
                return code == Codes.lessThan
                        ? start(code)
                        : code == Codes.eof || CharUtil.markdownLineEnding(code)
                        ? ok.step(code)
                        : nok.step(code);
            }
        }

        return new StateMachine()::start;
    }

}
