package appeng.libs.mdast.model;

import org.jetbrains.annotations.Nullable;

/**
 * Code (Literal) represents a block of preformatted text, such as ASCII art or computer code.
 * <p>
 * Code can be used where flow content is expected. Its content is represented by its value field.
 * <p>
 * This node relates to the phrasing content concept InlineCode.
 * <p>
 * For example, the following markdown:
 * <p>
 * foo()
 * <p>
 * Yields:
 * <p>
 * {
 * type: 'code',
 * lang: null,
 * meta: null,
 * value: 'foo()'
 * }
 * <p>
 * And the following markdown:
 * <p>
 * ```js highlight-line="2"
 * foo()
 * bar()
 * baz()
 * ```
 * <p>
 * Yields:
 * <p>
 * {
 * type: 'code',
 * lang: 'javascript',
 * meta: 'highlight-line="2"',
 * value: 'foo()\nbar()\nbaz()'
 * }
 */
public class MdAstCode extends MdAstLiteral implements MdAstFlowContent {
    public MdAstCode() {
        super("code");
    }

    /**
     * The language of the code, if not-null.
     */
    @Nullable
    public String lang;

    /**
     * Can be not-null if lang is not-null. It represents custom information relating to the node.
     */
    @Nullable
    public String meta;
}
