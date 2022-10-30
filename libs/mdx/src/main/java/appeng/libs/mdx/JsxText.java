package appeng.libs.mdx;

import appeng.libs.micromark.Construct;
import appeng.libs.micromark.State;
import appeng.libs.micromark.TokenizeContext;
import appeng.libs.micromark.Tokenizer;

final class JsxText {

    public static final Construct INSTANCE = new Construct();

    static {
        INSTANCE.tokenize = JsxText::tokenize;
    }

    private static State tokenize(TokenizeContext context, Tokenizer.Effects effects, State ok, State nok) {
        return FactoryTag.create(
                context,
                effects,
                ok,
                nok,
                true,
                "mdxJsxTextTag",
                "mdxJsxTextTagMarker",
                "mdxJsxTextTagClosingMarker",
                "mdxJsxTextTagSelfClosingMarker",
                "mdxJsxTextTagName",
                "mdxJsxTextTagNamePrimary",
                "mdxJsxTextTagNameMemberMarker",
                "mdxJsxTextTagNameMember",
                "mdxJsxTextTagNamePrefixMarker",
                "mdxJsxTextTagNameLocal",
                "mdxJsxTextTagExpressionAttribute",
                "mdxJsxTextTagExpressionAttributeMarker",
                "mdxJsxTextTagExpressionAttributeValue",
                "mdxJsxTextTagAttribute",
                "mdxJsxTextTagAttributeName",
                "mdxJsxTextTagAttributeNamePrimary",
                "mdxJsxTextTagAttributeNamePrefixMarker",
                "mdxJsxTextTagAttributeNameLocal",
                "mdxJsxTextTagAttributeInitializerMarker",
                "mdxJsxTextTagAttributeValueLiteral",
                "mdxJsxTextTagAttributeValueLiteralMarker",
                "mdxJsxTextTagAttributeValueLiteralValue",
                "mdxJsxTextTagAttributeValueExpression",
                "mdxJsxTextTagAttributeValueExpressionMarker",
                "mdxJsxTextTagAttributeValueExpressionValue"
        );
    }

}
