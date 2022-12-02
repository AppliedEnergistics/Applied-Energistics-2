package appeng.libs.mdast.model;

/**
 * InlineCode (Literal) represents a fragment of computer code, such as a file name, computer program, or anything a computer could parse.
 * <p>
 * InlineCode can be used where phrasing content is expected. Its content is represented by its value field.
 * <p>
 * This node relates to the flow content concept Code.
 * <p>
 * For example, the following markdown:
 * <p>
 * `foo()`
 * <p>
 * Yields:
 * <p>
 * {type: 'inlineCode', value: 'foo()'}
 */
public class MdAstInlineCode extends MdAstLiteral implements MdAstStaticPhrasingContent {
    public MdAstInlineCode() {
        super("inlineCode");
    }
}
