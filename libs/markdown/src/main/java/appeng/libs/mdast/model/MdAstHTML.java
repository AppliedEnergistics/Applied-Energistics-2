package appeng.libs.mdast.model;

/**
 * Represents a fragment of raw HTML.
 * <p>
 * HTML can be used where flow or phrasing content is expected. Its content is represented by its value field.
 * <p>
 * HTML nodes do not have the restriction of being valid or complete HTML ([HTML]) constructs.
 * <p>
 * For example, the following markdown:
 * <p>
 * &lt;div>
 * <p>
 * Yields:
 * <p>
 * {type: 'html', value: '&lt;div>'}
 */
public class MdAstHTML extends MdAstLiteral implements MdAstFlowContent, MdAstStaticPhrasingContent {
    public MdAstHTML() {
        super("html");
    }
}
