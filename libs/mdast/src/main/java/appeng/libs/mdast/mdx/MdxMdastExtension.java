package appeng.libs.mdast.mdx;

import appeng.libs.mdast.MdastContext;
import appeng.libs.mdast.MdastContextProperty;
import appeng.libs.mdast.MdastExtension;
import appeng.libs.mdast.mdx.model.MdxJsxAttribute;
import appeng.libs.mdast.mdx.model.MdxJsxExpressionAttribute;
import appeng.libs.mdast.model.MdAstPosition;
import appeng.libs.micromark.ParseException;
import appeng.libs.micromark.Point;
import appeng.libs.micromark.Token;
import appeng.libs.unist.UnistPosition;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public final class MdxMdastExtension {
    private static final MdastContextProperty<Stack<Tag>> TAG_STACK = new MdastContextProperty<>();
    private static final MdastContextProperty<Tag> TAG = new MdastContextProperty<>();

    public static final MdastExtension INSTANCE = MdastExtension.builder()
            .canContainEol("mdxJsxTextElement")
            .enter("mdxJsxFlowTag", MdxMdastExtension::enterMdxJsxTag)
            .enter("mdxJsxFlowTagClosingMarker", MdxMdastExtension::enterMdxJsxTagClosingMarker)
            .enter("mdxJsxFlowTagAttribute", MdxMdastExtension::enterMdxJsxTagAttribute)
            .enter("mdxJsxFlowTagExpressionAttribute", MdxMdastExtension::enterMdxJsxTagExpressionAttribute)
            .enter("mdxJsxFlowTagAttributeValueLiteral", MdxMdastExtension::buffer)
            .enter("mdxJsxFlowTagAttributeValueExpression", MdxMdastExtension::buffer)
            .enter("mdxJsxFlowTagSelfClosingMarker", MdxMdastExtension::enterMdxJsxTagSelfClosingMarker)
            .enter("mdxJsxTextTag", MdxMdastExtension::enterMdxJsxTag)
            .enter("mdxJsxTextTagClosingMarker", MdxMdastExtension::enterMdxJsxTagClosingMarker)
            .enter("mdxJsxTextTagAttribute", MdxMdastExtension::enterMdxJsxTagAttribute)
            .enter("mdxJsxTextTagExpressionAttribute", MdxMdastExtension::enterMdxJsxTagExpressionAttribute)
            .enter("mdxJsxTextTagAttributeValueLiteral", MdxMdastExtension::buffer)
            .enter("mdxJsxTextTagAttributeValueExpression", MdxMdastExtension::buffer)
            .enter("mdxJsxTextTagSelfClosingMarker", MdxMdastExtension::enterMdxJsxTagSelfClosingMarker)
            .exit("mdxJsxFlowTagClosingMarker", MdxMdastExtension::exitMdxJsxTagClosingMarker)
            .exit("mdxJsxFlowTagNamePrimary", MdxMdastExtension::exitMdxJsxTagNamePrimary)
            .exit("mdxJsxFlowTagNameMember", MdxMdastExtension::exitMdxJsxTagNameMember)
            .exit("mdxJsxFlowTagNameLocal", MdxMdastExtension::exitMdxJsxTagNameLocal)
            .exit("mdxJsxFlowTagExpressionAttribute", MdxMdastExtension::exitMdxJsxTagExpressionAttribute)
            .exit("mdxJsxFlowTagExpressionAttributeValue", MdxMdastExtension::data)
            .exit("mdxJsxFlowTagAttributeNamePrimary", MdxMdastExtension::exitMdxJsxTagAttributeNamePrimary)
            .exit("mdxJsxFlowTagAttributeNameLocal", MdxMdastExtension::exitMdxJsxTagAttributeNameLocal)
            .exit("mdxJsxFlowTagAttributeValueLiteral", MdxMdastExtension::exitMdxJsxTagAttributeValueLiteral)
            .exit("mdxJsxFlowTagAttributeValueLiteralValue", MdxMdastExtension::data)
            .exit("mdxJsxFlowTagAttributeValueExpression", MdxMdastExtension::exitMdxJsxTagAttributeValueExpression)
            .exit("mdxJsxFlowTagAttributeValueExpressionValue", MdxMdastExtension::data)
            .exit("mdxJsxFlowTagSelfClosingMarker", MdxMdastExtension::exitMdxJsxTagSelfClosingMarker)
            .exit("mdxJsxFlowTag", MdxMdastExtension::exitMdxJsxTag)
            .exit("mdxJsxTextTagClosingMarker", MdxMdastExtension::exitMdxJsxTagClosingMarker)
            .exit("mdxJsxTextTagNamePrimary", MdxMdastExtension::exitMdxJsxTagNamePrimary)
            .exit("mdxJsxTextTagNameMember", MdxMdastExtension::exitMdxJsxTagNameMember)
            .exit("mdxJsxTextTagNameLocal", MdxMdastExtension::exitMdxJsxTagNameLocal)
            .exit("mdxJsxTextTagExpressionAttribute", MdxMdastExtension::exitMdxJsxTagExpressionAttribute)
            .exit("mdxJsxTextTagExpressionAttributeValue", MdxMdastExtension::data)
            .exit("mdxJsxTextTagAttributeNamePrimary", MdxMdastExtension::exitMdxJsxTagAttributeNamePrimary)
            .exit("mdxJsxTextTagAttributeNameLocal", MdxMdastExtension::exitMdxJsxTagAttributeNameLocal)
            .exit("mdxJsxTextTagAttributeValueLiteral", MdxMdastExtension::exitMdxJsxTagAttributeValueLiteral)
            .exit("mdxJsxTextTagAttributeValueLiteralValue", MdxMdastExtension::data)
            .exit("mdxJsxTextTagAttributeValueExpression", MdxMdastExtension::exitMdxJsxTagAttributeValueExpression)
            .exit("mdxJsxTextTagAttributeValueExpressionValue", MdxMdastExtension::data)
            .exit("mdxJsxTextTagSelfClosingMarker", MdxMdastExtension::exitMdxJsxTagSelfClosingMarker)
            .exit("mdxJsxTextTag", MdxMdastExtension::exitMdxJsxTag)
            .build();

    private MdxMdastExtension() {
    }

    private static void buffer(MdastContext context, Token token) {
        context.buffer();
    }

    private static void data(MdastContext context, Token token) {
        context.config.enter.data.call(context, token)
        context.config.exit.data.call(context, token)
    }

    private static void enterMdxJsxTag(MdastContext context, Token token) {
        var tag = new Tag(token);
        if (!context.has(TAG_STACK)) {
            context.set(TAG_STACK, new Stack<>());
        }
        context.set(TAG, tag);
        context.buffer();
    }

    private static void enterMdxJsxTagClosingMarker(MdastContext context, Token token) {
        var stack =  context.get(TAG_STACK);

        if (stack.isEmpty()) {
            throw new ParseException(
                    'Unexpected closing slash `/` in tag, expected an open tag first',
                    token.start, token.end,
            'mdast-util-mdx-jsx:unexpected-closing-slash'
      )
        }
    }

    private static void enterMdxJsxTagAnyAttribute(MdastContext context, Token token) {
    const tag = /** @type {Tag} */ (context.get(TAG))

        if (tag.close) {
            throw new ParseException(
                    'Unexpected attribute in closing tag, expected the end of the tag',
                    token.start, token.end,
            'mdast-util-mdx-jsx:unexpected-attribute'
      )
        }
    }

    private static void enterMdxJsxTagSelfClosingMarker(MdastContext context, Token token) {
    const tag = /** @type {Tag} */ (context.get(TAG))

        if (tag.close) {
            throw new ParseException(
                    'Unexpected self-closing slash `/` in closing tag, expected the end of the tag',
                    token.start, token.end,
            'mdast-util-mdx-jsx:unexpected-self-closing-slash'
      )
        }
    }

    private static void exitMdxJsxTagClosingMarker(MdastContext context, Token token) {
    const tag = /** @type {Tag} */ (context.get(TAG))
        tag.close = true
    }

    private static void exitMdxJsxTagNamePrimary(MdastContext context, Token token) {
    const tag = /** @type {Tag} */ (context.get(TAG))
        tag.name = context.sliceSerialize(token)
    }

    private static void exitMdxJsxTagNameMember(MdastContext context, Token token) {
    const tag = /** @type {Tag} */ (context.get(TAG))
        tag.name += '.' + context.sliceSerialize(token)
    }

    private static void exitMdxJsxTagNameLocal(MdastContext context, Token token) {
    const tag = /** @type {Tag} */ (context.get(TAG))
        tag.name += ':' + context.sliceSerialize(token)
    }

    private static void enterMdxJsxTagAttribute(MdastContext context, Token token) {
    const tag = /** @type {Tag} */ (context.get(TAG))
        enterMdxJsxTagAnyAttribute.call(context, token)
        tag.attributes.push({type: 'mdxJsxAttribute', name: '', value: null})
    }

    private static void enterMdxJsxTagExpressionAttribute(MdastContext context, Token token) {
    const tag = /** @type {Tag} */ (context.get(TAG))
        enterMdxJsxTagAnyAttribute.call(context, token)
        tag.attributes.push({type: 'mdxJsxExpressionAttribute', value: ''})
        context.buffer()
    }

    private static void exitMdxJsxTagExpressionAttribute(MdastContext context, Token token) {
    const tag = /** @type {Tag} */ (context.get(TAG))
    const tail = /** @type {MdxJsxExpressionAttribute} */ (
                tag.attributes[tag.attributes.length - 1]
        )
        /** @type {Program|undefined} */
        // @ts-expect-error: custom.
    const estree = token.estree

        tail.value = context.resume()

        if (estree) {
            tail.data = {estree}
        }
    }

    private static void exitMdxJsxTagAttributeNamePrimary(MdastContext context, Token token) {
    const tag = /** @type {Tag} */ (context.get(TAG))
    const node = /** @type {MdxJsxAttribute} */ (
                tag.attributes[tag.attributes.length - 1]
        )
        node.name = context.sliceSerialize(token)
    }

    private static void exitMdxJsxTagAttributeNameLocal(MdastContext context, Token token) {
    const tag = /** @type {Tag} */ (context.get(TAG))
    const node = /** @type {MdxJsxAttribute} */ (
                tag.attributes[tag.attributes.length - 1]
        )
        node.name += ':' + context.sliceSerialize(token)
    }

    private static void exitMdxJsxTagAttributeValueLiteral(MdastContext context, Token token) {
    const tag = /** @type {Tag} */ (context.get(TAG))
        tag.attributes[tag.attributes.length - 1].value = parseEntities(
                context.resume(),
                {nonTerminated: false}
    )
    }

    private static void exitMdxJsxTagAttributeValueExpression(MdastContext context, Token token) {
    const tag = /** @type {Tag} */ (context.get(TAG))
    const tail = /** @type {MdxJsxAttribute} */ (
                tag.attributes[tag.attributes.length - 1]
        )
        /** @type {MdxJsxAttributeValueExpression} */
    const node = {type: 'mdxJsxAttributeValueExpression', value: context.resume()}
        /** @type {Program|undefined} */
        // @ts-expect-error: custom.
    const estree = token.estree

        if (estree) {
            node.data = {estree}
        }

        tail.value = node
    }

    private static void exitMdxJsxTagSelfClosingMarker(MdastContext context, Token token) {
    const tag = /** @type {Tag} */ (context.get(TAG))

        tag.selfClosing = true
    }

    private static void exitMdxJsxTag(MdastContext context, Token token) {
    const tag = /** @type {Tag} */ (context.get(TAG))
    var stack =  (context.get(TAG_STACK))
    const tail = stack[stack.length - 1]

        if (tag.close && tail.name !== tag.name) {
            throw new ParseException(
                    'Unexpected closing tag `' +
                            serializeAbbreviatedTag(tag) +
                            '`, expected corresponding closing tag for `' +
                            serializeAbbreviatedTag(tail) +
                            '` (' +
                            stringifyPosition(tail) +
                            ')',
                    token.start, token.end,
            'mdast-util-mdx-jsx:end-tag-mismatch'
      )
        }

        // End of a tag, so drop the buffer.
        context.resume()

        if (tag.close) {
            stack.pop()
        } else {
            context.enter(
                    {
                            type:
            token.type === 'mdxJsxTextTag'
                    ? 'mdxJsxTextElement'
                    : 'mdxJsxFlowElement',
                    name: tag.name,
                    attributes: tag.attributes,
                    children: []
        },
            token,
                    onErrorRightIsTag
      )
        }

        if (tag.selfClosing || tag.close) {
            context.exit(token, onErrorLeftIsTag)
        } else {
            stack.push(tag)
        }
    }

    /** @type {OnEnterError} */
    function onErrorRightIsTag(closing, open) {
    const tag = /** @type {Tag} */ (context.get(TAG))
    const place = closing ? ' before the end of `' + closing.type + '`' : ''
    const position = closing
                ? {start: closing.start, end: closing.end}
      : undefined

        throw new ParseException(
                'Expected a closing tag for `' +
                        serializeAbbreviatedTag(tag) +
                        '` (' +
                        stringifyPosition({start: open.start, end: open.end}) +
                ')' +
                place,
                position,
                'mdast-util-mdx-jsx:end-tag-mismatch'
    )
    }

    /** @type {OnExitError} */
    function onErrorLeftIsTag(a, b) {
    const tag = /** @type {Tag} */ (context.get(TAG))
        throw new ParseException(
                'Expected the closing tag `' +
                        serializeAbbreviatedTag(tag) +
                        '` either after the end of `' +
                        b.type +
                        '` (' +
                        stringifyPosition(b.end) +
                        ') or another opening tag after the start of `' +
                        b.type +
                        '` (' +
                        stringifyPosition(b.start) +
                        ')',
                {start: a.start, end: a.end},
        'mdast-util-mdx-jsx:end-tag-mismatch'
    )
    }

    /**
     * Serialize a tag, excluding attributes.
     * `self-closing` is not supported, because we don’t need it yet.
     */
    private static String serializeAbbreviatedTag(Tag tag) {
        return '<' + (tag.close ? '/' : '') + (tag.name || '') + '>'
    }

    private static class Tag {
        @Nullable
        String name;
        // Union type MdxJsxAttribute | MdxJsxExpressionAttribute
        List<Object> attributes = new ArrayList<>();
        boolean close;
        boolean selfClosing;
        Point start;
        Point end;

        public Tag(Token token) {
            start = token.start;
            end = token.end;
        }
    }
}
